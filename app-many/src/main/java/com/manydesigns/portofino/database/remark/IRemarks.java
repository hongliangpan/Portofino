package com.manydesigns.portofino.database.remark;

import java.sql.Connection;
import java.util.Map;

public interface IRemarks {

    /**
     * 获取表注释
     */
    String getTableRemark(Connection conn, String tableName);

    /**
     * 获取列注释
     */
    String getColumnRemark(Connection conn, String tableSchema, String tableName, String columnName);

    /**
     * 获取表所有列的注释
     */
    Map<String, String> getColumnRemarks(Connection conn, String tableSchema, String tableName);

    Map<String, String> getColumnRemarksBySql(Connection conn,  String sqlText);
}
