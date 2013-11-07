package com.ingenuity.dbconsole.app.controller;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataSourceFactory {

    private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

    @PostConstruct
    public void init() {

        String dbDriver = "oracle.jdbc.driver.OracleDriver";
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to dbDriver " + dbDriver, e);
        }

        List<JdbcConfig> configs = JdbcConfigLoader.loadConfigurations();
        for (JdbcConfig config : configs) {
            dataSourceMap.put(config.getDatasourceName(), create(config));
        }
    }

    // http://svn.apache.org/viewvc/commons/proper/dbcp/trunk/doc/ManualPoolingDataSourceExample.java?view=markup&pathrev=1092731
    private DataSource create(JdbcConfig config) {

        ObjectPool connectionPool = new GenericObjectPool(null);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(config.getJdbcUrl(),
                                                                                 config.getUsername(),
                                                                                 config.getPassword());

        String validationQuery = "select sysdate from dual";
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);

        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }

    public static Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(dataSourceMap);
    }
}
