package com.manydesigns.portofino.pageactions.crud;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Lists;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hongliangpan@gmail.com on 2015/12/16.
 */
public class ModelHelper {

    public static final Logger logger = LoggerFactory.getLogger(Persistence.class);

    public static void main(String[] args) {
        String sql = "INSERT INTO A\n" +
                "SELECT B.B1, B.B2, C.C1, C.C2\n" +
                "FROM B INNER JOIN C ON B.B0 = C.C0\n" +
                "WHERE B.B0 IN (SELECT D0 FROM D,E,G,H WHERE D.D1 = E.E1) \n";
        parseTableName(sql);
    }

    public static String parseSelectTableName(String sql) {
        sql = sql.replaceAll("\n", "");
        Pattern p = Pattern.compile(
                "(?i)(?<=(?:from)\\s{1,1000})(\\w+)"
        );
        Matcher m = p.matcher(sql);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    public static List<String> parseTableName(String sql) {
        List<String> tables = new ArrayList<>();
        String tableName = parseSelectTableName(sql);
        if (Strings.isNullOrEmpty(tableName)) {
            return tables;
        }
        tables.add(tableName);
        return tables;
    }

    public static List<String> parseTableNameSlow(String sql) {
        String tableName = parseSelectTableName(sql);

        Pattern p = Pattern.compile(
                "(?i)(?<=(?:from|into|update|join)\\s{1,1000}"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?" // 重复这里, 可以多个from后面的表
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + "(?:\\w{1,1000}(?:\\s{0,1000},\\s{0,1000})?)?"
                        + ")(\\w+)"
        );
        Matcher m = p.matcher(sql);
        List<String> tables = new ArrayList<>();
        while (m.find()) {
            tables.add(m.group());
        }
        System.out.println(tables);
        return tables;
    }

    public static void createCrudPageAndRemark(PageInstance pageInstance, CrudConfiguration configuration, String groovyFile) {
        if (null == configuration.getActualTable()) {
            return;
        }
        if (!Strings.isNullOrEmpty(configuration.getActualTable().getColumns().get(0).getMemo())) {
            return;
        }
        Table table = configuration.getActualTable();

        String dbName = configuration.getDatabase();

        // 构建表及注释信息
        CrudRemarkHelper.buildTableColumnRemarks(configuration.persistence.getSession(dbName), table);
        String query = configuration.getQuery();
        String title = table.getRemark();
        CrudNewPageHelper wizard = new CrudNewPageHelper();
        List<ChildPage> childPages = Lists.newArrayList();

        File dir = pageInstance.getDirectory();
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template template = engine.createTemplate(CrudAction.class.getResource(groovyFile));

            wizard.createCrudPage(dir, table, query, childPages, template, CrudRemarkHelper.getBindings(), title, dbName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Database syncTable(CrudConfiguration configuration) {
        if (configuration.getActualTable() != null) {
            return null;
        }
        initDb(configuration);

        if (null == configuration.getActualDatabase() || null == configuration.getActualDatabase().getConnectionProvider()) {
            return null;
        }
        Database database = refreshModel(configuration);
        String query = configuration.getQuery();
        String tableName = parseTableName(query).get(0);
        //query.trim().replaceFirst("from", "FROM").split("FROM")[1].trim().split(" ")[0];
        Table table = ModelHelper.getTable(database, tableName);
        ModelHelper.saveTableToXmlModel(configuration.persistence, table);
        configuration.persistence.loadXmlModel();
        configuration.init();
        return database;
    }

    public static void initDb(CrudConfiguration configuration) {
        // 设置默认的数据库
        if (configuration.getActualDatabase() == null) {
            configuration.getDatabase();
            Database db = DatabaseLogic.findDatabaseByName(configuration.persistence.getModel(), configuration.getDatabase());
            configuration.setActualDatabase(db);
        }
    }

    public static Table getTable(CrudConfiguration configuration, String tableName) {
        Database database = refreshModel(configuration);
        return getTable(database, tableName);
    }

    public static Table getTable(Database database, String tableName) {
        if (null == database) {
            return null;
        }
        List<Table> tables = database.getAllTables();
        for (Table table : tables) {
            if (table.getTableName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    public static Database refreshModel(CrudConfiguration configuration) {
        Model model = configuration.persistence.getModel();
        ConnectionProvider connectionProvider = configuration.getActualDatabase().getConnectionProvider();
        Database database = connectionProvider.getDatabase();
        Database targetDatabase;
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        try {
            synchronized (configuration.persistence) {
                targetDatabase = dbSyncer.syncDatabase(model);
            }
//            Model newModel = new Model();
//            newModel.getDatabases().add(targetDatabase);
//            model.init();
            return targetDatabase;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("error.in.database.synchronization._", e));
        } finally {
            connectionProvider.setDatabase(database); //Restore
        }

        return null;
    }

    public static synchronized void saveTableToXmlModel(Persistence persistence, Table table) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File modelDir = getModelDirectory(persistence);

            File databaseDir = new File(modelDir, table.getDatabaseName());

            File schemaDir = new File(databaseDir, table.getSchemaName());

            File tableFile = new File(schemaDir, table.getTableName() + ".table.xml");
            m.marshal(table, tableFile);
            persistence.getModel().init();
            logger.info("Saved xml model to file: {}", tableFile);

            //TODO 模型文件变化后怎样同步 到 config或persistence
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected static File getModelDirectory(Persistence persistence) {
        return new File(persistence.getAppModelFile().getParentFile(), FilenameUtils.getBaseName(persistence.getAppModelFile().getName()));
    }

}
