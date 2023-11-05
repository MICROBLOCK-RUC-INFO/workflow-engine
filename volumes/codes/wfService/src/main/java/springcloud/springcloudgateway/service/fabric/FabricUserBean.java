package springcloud.springcloudgateway.service.fabric;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
public class FabricUserBean implements User {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private String mspId;
    private String userName;
    private String crtPath;
    private String keyPath;


    public FabricUserBean(String userName, String mspId, String crtPath, String keyPath) {
        this.mspId = mspId;
        this.userName = userName;
        this.crtPath = crtPath;
        this.keyPath = keyPath;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>();
    }

    @Override
    public String getAccount() {
        return "";
    }

    @Override
    public String getAffiliation() {
        return "";
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {
            @Override
            public PrivateKey getKey() {

                PrivateKeyInfo pemPair;
                try {
                    Reader pemReader = new StringReader(new String(IOUtils.toByteArray(new FileInputStream(keyPath))));

                    PEMParser pemParser = new PEMParser(pemReader);
                    pemPair = (PrivateKeyInfo) pemParser.readObject();
                    return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public String getCert() {
                try {
                    return new String(Files.readAllBytes(Paths.get(crtPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
    }

    @Override
    public String getMspId() {
        return mspId;
    }
}
