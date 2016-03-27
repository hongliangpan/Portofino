package com.manydesigns.portofino.utils;

import com.glodon.app.base.dbutils.DbHelper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by hongliangpan@gmail.com on 2016/2/19.
 */
public class DictCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictCache.class);
    /**
     * key=type、二级key=name
     */
    private static LoadingCache<String, Map<String, String>> cache;

    static {
        cache = CacheBuilder.newBuilder().maximumSize(1000).build(
                new CacheLoader<String, Map<String, String>>() {
                    public Map<String, String> load(String type) throws Exception {
                        DbHelper dbHelper = PortofinoModelUtils.getDbHelper("boss");
                        List<Map<String, Object>> datas = dbHelper.find("SELECT c_name,c_value FROM many_dict WHERE c_type_id=?", type);
                        Map result = Maps.newConcurrentMap();
                        for (Map<String, Object> data : datas) {
                            result.put(data.get("c_name"), data.get("c_value"));
                        }
                        return result;
                    }
                }
        );
    }

    public static void invalidate(String type) {
        cache.invalidate(type);
    }

    public static String getValue(String type, String name) {
        try {
            Map<String, String> datas = cache.get(type);
            if (datas != null) {
                return datas.get(name);
            }
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return "";
    }

    public static String getValue(String type, String name, String defaultValue) {
        String result = getValue(type, name);
        if (Strings.isNullOrEmpty(result)) {
            return defaultValue;
        }
        return result;
    }

    public static String getValueFromDb(String type, String name) {
        DbHelper dbHelper = PortofinoModelUtils.getDbHelper("boss");
        Map<String, Object> map = dbHelper.findFirst("SELECT c_value FROM many_dict WHERE c_type_id=? AND c_name=?", type, name);
        if (map != null) {
            return map.get("c_value").toString();
        }
        return "";
    }
}
