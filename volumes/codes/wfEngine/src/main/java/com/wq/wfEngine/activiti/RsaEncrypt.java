package com.wq.wfEngine.activiti;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.springframework.util.Base64Utils;

/**
 * 对文件加签、验签工具类
 * 生成私钥：openssl genrsa -out rsa_private_key.pem 1024
 * 私钥还不能直接被使用，需要进行PKCS#8编码：openssl pkcs8 -topk8 -inform PEM -in rsa_private_key.pem -outform PEM -nocrypt
 * 根据私钥生成公钥：openssl rsa -in rsa_private_key.pem -pubout -out rsa_public_key.pem
 * 使用私钥sha512签名：openssl dgst -sha512 -sign rsa_private_key.pem -out xx.tar.gz.sign xx.tar.gz
 * 使用公钥sha512验签：openssl dgst -verify rsa_public_key.pem -sha512 -signature xx.tar.gz.sign xx.tar.gz
 * @author XIHONGLIE
 * @date 2018-03-27
 */
public class RsaEncrypt {
    /**
     * rsa签名
     * @param data  待签名的字符串
     * @param priKey  rsa私钥字符串
     * @return 签名结果
     * @throws Exception    签名失败则抛出异常
     */
    public byte[] rsaSign(byte[] data, RSAPrivateKey priKey)
            throws SignatureException {
        try {
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initSign(priKey);
            signature.update(data);

            byte[] signed = signature.sign();
            return signed;
        } catch (Exception e) {
            throw new SignatureException("RSAcontent = " + data
                    + "; charset = ", e);
        }
    }

    public String rsaSignToStr(byte[] data, RSAPrivateKey priKey)
            throws SignatureException {
        try {
            byte[] signed = rsaSign(data,priKey);
            String signedStr = Base64Utils.encodeToString(signed);
            return signedStr;
        } catch (Exception e) {
            throw new SignatureException("RSAcontent = " + data
                    + "; charset = ", e);
        }
    }

    /**
     * rsa验签
     * @param data  被签名的内容
     * @param sign   签名后的结果
     * @param pubKey   rsa公钥
     * @return 验签结果
     * @throws SignatureException 验签失败，则抛异常
     */
    public boolean verify(byte[] data, byte[] sign, RSAPublicKey pubKey)
            throws SignatureException {
        try {
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify(pubKey);
            signature.update(data);
            return signature.verify(sign);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SignatureException("RSA验证签名[content = " + data
                    + "; charset = " + "; signature = " + sign + "]发生异常!", e);
        }
    }

    /**
     * 私钥
     */
    private RSAPrivateKey privateKey;

    /**
     * 公钥
     */
    private RSAPublicKey publicKey;

    /**
     * 字节数据转字符串专用集合
     */
    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * 获取私钥
     * @return 当前的私钥对象
     */
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取公钥
     * @return 当前的公钥对象
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 随机生成密钥对
     */
    public void genKeyPair() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    /**
     * 从.pem文件中取得私钥
     * @param filePath 文件路径
     * @return 私钥
     */
    public String getPrivateKeyFromFile(String filePath){
        String strPrivateKey = "";
        try {
            BufferedReader privateKey = new BufferedReader(new FileReader(filePath));
            String line = "";
            while((line = privateKey.readLine()) != null){
                strPrivateKey += line;
            }
            privateKey.close();
            strPrivateKey = strPrivateKey.replace("-----BEGIN PRIVATE KEY-----","").replace("-----END PRIVATE KEY-----","");
        }catch (Exception e){
            e.printStackTrace();
        }
        return strPrivateKey;
    }

    /**
     * 从.pem文件中取得公钥
     * @param filePath 文件路径
     * @return 公钥
     */
    public String getPublicKeyFromFile(String filePath){
        String strPublicKey = "";
        try {
            BufferedReader publicKey = new BufferedReader(new FileReader(filePath));
            String line = "";
            while((line = publicKey.readLine()) != null){
                strPublicKey += line;
            }
            publicKey.close();
            strPublicKey = strPublicKey.replace("-----BEGIN PUBLIC KEY-----","").replace("-----END PUBLIC KEY-----","");
        }catch (Exception e){
            e.printStackTrace();
        }
        return strPublicKey;
    }

    /**
     * 从字符串中加载公钥
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public void loadPublicKey(String publicKeyStr) throws Exception {
        try {
            byte[] buffer = Base64Utils.decodeFromString(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        }catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 加载私钥
     * @param privateKeyStr 私钥文件名
     * @return 是否成功
     * @throws Exception
     */
    public void loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64Utils.decodeFromString(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception(e.getMessage());
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 加密过程
     * @param publicKey 公钥
     * @param plainTextData 明文数据
     * @return
     * @throws Exception 加密过程中的异常信息
     */
    public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 解密过程
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     * @throws Exception   解密过程中的异常信息
     */
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * 字节数据转十六进制字符串
     * @param data  输入数据
     * @return 十六进制内容
     */
    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            // 取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            // 取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }

    /**
     * btye转换hex函数
     * @param byteArray
     * @return
     */
    public static String byteToHex(byte[] byteArray) {
        StringBuffer strBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                strBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                strBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return strBuff.toString();
    }

    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     */
    public static byte[] readFileByBytes(String fileName) {
        File file = new File(fileName);
        InputStream in = null;
        byte[] txt = new byte[(int) file.length()];
        try {
            // 一次读一个字节
            in = new FileInputStream(file);
            int tempbyte;
            int i = 0;
            while ((tempbyte = in.read()) != -1) {
                txt[i] = (byte) tempbyte;
                i++;
            }
            in.close();
            return txt;
        } catch (IOException e) {
            e.printStackTrace();
            return txt;
        }
    }

    /**
     * Main 测试方法
     * @param args
     */
    /*
    public static void main(String[] args) {
        RsaEncrypt rsaEncrypt = new RsaEncrypt();
        try {
            String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPTrsUJ26WDSEQwKuAJhQ6XTNHKl1/+bWeyKRQKb0jeCyuiChMxN/qYSgg2BvS2bP51Rb5P9/UE1Rxm5drr3RYNMDvQoXBuA+rHiUX3wkdXmWSaktVbfe5C95N5FCF2jyLMIuWmrMk6Wo3r5MXrCb54A6zU7SzO/r7F0VkpBh9KwIDAQAB";
            rsaEncrypt.loadPublicKey(publicKey);
            System.out.println("加载公钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载公钥失败");
        }
        // 加载私钥
        try {
            String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAM9OuxQnbpYNIRDAq4AmFDpdM0cqXX/5tZ7IpFApvSN4LK6IKEzE3+phKCDYG9LZs/nVFvk/39QTVHGbl2uvdFg0wO9ChcG4D6seJRffCR1eZZJqS1Vt97kL3k3kUIXaPIswi5aasyTpajevkxesJvngDrNTtLM7+vsXRWSkGH0rAgMBAAECgYAlnFEQnP7RNlyTX4E95Kqy1AnjlWoVN8adoiU9bfUkpD7nA0jcdLNzIGFZZBvYKysd3m0ml1ISdddSLTpRjSl8K6O9dJDud3G9Oh3qCGgFflcKuKEXDnlaooX1sBrWF5vS0Gvg98x2C52Pnblm2eGVuTvCMaINDZLaamUlaFldMQJBAO3clhAIBJSWlm7Tt/a1d7+IbsGcS16Rk/N23DHg6LADXxIezxgSrpztSa5Nq81W5RC2WsotYYeRt+8KHsPRRn8CQQDfHa7A7hBTOT/V4FXTApuJUlReUh6cHWPsrxf/rUYKylK9WYBAJv1AZW9KaRtcadyu7ldNMb3MAsbE2vLoW2tVAkEAxhvLAF8tMXSapoO/3MMXkXbYiHjcbU9iooyEqSZhpven3ze51Jr6w8j+bSZTyRpufpTi2TEi4f8D6xvKs91BkQJBAKLS05xiX7GMfwSDQb7LEVzmo0FuJn6BiFHK+fWRqyLmwfkDHvAyQ/FB1TT1fY00iGN09msUWNFQWWSB8HEXfj0CQQCAJYV35rNJ781DXhBH5m1tq74zDNzAqynjm0hqhzlVMSHTYFIeU6SBnhk3swnfvJ8kgy3bQJtohYZ8Tuaz19VN";
            rsaEncrypt.loadPrivateKey(privateKey);
            System.out.println("加载私钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载私钥失败");
        }
        // 测试字符串
        String encryptStr = "12321dsfasf1321312fsfdsafsdafasfsadf";
        try {
            System.out.println(new Date());
            // 加密
            byte[] cipher = rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(),
                    encryptStr.getBytes());
            // 解密
            byte[] plainText = rsaEncrypt.decrypt(rsaEncrypt.getPrivateKey(),
                    cipher);
            System.out.println(new Date());
            System.out.println(new String(plainText));
            byte[] content = readFileByBytes("/data/zhnx/IN/data_xinbao.tar.gz");
            // 签名验证
            byte[] signbyte = rsaEncrypt.rsaSign(content, rsaEncrypt.getPrivateKey());
            System.out.println("签名-----：" + byteToHex(signbyte));
            ByteUtil.saveFile(signbyte,"/data/zhnx/IN/","data_xinbao1.tar.gz.sign");
            Boolean isok = rsaEncrypt.verify(content, signbyte, rsaEncrypt.getPublicKey());
            System.out.println("验证：" + isok);

            // 读取验证文件
            byte[] read = readFileByBytes("/data/zhnx/IN/data_xinbao.tar.gz.sign");
            System.out.println("读取签名文件：" + byteToHex(read));
            Boolean isfok = rsaEncrypt.verify(content, read, rsaEncrypt.getPublicKey());
            System.out.println("文件验证2：" + isfok);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

     */
}
