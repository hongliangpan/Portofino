package com.manydesigns.portofino.utils;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.HashMap;

/**
 * Created by hongliangpan@gmail.com on 2016/1/24.
 */
public class UserUtils {

    public static Object getLoginUserProperty(String field) {
        Subject subject = SecurityUtils.getSubject();
        if (null == subject.getPrincipals()) {
            return null;
        }
        Object t_primaryPrincipal = subject.getPrincipals().getPrimaryPrincipal();
        if (null == t_primaryPrincipal || !(t_primaryPrincipal instanceof HashMap)) {
            return null;
        }
        if (null != ((HashMap) t_primaryPrincipal).get(field)) {
            return ((HashMap) t_primaryPrincipal).get(field);
        } else {
            return null;
        }
    }

    public static Long getLoginUserId() {
        Object object = getLoginUserProperty("c_id");
        if (null == object) {
            return null;
        }
        return (Long) object;
    }

    public static String getLoginUserName() {
        Object object = getLoginUserProperty("c_name");
        if (null == object) {
            return null;
        }
        return (String) object;
    }

    public static Long getLoginUserBranchId() {
        Object object = getLoginUserProperty("c_region_id");
        if (null == object) {
            return null;
        }
        return (Long) object;
    }

    public static String getSql4Filter(String originalSql,String field,Object value) {
        String sql = originalSql.toLowerCase();
        if (value == null) {
            return originalSql;
        }
        String filter = field+" = " + value;
        if (sql.indexOf("where") < 0) {
            filter = " where " + filter;
        }
        else{
            filter = " and " + filter;
        }
        if (sql.indexOf("order by") > 0) {
            return sql.substring(0, sql.indexOf("order by")) + filter + sql.substring(sql.indexOf("order by"));
        } else {
            return sql + filter;
        }
    }
}
