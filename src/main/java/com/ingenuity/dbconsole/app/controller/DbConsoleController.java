package com.ingenuity.dbconsole.app.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RequestMapping("/dbconsole")
@Controller
public class DbConsoleController {

    private Logger logger = Logger.getLogger(getClass());

    private Map<String, DataSource> dataSourceMap;

    @PostConstruct
    public void init() {
        dataSourceMap = DataSourceFactory.getDataSources();
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<String> listDataSources() {
        return dataSourceMap.keySet();
    }

    /**
     *
     * @param dataSourceName name of the database to connect to
     * @param sql statement to execute
     * @return a table represented as list of list.  1st row is the summary or error info, 2nd row is the column header, 3rd row to n is the data
     * @throws SQLException
     */
    @RequestMapping("/{dataSourceName}")
    @ResponseBody
    public List<List<String>> execute(@PathVariable String dataSourceName,
                                      @RequestParam(value = "q", required = false) String sql) {

        if (StringUtils.isBlank(sql)) {
            return Collections.emptyList();
        }

        return execute(dataSourceMap.get(dataSourceName), sql);
    }

    private List<List<String>> execute(DataSource dataSource, String sql) {
        List<List<String>> rows = new ArrayList<List<String>>();
        rows.add(Arrays.asList("init message"));

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            ResultSet rs = null;
            if (statement.execute(sql)) {
                rs = statement.getResultSet();
                rows.set(0, Arrays.asList("query executed"));
                appendResults(rs, rows);
            } else {
                int updatedCount = statement.getUpdateCount();
                rows.set(0, Arrays.asList(updatedCount + "rows updated"));
            }

            if (rs != null) {
                rs.close();
            }
            connection.close();

        } catch (Exception e) {
            logger.error("get error while executing statement " + sql, e);
            rows.set(0, Arrays.asList("error"));
        }

        return rows;
    }

    private void appendResults(ResultSet rs, List<List<String>> rows) throws SQLException {

        ResultSetMetaData meta = rs.getMetaData();
        int len = meta.getColumnCount();
        List<String> headers = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            headers.add(meta.getColumnLabel(i + 1));
        }
        rows.add(headers);

        while (rs.next()) {
            List<String> row = new ArrayList<String>();
            for (int i = 0; i < len; i++) {
                row.add(rs.getString(i + 1));
            }
            rows.add(row);
        }
    }
}
