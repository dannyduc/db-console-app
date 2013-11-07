package com.ingenuity.dbconsole.app.controller;

import lombok.Data;

@Data
public class JdbcConfig {

    private final String datasourceName;
    private final String username;
    private final String password;
    private final String jdbcUrl;
}
