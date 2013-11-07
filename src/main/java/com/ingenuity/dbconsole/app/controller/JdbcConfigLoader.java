package com.ingenuity.dbconsole.app.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

public class JdbcConfigLoader {

    public static List<JdbcConfig> loadConfigurations() {

        List<JdbcConfig> configs = new ArrayList<JdbcConfig>();

        ResourceBundle resourceBundle = ResourceBundle.getBundle("rosetta");
        Enumeration<String> keys = resourceBundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.endsWith("datasource.url")) {

                String jdbcUrl = resourceBundle.getString(key);

                String dataSourceName = key.split("\\.")[0];
                String username = resourceBundle.getString(String.format("%s.datasource.username", dataSourceName));
                String password = resourceBundle.getString(String.format("%s.datasource.password", dataSourceName));

                configs.add(new JdbcConfig(dataSourceName, username, password, jdbcUrl));
            }
        }

        return configs;
    }

    public static void main(String[] args) {
        List<JdbcConfig> configs = loadConfigurations();
        System.out.println(configs);
    }
}
