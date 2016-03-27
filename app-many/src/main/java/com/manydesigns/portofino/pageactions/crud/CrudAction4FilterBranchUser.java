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
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import com.manydesigns.portofino.utils.UserUtils;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.lang.StringUtils;

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
public class CrudAction4FilterBranchUser extends CrudAction4AppBase {
    public void loadObjects() {
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

    public static Long getLoginUserId() {
        return UserUtils.getLoginUserId();
    }

    public static String getLoginUserName() {
        return UserUtils.getLoginUserName();
    }

    public static Long getLoginUserBranchId() {
        return UserUtils.getLoginUserBranchId();
    }

    public String getSql(String originalSql) {
        return originalSql;
    }

    public void processException(Exception e) {
        String message = e.getCause().getMessage();
        if (message.indexOf("Duplicate entry") >= 0 && message.indexOf("_name") >= 0) {
            message = "【对象名称已经存在：" + getFieldValue(message) + "】";
        } else if (message.indexOf("Duplicate entry") >= 0) {
            message = "【数据已经存在：" + getFieldValue(message) + "】";
        } else if (message.indexOf("parent row") >= 0) {
            message = "【数据已经被引用：" + getFieldValue(message) + "】";
        }

        logger.warn("Constraint violation in update" + message, e);
        throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated") + message);
    }

    protected void doDelete(Object object) {
        try {
            this.session.delete(this.baseTable.getActualEntityName(), object);
        } catch (Exception e) {
            processException(e);
        }
    }

    public static String getSql4DeptFilter(String originalSql) {
        String sql = originalSql.toLowerCase();
        Long deptId = getLoginUserBranchId();
        if (deptId == null || deptId <= 10) {
            return originalSql;
        }
        return UserUtils.getSql4Filter(sql, "c_branch_id", deptId);
    }
    protected void createSetup(Object object) {
        HashMap map = (HashMap) object;
        setDefaultValue(map, "c_branch_id", getLoginUserBranchId());
        setDefaultValue(map, "c_servicer", getLoginUserId());
        setDefaultValue(map, "c_province",map.get("c_branch_id"));
    }
    protected void editSetup(Object object) {
        HashMap map = (HashMap) object;
        setDefaultValue(map, "c_branch_id", getLoginUserBranchId());
        setDefaultValue(map, "c_servicer", getLoginUserId());
        setDefaultValue(map, "c_province",map.get("c_branch_id"));
    }

    protected void setIsEdit(CrudProperty prop, boolean isAdmin,String field) {
        if (prop.getName().equals(field)) {
            if (isAdmin) {
                prop.setInsertable(true);
                prop.setUpdatable(true);
            } else {
                prop.setInsertable(false);
                prop.setUpdatable(false);
            }
        }
    }
    protected boolean isAdmin() {
        return  ("super".equals(getLoginUserName()) || "admin".equals(getLoginUserName()) || "wangyg".equals(getLoginUserName()) );
    }
}
