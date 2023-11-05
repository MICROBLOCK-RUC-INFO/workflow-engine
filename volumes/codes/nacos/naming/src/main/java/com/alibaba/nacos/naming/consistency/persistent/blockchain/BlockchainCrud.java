package com.alibaba.nacos.naming.consistency.persistent.blockchain;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:08 下午
 */
public interface BlockchainCrud {
    /**
     * 插入数据
     *
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public String fabricPut(String key, String value) throws Exception;

    /**
     * 查询服务
     *
     * @param key
     * @return
     * @throws Exception
     */
    public String fabricQueryByKey(String key) throws Exception;

    /**
     * 删除服务信息
     *
     * @param key
     * @return
     * @throws Exception
     */
    public String fabricDelete(String key) throws Exception;

    /**
     * 查询所有信息
     *
     * @return
     * @throws Exception
     */
    public String fabricQueryAllNamingData() throws Exception;
}
