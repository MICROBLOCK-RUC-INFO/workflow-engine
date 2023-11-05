package org.activiti.engine.impl.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.Entity;

public class sharedMapForThreads {
    /**
     * 设计两个同样的结构，一个为0一个为1，nowUse初始为零，先根据nowUse,选择结构进行写入，每次写入前，对应的writerCounts++
     * 写入完成后writerCounts--,当需要统一写入了，先将nowUse切换，然后所有的还未开始的写入会根据新的nowUse选择结构进行写入
     * 旧的nowUse对应的结构，须等待所有writer写入完毕，writerCounts变为0后，读取数据，再整体将数据写入数据库
     */
    protected static Map<Class<? extends Entity>, Map<String, Entity>> allInsertedObjects
    = new HashMap<Class<? extends Entity>, Map<String, Entity>>();
    protected static Map<Class<? extends Entity>, Map<String, Entity>> allDeletedObjects
    = new HashMap<Class<? extends Entity>, Map<String, Entity>>();
    protected static Map<Class<? extends Entity>, List<BulkDeleteOperation>> allBulkDeleteOperations
    = new HashMap<Class<? extends Entity>, List<BulkDeleteOperation>>();
    protected static List<Entity> allUpdatedObjects = new ArrayList<Entity>();
    protected static int[] writerCounts=new int[2];
    protected static int nowUse=0;//设计两个同样的结构，一个为0一个为1，初始为零，先往0进行写入，每次写入前
}
