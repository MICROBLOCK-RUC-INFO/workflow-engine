package org.activiti.engine.impl.db.cache;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @apiNote 应该是没用了，最开始是用来缓存静态分配表的，后面在写服务动态绑定的时候，用户任务的分配表和服务任务的绑定服务信息表的缓存就合在一起了，在tableOperator里
 */
public class allocationTable {
    private static ThreadLocal<Stack<ConcurrentHashMap<String,String>>> threadLocalAllocationTable=new ThreadLocal<Stack<ConcurrentHashMap<String,String>>>();

    public static void setAllocationTable(ConcurrentHashMap<String,String> allocationTable) {
        Stack<ConcurrentHashMap<String,String>> stack=threadLocalAllocationTable.get();
        if (stack==null) {
            stack=new Stack<ConcurrentHashMap<String,String>>();
            threadLocalAllocationTable.set(stack);
        }
        stack.push(allocationTable);
    }

    public static ConcurrentHashMap<String,String> getAllocationTable() {
        Stack<ConcurrentHashMap<String,String>> stack=threadLocalAllocationTable.get();
        if (stack.isEmpty()) {
            return null;
          }
          return stack.peek();
    }

    public static void removeAllocationTable() {
        Stack<ConcurrentHashMap<String,String>> stack=threadLocalAllocationTable.get();
        while (!stack.isEmpty()) {
            stack.pop();
        }
        threadLocalAllocationTable.remove();
    }
}
