package org.activiti.engine.impl.db.cache;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

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
