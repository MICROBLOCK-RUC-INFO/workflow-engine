package org.activiti.engine.impl.db.redis.tools.operation.verify;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.activiti.engine.impl.db.redis.tools.operation.registry.registerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 用于签名认证，公私钥存在redis中
 */
public class verifyOperator {
    private final static StringRedisTemplate redisClient=redisUtil.getTableRedisClient();
    private final static Logger logger=LoggerFactory.getLogger(verifyOperator.class);

    private final static String PRIVATEKEY="pri";
    private final static String PUBLICKEY="pub";

    public static boolean verify(String name,String data,String signature) {
        try {
            Object pub=redisClient.opsForHash().get(name, PUBLICKEY);
            if (pub==null) return false;
            PublicKey pubkey= KeyFactory.getInstance("EC").
                                generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(String.valueOf(pub).getBytes())));
            Signature sig=Signature.getInstance("SHA256withECDSA");
            sig.initVerify(pubkey);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dataHash = digest.digest(data.getBytes("UTF-8"));
            sig.update(dataHash);
            if (!sig.verify(Base64.getDecoder().decode(signature))) {
                logger.warn(String.format("verify failed\ndataHash after base64:%s\nsig:%s\norg:%s\npubKey:%s", Base64.getEncoder().encodeToString(dataHash),signature,name,pub));
                return false;
            } 
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
