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
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.database.TableCriteria;
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
import net.sourceforge.stripes.action.Resolution;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * 写sql 出查询页面，能导入导出
 * TODO
 * <p/>
 */
@SupportsPermissions({CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT,
        CrudAction.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template_sql_query.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud4SqlQuery")
public class CrudAction4SqlQuery extends CrudAction4AppBase {

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();

        crudConfigurationForm.readFromObject(crudConfiguration);

        readPageConfigurationFromRequest();

        crudConfigurationForm.readFromRequest(context.getRequest());

        boolean valid = crudConfigurationForm.validate();
        valid = validatePageConfiguration() && valid;

        if (propertiesTableForm != null) {
            propertiesTableForm.readFromObject(propertyEdits);
            propertiesTableForm.readFromRequest(context.getRequest());
            valid = propertiesTableForm.validate() && valid;
        }

        if (selectionProvidersForm != null) {
            selectionProvidersForm.readFromRequest(context.getRequest());
            valid = selectionProvidersForm.validate() && valid;
        }

        if (valid) {
            updatePageConfiguration();
            if (crudConfiguration == null) {
                crudConfiguration = new com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration();
            }
            crudConfigurationForm.writeToObject(crudConfiguration);

            if (propertiesTableForm != null) {
                updateProperties();
            }

            if (selectionProviderSupport != null &&
                    !selectionProviderSupport.getAvailableSelectionProviderNames().isEmpty()) {
                updateSelectionProviders();
            }

            saveConfiguration(crudConfiguration);
            //hongliangpan add
            createQueryPage();
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
            return getConfigurationView();
        }
    }
    //hongliangpan add
    public void createQueryPage() {
        CrudConfiguration configuration = (CrudConfiguration) crudConfiguration;
        String dbName = configuration.getDatabase();
        Table table = configuration.getActualTable();
        // 构建表及注释信息
        CrudRemarkHelper.buildTableColumnRemarks(session, table);
        String query = configuration.getQuery();
        String title = table.getRemark();

        //hongliangpan add
        CrudNewPageHelper wizard = new CrudNewPageHelper();
        List<ChildPage> childPages = Lists.newArrayList();
        File dir = pageInstance.getDirectory();
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template template = engine.createTemplate(CrudAction.class.getResource("CrudAction4SqlQuery.groovy"));

            wizard.createCrudPage(dir, table, query, childPages, template, CrudRemarkHelper.getBindings(), title, dbName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void loadObjects() {
        //Se si passano dati sbagliati al criterio restituisco messaggio d'errore
        // ma nessun risultato
        try {
            TableCriteria criteria = new TableCriteria(baseTable);
            if(searchForm != null) {
                searchForm.configureCriteria(criteria);
            }
            if(!StringUtils.isBlank(sortProperty) && !StringUtils.isBlank(sortDirection)) {
                try {
                    PropertyAccessor orderByProperty = classAccessor.getProperty(sortProperty);
                    criteria.orderBy(orderByProperty, sortDirection);
                } catch (NoSuchFieldException e) {
                    logger.error("Can't order by " + sortProperty + ", property accessor not found", e);
                }
            }
            objects = QueryUtils.getObjects(session, getBaseQuery(), criteria, this, firstResult, maxResults);
        } catch (ClassCastException e) {
            objects=new ArrayList<Object>();
            logger.warn("Incorrect Field Type", e);
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type"));
        }
    }
}
