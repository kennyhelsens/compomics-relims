package com.compomics.relims.model.provider.projectlist;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.interfaces.ProjectListProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * This class is a
 */
public class ProjectListProviderRedis implements ProjectListProvider {


    protected final String projectKey;
    protected final JedisPool jedisPool;

    public ProjectListProviderRedis() {
        projectKey = RelimsProperties.getRedisProjectKey();
        jedisPool = new JedisPool(new JedisPoolConfig(), RelimsProperties.getRedisServer());
    }

    /**
     * Returns the next projectid from the list.
     * Returns -1 if no more projects left.
     *
     * @return
     */
    public long nextProjectID() {
        Jedis jedis = jedisPool.getResource();
        String item = jedis.lpop(projectKey);
        jedisPool.returnResource(jedis);

        if(item == null){
            return -1;
        }else{
            return Long.parseLong(item);
        }

    }
}
