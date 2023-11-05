package com.wq.wfEngine.tool.serviceComposition.simpleJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/*
 * 简易的json解析
 * 使用栈，栈用来缓存对象，StringBuilder缓存字符串,appendStatus true已有左引号等待右引号，false等待左引号
 * 
 */
public class simpleJsonParser {
    @SuppressWarnings("unchecked")
    public static Object parse(String jsonStr) {
        if (jsonStr==null) throw new RuntimeException("simpleJson解析失败,因为json字符串为空");
        //jsonStr=jsonStr.replaceAll("\\\\", "");
        //jsonStr=jsonStr.replaceAll("\"", "");
        int length=jsonStr.length();
        //栈用来暂存对象
        Stack<Object> stack=new Stack<>();
        StringBuilder sb=new StringBuilder();
        boolean isNum=true;
        boolean isDouble=false;
        boolean notString=false;
        boolean appendStatus=false;
        int dotTimes=0;
        boolean isScience=false;
        for (int i=0;i<length;i++) {
            if (jsonStr.charAt(i)=='{') {
                stack.push(new HashMap<String,Object>());
            } else if (jsonStr.charAt(i)=='[') {
                stack.push(new ArrayList<Object>());
            } else if (jsonStr.charAt(i)=='"'&&(!appendStatus)) {
                appendStatus=!appendStatus;
                continue;
            } else if ((jsonStr.charAt(i)=='\\'||jsonStr.charAt(i)==' ')&&(!appendStatus)) {
                continue;
            } else if (jsonStr.charAt(i)==':'&&(!appendStatus)) {
                stack.push(sb.toString());
                sb.setLength(0);
            } else if (jsonStr.charAt(i)==','&&(!appendStatus)) {
                if (sb.length()==0) continue;
                String sbString=sb.toString();
                Object value=sbString;
                if (notString) {
                    if (isNum) {
                        if (isDouble) {
                            value=stringToNum.getDouble(sbString).doubleValue();
                        } else {
                            Integer intNum=stringToNum.getInteger(sbString);
                            if (intNum==null) {
                                value=stringToNum.getLong(sbString).longValue();
                            } else {
                                value=intNum.intValue();
                            }
                        } 
                    }
                    else if (sbString.equals("true")||sbString.equals("false")) {
                        value=Boolean.valueOf(sbString).booleanValue();
                    }
                    notString=false;
                    isNum=true;
                    isDouble=false;
                    dotTimes=0;
                    isScience=false;
                }
                sb.setLength(0);
                if (stack.peek() instanceof String) {
                    String key=String.valueOf(stack.pop());
                    if (stack.peek() instanceof HashMap) {
                        ((HashMap<String,Object>)stack.peek()).put(key,value);
                    } else {
                        throw new RuntimeException("simpleJson解析失败,失败在第"+i+"个字符,json字符串为"+jsonStr);
                    }
                } else if (stack.peek() instanceof ArrayList) {
                    ((ArrayList<Object>)stack.peek()).add(value);
                }
            } else if (jsonStr.charAt(i)==']') {
                if (sb.length()>0) {
                    String sbString=sb.toString();
                    Object value=sbString;
                    if (notString) {
                        if (isNum) {
                            if (isDouble) {
                                value=stringToNum.getDouble(sbString).doubleValue();
                            } else {
                                Integer intNum=stringToNum.getInteger(sbString);
                                if (intNum==null) {
                                    value=stringToNum.getLong(sbString).longValue();
                                } else {
                                    value=intNum.intValue();
                                }
                            } 
                        }
                        notString=false;
                        isNum=true;
                        isDouble=false;
                        dotTimes=0;
                        isScience=false;
                    }
                    ((ArrayList<Object>)stack.peek()).add(value);;
                    sb.setLength(0);
                }
                if (stack.size()>1) {
                    Object value=stack.pop();
                    if (stack.peek() instanceof String) {
                        String key=String.valueOf(stack.pop());
                        if (stack.peek() instanceof HashMap) {
                            ((HashMap<String,Object>)stack.peek()).put(key,value);
                        } else {
                            throw new RuntimeException("simpleJson解析失败,失败在第"+i+"个字符,json字符串为"+jsonStr);
                        }
                    } else if (stack.peek() instanceof ArrayList) {
                        ((ArrayList<Object>)stack.peek()).add(value);
                    }
                }
            } else if (jsonStr.charAt(i)=='}') {
                if (sb.length()>0) {
                    String key=String.valueOf(stack.pop());
                    String sbString=sb.toString();
                    Object value=sbString;
                    if (notString) {
                        if (isNum) {
                            if (isDouble) {
                                value=stringToNum.getDouble(sbString).doubleValue();
                            } else {
                                Integer intNum=stringToNum.getInteger(sbString);
                                if (intNum==null) {
                                    value=stringToNum.getLong(sbString).longValue();
                                } else {
                                    value=intNum.intValue();
                                }
                            } 
                        }
                        else if (sbString.equals("true")||sbString.equals("false")) {
                            value=Boolean.valueOf(sbString).booleanValue();
                        }
                        notString=false;
                        isNum=true;
                        isDouble=false;
                        dotTimes=0;
                        isScience=false;
                    }
                    ((HashMap<String,Object>)stack.peek()).put(key,value);
                    sb.setLength(0);
                }
                if (stack.size()>1) {
                    Map<String,Object> value=((Map<String,Object>)stack.pop());
                    if (stack.peek() instanceof String) {
                        String key=String.valueOf(stack.pop());
                        if (stack.peek() instanceof HashMap) {
                            ((HashMap<String,Object>)stack.peek()).put(key,value);
                        } else {
                            throw new RuntimeException("simpleJson解析失败,失败在第"+i+"个字符,json字符串为"+jsonStr);
                        }
                    } else if (stack.peek() instanceof ArrayList) {
                        ((ArrayList<Object>)stack.peek()).add(value);
                    }
                } else {
                    continue;
                }
            } else {
                if (jsonStr.charAt(i)=='"') {
                    appendStatus=!appendStatus;
                } else {
                    if ((!notString)&&(!appendStatus)) {
                        notString=true;
                    }
                    if (notString&&(jsonStr.charAt(i)<'0'||jsonStr.charAt(i)>'9')) {
                        if (jsonStr.charAt(i)=='.') {
                            isDouble=true;
                            if (dotTimes==0) dotTimes++;
                            else throw new RuntimeException("simpleJson解析失败,失败在第"+i+"个字符,json字符串为"+jsonStr+" 数字不应有多个.");
                        } else if (jsonStr.charAt(i)=='E') {
                            if (dotTimes==1&&!isScience) isScience=true;
                            else throw new RuntimeException("simpleJson解析失败,失败在第"+i+"个字符,json字符串为"+jsonStr+" 科学计数法格式错误");
                        } else {
                            isNum=false;
                        }
                    }
                    if (jsonStr.charAt(i)!='\n'||appendStatus) sb.append(jsonStr.charAt(i));
                }
            }
        }
        if (stack.isEmpty()) {
            throw new RuntimeException("simpleJson解析失败,因为解析最后的栈为空,json字符串为"+jsonStr);
        }
        return stack.peek();
    }
}
