package springcloud.springcloudgateway.workflow.tools;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

public class deCoder {
    public static Object streamToEntity(String value) {
        try {
            return streamToEntity(Base64.getDecoder().decode(value)); 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object streamToEntity(byte[] value) {
        try {
            if (value==null) {
                return null;
            }
            ByteArrayInputStream reader = new ByteArrayInputStream(value);
            ObjectInputStream in=new ObjectInputStream(reader);
            Object res=in.readObject();
            in.close();
            reader.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
