package springcloud.springcloudgateway.workflow.tools;

import java.util.Map;

import org.activiti.engine.impl.db.workflowClass.serviceTaskRes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class jsonTransfer {
    private static ObjectMapper objectMapper=new ObjectMapper();
    public static Map<String,Object> jsonToMap(String jsonStr) {
        try {
            Map<String,Object> res=objectMapper.readValue(jsonStr, new TypeReference<Map<String,Object>>(){});
            return res;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static String mapToJsonString(Map<String,Object> map) {
        try {
            String jsonStr=objectMapper.writeValueAsString(map);
            return jsonStr;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static Map<String,serviceTaskRes> jsonToServiceTaskRes(String jsonStr) {
        try {
            Map<String,serviceTaskRes> res=objectMapper.readValue(jsonStr, new TypeReference<Map<String,serviceTaskRes>>(){});
            return res;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static String serviceTaskResToJsonString(Map<String,serviceTaskRes> serviceTaskRes) {
        try {
            String jsonStr=objectMapper.writeValueAsString(serviceTaskRes);
            return jsonStr;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

}
