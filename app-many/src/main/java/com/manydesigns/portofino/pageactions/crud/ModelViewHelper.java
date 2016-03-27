package com.manydesigns.portofino.pageactions.crud;

import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.sql.Connection;
import java.util.List;

/**
 * Created by hongliangpan@gmail.com on 2015/12/16.
 * <p/>
 * <p/>
 * 视图名 ：v_
 * 必须有有  c_id列 主键
 * <p/>
 * 动态创建临时表和主键 tmp_v_
 * 临时表创建主键 c_id
 * <p/>
 * 流程：
 * 1. configuration.xml保存前先替换视图名为 表名 如：from v_ 改为 from tmp_v_
 * 2. 创建临时表及主键  增加注释
 * 3. 表后保存为table.xml
 * 4.需要同步模型
 * 5.查询数据时 使用视图  ，如：from tmp_v_ 改为 from v_
 */
public class ModelViewHelper {

    public static final Logger logger = LoggerFactory.getLogger(Persistence.class);
    public static final String TMP_TABLE_PREFIX = "tmp_";
    public static final String VIEW_PREFIX = "v_";
    public static final String TMP_TABLE_VIEW_PREFIX = TMP_TABLE_PREFIX + VIEW_PREFIX;

    public static boolean isViewName(String viewName) {
        return viewName.startsWith(VIEW_PREFIX);
    }

    public static boolean isTableName(String tableName) {
        return tableName.startsWith(TMP_TABLE_VIEW_PREFIX);
    }

    public static void createViewTable(PageInstance pageInstance, CrudConfiguration configuration, Session session) {

        List<String> tableNames = ModelHelper.parseTableName(configuration.getQuery());
        if (tableNames.size() == 0) {
            return;
        }
        String tableName = tableNames.get(0);
        String viewName = tableName.substring(TMP_TABLE_PREFIX.length());

        ModelHelper.initDb(configuration);

        if (!isViewName(viewName)) {
            return;
        }
        createTable(pageInstance, configuration, session, tableName, viewName);
        Database database = ModelHelper.syncTable(configuration);
        Table table = ModelHelper.getTable(database, tableName);
        if (table == null) {
            return;
        }
        ModelHelper.saveTableToXmlModel(configuration.persistence, table);
        saveViewToXmlModel(configuration.persistence, table, viewName);
        configuration.persistence.loadXmlModel();
    }

    private static void createTable(PageInstance pageInstance, CrudConfiguration configuration, Session session, String tableName, String viewName) {
        if (session == null) {
            session = getSession(configuration);
        }
        String sql = "CREATE TABLE " + tableName + " AS SELECT * FROM " + viewName + " LIMIT 1";
        String dropSql = "DROP TABLE IF EXISTS " + tableName;

        String id;
        if (configuration.getQuery().toLowerCase().contains("id")) {
            id = "id";
        } else {
            id = "c_id";
        }
        String pkSql = "ALTER TABLE " + tableName + " ADD PRIMARY KEY (" + id + ")";

        String remarkSql = "ALTER TABLE " + tableName + " COMMENT='" + pageInstance.getTitle() + "'";
        try {
            QueryUtils.runSqlDml(session, dropSql);
            QueryUtils.runSqlDml(session, sql);
            QueryUtils.runSqlDml(session, pkSql);
            QueryUtils.runSqlDml(session, remarkSql);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Connection getConnection(CrudConfiguration configuration) {
        Connection conn = null;
        ConnectionProvider connectionProvider = configuration.getActualDatabase().getConnectionProvider();
        try {
            logger.debug("Acquiring connection");
            conn = connectionProvider.acquireConnection();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            connectionProvider.releaseConnection(conn);
        }
        return conn;

    }

    public static Session getSession(CrudConfiguration configuration) {
        return configuration.persistence.getSession(configuration.getDatabase());
    }

    public static synchronized void saveViewToXmlModel(Persistence persistence, Table table, String viewName) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File modelDir = ModelHelper.getModelDirectory(persistence);

            File databaseDir = new File(modelDir, table.getDatabaseName());

            File schemaDir = new File(databaseDir, table.getSchemaName());

            table.setTableName(viewName);//xxx
            File tableFile = new File(schemaDir, viewName + ".table.xml");
            m.marshal(table, tableFile);
            persistence.getModel().init();
            logger.info("Saved xml model to file: {}", tableFile);

            //TODO 模型文件变化后怎样同步 到 config或persistence
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String replaceViewNameToTableName(CrudConfiguration configuration) {
        List<String> tableNames = ModelHelper.parseTableName(configuration.getQuery());
        if (tableNames.size() == 0) {
            return "";
        }
        String viewName = tableNames.get(0).toLowerCase();
        if (!viewName.startsWith("v_")) {
            return viewName;
        }
        String tableName = TMP_TABLE_PREFIX + viewName;
        configuration.setQuery(configuration.getQuery().replaceAll(viewName, tableName));
        return tableName;
    }
    public static String replaceTableNameToViewName(CrudConfiguration configuration) {
        List<String> tableNames = ModelHelper.parseTableName(configuration.getQuery());
        if (tableNames.size() == 0) {
            return "";
        }
        String tableName = tableNames.get(0).toLowerCase();
        if (!tableName.startsWith(TMP_TABLE_PREFIX)) {
            return tableName;
        }
        String viewName =  tableName.substring(TMP_TABLE_PREFIX.length());
        configuration.setQuery(configuration.getQuery().replaceAll(tableName,viewName));
        return tableName;
    }
    public static String replaceTableNameToViewName(String sql) {
        List<String> tableNames = ModelHelper.parseTableName(sql);
        if (tableNames.size() == 0) {
            return "";
        }
        String tableName = tableNames.get(0).toLowerCase();
        if (!isTableName(tableName)) {
            return sql;
        }
        String viewName = tableName.substring(TMP_TABLE_PREFIX.length());
        return sql.replaceAll(tableName, viewName);
    }

    public static String getTableName(String viewName) {
        if(isTableName(viewName)){
            return  viewName;
        }
        return TMP_TABLE_PREFIX+viewName;
    }
}
