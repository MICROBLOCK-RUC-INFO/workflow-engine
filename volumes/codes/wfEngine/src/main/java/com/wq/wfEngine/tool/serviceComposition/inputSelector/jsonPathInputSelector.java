package com.wq.wfEngine.tool.serviceComposition.inputSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.wq.wfEngine.tool.jsonTransfer;

/**
 * @apiNote 用开源的jsonPath做的数据对齐
 */
@Component
public class jsonPathInputSelector implements inputSelectorInterface<String,String>{
    
    public String select(Map<String,Object> dataRoot,String sentence) {
        return select(jsonTransfer.toJsonString(dataRoot), sentence);
    }

    //这个可以用来替换simpleJsonParser与inputSelectParser,使用的开源的JsonPath
    public String select(String dataRootJson,String sentence) {
        if (dataRootJson==null||sentence==null) return null;
        Map<String,String> selectMap=jsonTransfer.jsonToMapStr(sentence);
        Object dataRoot=Configuration.defaultConfiguration().jsonProvider().parse(dataRootJson);
        Map<String,Object> resultMap=new HashMap<String,Object>();
        for (Entry<String,String> entry:selectMap.entrySet()) {
            resultMap.put(entry.getKey(),JsonPath.read(dataRoot, process(entry.getValue())));
        }
        return jsonTransfer.toJsonString(resultMap);
    }

    //这个应该是为了适配
    private String process(String sentence) {
        if (!sentence.startsWith("$")) {
            //if (sentence.startsWith("all")) sentence="$.."+sentence;
            //else sentence="$."+sentence;
            sentence="$."+sentence;
        }
        return sentence;
    }
}
