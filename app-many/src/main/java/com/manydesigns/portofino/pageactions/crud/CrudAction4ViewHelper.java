/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.pageactions.crud;

import com.google.api.client.util.Lists;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Schema;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.annotations.SupportsDetail;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * <p/>
 */

public class CrudAction4ViewHelper {
    protected static final Pattern FROM_PATTERN =
            Pattern.compile("(SELECT\\s+.*\\s+)?FROM\\s+([a-z_$\\u0080-\\ufffe]{1}[a-z_$0-9\\u0080-\\ufffe]*).*",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static Table getTableViewFromQueryString(Database database, String queryString) {
        Matcher matcher = FROM_PATTERN.matcher(queryString);
        String entityName;
        if (matcher.matches()) {
            entityName = matcher.group(2);
        } else {
            return null;
        }

        Table table = findViewByEntityName(database, entityName);
        return table;
    }

    public static Table findViewByEntityName(Database database, String entityName) {
        //TODO
        String sql = "SELECT VIEW_DEFINITION FROM information_schema.`VIEWS` WHERE TABLE_SCHEMA='' AND TABLE_NAME=''";
        //QueryUtils.runSqlReturnMap(session, sql);
        for (Schema schema : database.getSchemas()) {
            for (Table table : schema.getTables()) {
                if (entityName.equals(table.getActualEntityName())) {
                    return table;
                }
            }
        }
        return null;
    }
}
