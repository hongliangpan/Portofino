package com.manydesigns.portofino.utils;

import com.glodon.app.base.db.DataSourceUtils;
import com.glodon.app.base.dbutils.DbHelper;
import com.google.api.client.util.Maps;
import com.google.common.io.Resources;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import groovy.sql.Sql;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by hongliangpan@gmail.com on 2016/1/26.
 */
public class PortofinoModelUtils {
    public static final Logger logger = LoggerFactory.getLogger(PortofinoModelUtils.class);
    public static final String APP_JSON = "app.json";
    public static final String APP_MODEL_FILE = "portofino-model.xml";
    protected static Model model = null;

    PortofinoModelUtils() {
        model = loadXmlModel(APP_MODEL_FILE);
    }

    public static synchronized Model loadXmlModel(String dbname) {
        File appModelFile = getModelFile();

        try {
            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Unmarshaller um = jc.createUnmarshaller();
            return (Model) um.unmarshal(appModelFile);
        } catch (Exception e) {
            String msg = "Cannot load/parse model: " + appModelFile;
            logger.error(msg, e);
        }
        return null;
    }

    @NotNull
    private static File getModelFile() {
        try {
            String appDir = new File(Resources.getResource(APP_JSON).getFile()).getParentFile().getParent();
            File appModelFile = new File(appDir + "/" + APP_MODEL_FILE);
            logger.info("Loading xml model from file: {}", appModelFile.getAbsolutePath());
            return appModelFile;
        } catch (Exception e) {
            String msg = "Cannot load model: " + APP_MODEL_FILE;
            logger.error(msg, e);
        }

        return null;
    }


    public static Database getDatabase(String dbname) {
        if (model == null) {
            model = loadXmlModel(APP_MODEL_FILE);
        }
        for (Database database : model.getDatabases()) {
            if (database.getDatabaseName().equals(dbname)) {
                return database;
            }
        }
        return null;
    }

    public static ConnectionProvider getConnectionProvider(String dbname) {
        Database db = getDatabase(dbname);
        if (null == db) {
            return null;
        }
        return db.getConnectionProvider();
    }

    public static DbHelper getDbHelper(String dbname) {
        JdbcConnectionProvider conn = (JdbcConnectionProvider) getConnectionProvider(dbname);
        if (null == conn) {
            return null;
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("url", conn.getUrl());
        map.put("driverClassName", conn.getDriver());
        map.put("username", conn.getUsername());
        map.put("password", conn.getPassword());
        try {
            DataSource dataSource = DataSourceUtils.createDataSource(map);
            return new DbHelper(dataSource);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static Sql getGroovySql(String dbName) {
        JdbcConnectionProvider conn = (JdbcConnectionProvider) getConnectionProvider(dbName);
        if (null == conn) {
            return null;
        }
        try {
            return Sql.newInstance(conn.getUrl(), conn.getUsername(), conn.getPassword(), conn.getDriver());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
