package com.manydesigns.portofino.pageactions.crud;

import com.manydesigns.portofino.database.remark.MysqlRemarks;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hongliangpan@gmail.com on 2015/11/21.
 */
public class CrudRemarkUtils {
    public static final Logger logger = LoggerFactory.getLogger(CrudRemarkUtils.class);

    public static void buildTableColumnRemarks(Session session, Table table) {
        MysqlRemarks remarks = new MysqlRemarks();
        Connection conn = getConnection(session);
        table.setRemark(remarks.getTableRemark(conn, table.getTableName()));
        conn = getConnection(session);
        Map<String, String> columnRemarks = remarks.getColumnRemarks(conn, table.getSchemaName(), table.getTableName());
        for (Column column : table.getColumns()) {
            column.setRemark(columnRemarks.get(column.getColumnName()));
            column.setMemo(column.getRemark());
        }
    }

    @Nullable
    public static Connection getConnection(Session session) {
        Connection conn = null;
        try {
            conn = ((SessionFactoryImplementor) session.getSessionFactory()).getConnectionProvider().getConnection();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return conn;
    }

    public static HashMap<String, String> getbindings(){
        // hongliangpan add
        HashMap<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", "");
        bindings.put("parentProperty", "nothing");
        bindings.put("linkToParentProperty", NO_LINK_TO_PARENT);
        return bindings;
    }
    public static final String NO_LINK_TO_PARENT = new String();
}
