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
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.blobs.BlobUtils;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.PropertyAccessor;
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
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * <p/>
 */
@SupportsPermissions({CrudAction4View.PERMISSION_CREATE, CrudAction4View.PERMISSION_EDIT,
        CrudAction4View.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template_view.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud4View")
public class CrudAction4View extends CrudAction4AppBase {
    @Override
    protected boolean saveConfiguration(Object configuration) {
        //TODO 如果是第一次 修改配置，如没有lable

        CrudConfiguration   crudConfigurationLocal =(CrudConfiguration) crudConfiguration;
        boolean isFirstView = false;
        if(null==crudConfigurationLocal.getActualTable()){
            isFirstView=true;
        }
        else if(ModelViewHelper.isViewName(crudConfigurationLocal.getActualTable().getTableName())){
            isFirstView=true;
        }

        ModelViewHelper.replaceViewNameToTableName((CrudConfiguration) crudConfiguration);
        boolean result = super.saveConfiguration(configuration);
        //hongliangpan add
        if(isFirstView) {
            createQueryPage();
        }

        //boolean result = super.saveConfiguration(configuration);
        return result;
    }

    //hongliangpan add
    public void createQueryPage() {
        CrudConfiguration configuration = (CrudConfiguration) crudConfiguration;
        ModelViewHelper.createViewTable(pageInstance, configuration, session);
        ModelHelper.createCrudPageAndRemark(pageInstance, configuration, "CrudAction4View.groovy");
    }

    protected void executeSearch() {
        this.setupSearchForm();
        if (this.maxResults == null) {
            this.maxResults = this.getCrudConfiguration().getRowsPerPage();
        }
        //TODO 页面初始化时，把视图改为table
        ModelViewHelper.replaceTableNameToViewName((CrudConfiguration) crudConfiguration);
        this.loadObjects();
        this.setupTableForm(Mode.VIEW);
        BlobUtils.loadBlobs(this.tableForm, this.getBlobManager(), false);
    }

    //TODO 执行sql 时，取 从 视图查询数据
    public void loadObjects() {
        try {
            TableCriteria e = new TableCriteria(this.baseTable);
            if (this.searchForm != null) {
                this.searchForm.configureCriteria(e);
            }

            if (!StringUtils.isBlank(this.sortProperty) && !StringUtils.isBlank(this.sortDirection)) {
                try {
                    PropertyAccessor e1 = this.classAccessor.getProperty(this.sortProperty);
                    e.orderBy(e1, this.sortDirection);
                } catch (NoSuchFieldException var3) {
                    logger.error("Can\'t order by " + this.sortProperty + ", property accessor not found", var3);
                }
            }
            String sql = this.getBaseQuery();
            sql = ModelViewHelper.replaceTableNameToViewName(sql);
            this.objects = QueryUtils.getObjects(this.session, sql, e, this, this.firstResult, this.maxResults);
        } catch (ClassCastException var4) {
            this.objects = new ArrayList();
            logger.warn("Incorrect Field Type", var4);
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type", new Object[0]));
        }

    }
}
