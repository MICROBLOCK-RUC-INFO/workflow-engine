package com.wq.wfEngine.tool.serviceComposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.StringIdGenerator;
import com.wq.wfEngine.tool.jsonTransfer;
import com.wq.wfEngine.tool.serviceComposition.simpleJson.simpleJsonParser;
/*
 * 当时写的时候以为只是一个示范需求，所以自己写了simpleJsonParser与inputSelectParser
 * 但是如果还有更多需求的话，可能需要考虑替换为https://github.com/json-path/JsonPath.git
 * 这个开源项目，JSONPATH可以通过固定的语法，拿到JSONObject的数据
 */
public class inputSelectParser {
    private static final Logger logger=LoggerFactory.getLogger(inputSelectParser.class);
    public static String getValue(Map<String,Object> root,String valueString) {
        int index=-1;
        Object res=null;
        String[] paths=valueString.split("\\.");
            Object pointer =root;
            List<Object> list=null;
            List<String> listPath=new ArrayList<>();
            for (int i=0;i<paths.length;++i) {
                if (paths[i].charAt(0)=='$') {
                    index=Integer.valueOf(paths[i].substring(1)).intValue();
                    if (i!=paths.length-1) throw new RuntimeException("暂时不支持这种");
                    continue;
                }
                if (pointer==null) {
                    break;
                } else if (pointer instanceof Map) {
                    pointer=((Map<String,Object>)pointer).get(paths[i]);
                } else if (pointer instanceof List) {
                    list=(List<Object>)pointer;
                    if (list.isEmpty()) {
                        pointer=null;
                    } else {
                        pointer=((Map<String,Object>)list.get(0)).get(paths[i]);
                        listPath.add(paths[i]);
                    }
                } else {
                    pointer=null;
                }
            }
            if (pointer!=null) {
                if (list!=null) {
                    List<Object> l=new ArrayList<>();
                    for (Object object:list) {
                        for (String str:listPath) {
                            object=((Map<String,Object>)object).get(str);
                        }
                        l.add(object);
                    }
                    res= String.valueOf(l);
                } else {
                    //这里为什么是toString
                    res= String.valueOf(pointer);
                }

            } else {
                return null;
            }
            if (index!=-1) {
                List<Object> resList=(List<Object>)res;
                res=resList.get(index);
            }
            return String.valueOf(res);
    }

    @SuppressWarnings("unchecked")
    public static String parse(Map<String,Object> root,String sentence) {
        System.out.println(String.format("inputSelect root %s,sentence %s", jsonTransfer.toJsonString(root),sentence));
        //logger.info("inputSelect root {},sentence {}",root,sentence);
        Map<String,Object> mapping=(Map<String,Object>)simpleJsonParser.parse(sentence);
        Map<String,Object> result=new HashMap<>();
        int index=-1;
        
        for (String name:mapping.keySet()) {
            String[] paths=String.valueOf(mapping.get(name)).split("\\.");
            Object pointer =root;
            List<Object> list=null;
            List<String> listPath=new ArrayList<>();
            for (int i=0;i<paths.length;++i) {
                if (paths[i].charAt(0)=='$') {
                    index=Integer.valueOf(paths[i].substring(1)).intValue();
                    if (i!=paths.length-1) throw new RuntimeException("暂时不支持这种");
                    continue;
                }
                if (pointer==null) {
                    break;
                } else if (pointer instanceof Map) {
                    pointer=((Map<String,Object>)pointer).get(paths[i]);
                } else if (pointer instanceof List) {
                    list=(List<Object>)pointer;
                    if (list.isEmpty()) {
                        pointer=null;
                    } else {
                        pointer=((Map<String,Object>)list.get(0)).get(paths[i]);
                        listPath.add(paths[i]);
                    }
                } else {
                    pointer=null;
                }
            }
            if (pointer!=null) {
                if (list!=null) {
                    List<Object> l=new ArrayList<>();
                    for (Object object:list) {
                        for (String str:listPath) {
                            object=((Map<String,Object>)object).get(str);
                        }
                        l.add(object);
                    }
                    result.put(name,l);
                } else {
                    //这里为什么是toString
                    result.put(name,pointer.toString());
                }

            } else {
                result.put(name,null);
            }
            if (index!=-1) {
                List<Object> resList=(List<Object>)result.get(name);
                result.put(name,resList.get(index));
            }
        }
        return jsonTransfer.toJsonString(result);
    }
}
