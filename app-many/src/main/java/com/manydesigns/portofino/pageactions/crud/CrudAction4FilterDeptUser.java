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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.annotations.SupportsDetail;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * <p/>
 */
@SupportsPermissions({CrudAction4FilterProductUser.PERMISSION_CREATE, CrudAction4FilterProductUser.PERMISSION_EDIT,
        CrudAction4FilterProductUser.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template_new_page.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud")
public class CrudAction4FilterDeptUser extends CrudAction4AppBase {
    public void loadObjects() {
        // Se si passano dati sbagliati al criterio restituisco messaggio d'errore
        // ma nessun risultato
        try {
            TableCriteria criteria = new TableCriteria(baseTable);
            if (searchForm != null) {
                searchForm.configureCriteria(criteria);
            }
            if (!StringUtils.isBlank(sortProperty) && !StringUtils.isBlank(sortDirection)) {
                try {
                    PropertyAccessor orderByProperty = classAccessor.getProperty(sortProperty);
                    criteria.orderBy(orderByProperty, sortDirection);
                } catch (NoSuchFieldException e) {
                    logger.error("Can't order by " + sortProperty + ", property accessor not found", e);
                }
            }
            objects = QueryUtils.getObjects(session, getSql(getBaseQuery()), criteria, this, firstResult,
                    maxResults);
        } catch (ClassCastException e) {
            objects = new ArrayList<Object>();
            logger.warn("Incorrect Field Type", e);
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type"));
        }
    }
    @Override
    public long getTotalSearchRecords() {
        // calculate totalRecords
        TableCriteria criteria = new TableCriteria(baseTable);
        if (searchForm != null) {
            searchForm.configureCriteria(criteria);
        }
        QueryStringWithParameters query = QueryUtils.mergeQuery(
                getBaseQuery()
                , criteria, this);

        String queryString = query.getQueryString();
        String totalRecordsQueryString;
        try {
            totalRecordsQueryString = generateCountQuery(queryString);
            totalRecordsQueryString = getSql(totalRecordsQueryString);
        } catch (JSQLParserException e) {
            throw new Error(e);
        }
        List<Object> result = QueryUtils.runHqlQuery(session, totalRecordsQueryString, query.getParameters());
        return (Long) result.get(0);
    }

    public static Long getLoginUserProperty(String field) {
        Subject subject = SecurityUtils.getSubject();
        if (null == subject.getPrincipals()) {
            return null;
        }
        Object t_primaryPrincipal = subject.getPrincipals().getPrimaryPrincipal();
        if (null == t_primaryPrincipal || !(t_primaryPrincipal instanceof HashMap)) {
            return null;
        }
        if (null != ((HashMap) t_primaryPrincipal).get(field)) {
            return (Long) ((HashMap) t_primaryPrincipal).get(field);
        } else {
            return null;
        }
    }

    public static Long getLoginUserId() {
        return getLoginUserProperty("c_id");
    }

    public static Long getLoginUserDeptId() {
        return getLoginUserProperty("c_dept_id");
    }

    public String getSql(String originalSql) {
        return originalSql;
    }
}
