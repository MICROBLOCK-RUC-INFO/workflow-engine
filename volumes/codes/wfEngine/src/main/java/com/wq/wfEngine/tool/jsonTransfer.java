package com.wq.wfEngine.tool;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.workflowClass.serviceTaskRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @apiNote 封装的Json字符串转换
 */
public class jsonTransfer {
    private static ObjectMapper objectMapper=new ObjectMapper();
    private final static Logger logger=LoggerFactory.getLogger(jsonTransfer.class);
    public static Map<String,Object> jsonToMap(String jsonStr) {
        try {
            Map<String,Object> res=objectMapper.readValue(jsonStr, new TypeReference<Map<String,Object>>(){});
            return res;
        } catch (Exception e) {
            logger.error(String.format("jsonTransfer执行失败,因为", e.getMessage()));
            return null;
        }
    }


    public static String toJsonString(Object object) {
        try {
            String jsonStr=objectMapper.writeValueAsString(object);
            return jsonStr;
        } catch (Exception e) {
            logger.error(String.format("jsonTransfer执行失败,因为", e.getMessage()));
            return null;
        }
    }

    public static Map<String,String> jsonToMapStr(String jsonStr) {
        if (jsonStr==null) return null;
        try {
            Map<String,String> res=objectMapper.readValue(jsonStr, new TypeReference<Map<String,String>>(){});
            return res;
        } catch (Exception e) {
            logger.error(String.format("jsonTransfer执行失败,因为", e.getMessage()));
            return null;
        }
    }

    public static Map<String,serviceTaskRes> jsonToServiceTaskRes(String jsonStr) {
        try {
            Map<String,serviceTaskRes> res=objectMapper.readValue(jsonStr, new TypeReference<Map<String,serviceTaskRes>>(){});
            return res;
        } catch (Exception e) {
            logger.error(String.format("jsonTransfer执行失败,因为", e.getMessage()));
            return null;
        }
    }

    public static String serviceTaskResToJsonString(Map<String,serviceTaskRes> serviceTaskRes) {
        try {
            String jsonStr=objectMapper.writeValueAsString(serviceTaskRes);
            return jsonStr;
        } catch (Exception e) {
            logger.error(String.format("jsonTransfer执行失败,因为", e.getMessage()));
            return null;
        }
    }

    public static List<Map<String, Object>> jsonToSigs(String jsonStr) {
        try {
            List<Map<String,Object>> res=objectMapper.readValue(jsonStr, new TypeReference<List<Map<String,Object>>>(){});
            return res;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

}
