package com.wq.wfEngine.tool;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class hMacMD5 {
    public static int encode(String data,String key) throws InvalidKeyException, NoSuchAlgorithmException {
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "HmacMD5");
        //getInstance得到实例
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        // 加密处理
        BigInteger macMd5 = new BigInteger(mac.doFinal(data.getBytes()));
        String hMacString=macMd5.toString();
        int hashCode=Math.abs(hMacString.hashCode());
        return hashCode;
    }
}
