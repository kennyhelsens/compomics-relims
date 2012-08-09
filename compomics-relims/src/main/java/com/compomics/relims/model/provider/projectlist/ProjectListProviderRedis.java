package com.compomics.relims.model.provider.projectlist;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.interfaces.ProjectListProvider;
import redis.clients.jedis.Jedis;

/**
 * This class is a
 */
public class ProjectListProviderRedis implements ProjectListProvider {


    protected final Jedis jedis;
    protected final String projectKey;
    protected final String server;

    public ProjectListProviderRedis() {
        server = RelimsProperties.getRedisServer();
        projectKey = RelimsProperties.getRedisProjectKey();
        jedis = new Jedis(server);
    }

    /**
     * Returns the next projectid from the list.
     * Returns -1 if no more projects left.
     *
     * @return
     */
    public long nextProjectID() {
        String lRpop = jedis.lpop(projectKey);
        if(lRpop == null){
            return -1;
        }else{
            return Long.parseLong(lRpop);
        }
    }
}
