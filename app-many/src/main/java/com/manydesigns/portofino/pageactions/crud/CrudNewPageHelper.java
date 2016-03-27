/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.fields.BooleanField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.fields.TextField;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.portofino.actions.admin.appwizard.SelectableRoot;
import com.manydesigns.portofino.actions.admin.database.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.admin.database.forms.SelectableSchema;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Group;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * crud页面，根据表注释自动生成页面label
 * 来自 ApplicationWizard
 * hongliangpan add
 */
@RequiresAuthentication
@RequiresAdministrator
public class CrudNewPageHelper extends AbstractPageAction {

    @SuppressWarnings({"RedundantStringConstructorCall"})
    public static final String NO_LINK_TO_PARENT = new String();

    protected ListMultimap<Table, Reference> children = ArrayListMultimap.create();
    protected int maxColumnsInSummary = 5;
    protected int maxDepth = 5;
    protected int depth;
    public static final int MULTILINE_THRESHOLD = 256;

    private boolean isMultipleRoles(Table fromTable, Reference ref, Collection<Reference> references) {
        boolean multipleRoles = false;
        for (Reference ref2 : references) {
            if (ref2 != ref && ref2.getActualFromColumn().getTable().equals(fromTable)) {
                multipleRoles = true;
                break;
            }
        }
        return multipleRoles;
    }

    public Page createCrudPage(File dir, Table table, String query, List<ChildPage> childPages,
                               Template template, Map<String, String> bindings, String title, String dbName)
            throws Exception {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
                SessionMessages.addWarningMessage(
                        ElementsThreadLocals.getText("couldnt.create.directory", dir.getAbsolutePath()));
                return null;
            }
        }
        logger.info("Creating CRUD page {}", dir.getAbsolutePath());
        CrudConfiguration configuration = new CrudConfiguration();
        configuration.setDatabase(dbName);
        configuration.setupDefaults();

        configuration.setQuery(query);
        String variable = table.getActualEntityName();
        configuration.setVariable(variable);

        configuration.setName(table.getActualEntityName());
        // hongliangpan add
        configuration.setSearchTitle("查询" + title);
        configuration.setCreateTitle("新建" + title);
        configuration.setEditTitle("修改" + title);
        configuration.setReadTitle(title);

        int summ = 0;
        String linkToParentProperty = bindings.get("linkToParentProperty");
        for (Column column : table.getColumns()) {
            summ = setupColumn(column, configuration, summ, linkToParentProperty);
        }

        DispatcherLogic.saveConfiguration(dir, configuration);
        Page page = new Page();
        page.setId(RandomUtil.createRandomId());
        page.setTitle(title);
        page.setDescription(title);

        Collection<Reference> references = children.get(table);
        if (references != null && depth < maxDepth) {
            ArrayList<ChildPage> pages = page.getDetailLayout().getChildPages();
            depth++;
            for (Reference ref : references) {
                createChildCrudPage(dir, template, variable, references, ref, pages, dbName);
            }
            depth--;
            Collections.sort(pages, new Comparator<ChildPage>() {
                public int compare(ChildPage o1, ChildPage o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
        }

        DispatcherLogic.savePage(dir, page);
        File actionFile = new File(dir, "action.groovy");
        FileWriter fileWriter = new FileWriter(actionFile);

        template.make(bindings).writeTo(fileWriter);
        IOUtils.closeQuietly(fileWriter);

        logger.debug("Creating _detail directory");
        File detailDir = new File(dir, PageInstance.DETAIL);
        if (!detailDir.isDirectory() && !detailDir.mkdir()) {
            logger.warn("Could not create detail directory {}", detailDir.getAbsolutePath());
            SessionMessages.addWarningMessage(
                    ElementsThreadLocals.getText("couldnt.create.directory", detailDir.getAbsolutePath()));
        }

        ChildPage childPage = new ChildPage();
        childPage.setName(dir.getName());
        childPage.setShowInNavigation(true);
        childPages.add(childPage);

        return page;

    }


    public boolean isNewConnectionProvider() {
        return false;
    }

    protected int setupColumn
            (Column column, CrudConfiguration configuration, int columnsInSummary, String linkToParentProperty) {

        if (column.getActualJavaType() == null) {
            logger.debug("Column without a javaType, skipping: {}", column.getQualifiedName());
            return columnsInSummary;
        }

        Table table = column.getTable();
        @SuppressWarnings({"StringEquality"})
        boolean enabled =
                !(linkToParentProperty != NO_LINK_TO_PARENT &&
                        column.getActualPropertyName().equals(linkToParentProperty))
                        && !isUnsupportedProperty(column);

        boolean inPk = DatabaseLogic.isInPk(column);
        boolean inFk = DatabaseLogic.isInFk(column);
        boolean inSummary =
                enabled &&
                        (inPk || columnsInSummary < maxColumnsInSummary);
        boolean updatable = enabled && !column.isAutoincrement() && !inPk;
        boolean insertable = enabled && !column.isAutoincrement();

        // hongliangpan add
        if (column.getColumnName().startsWith("c_is_")) {
            column.setJavaType(Boolean.class.getName());
        }

        if (!configuration.isLargeResultSet()) {
            detectBooleanColumn(table, column);
        }

        if (enabled && inPk && !inFk &&
                Number.class.isAssignableFrom(column.getActualJavaType()) &&
                !column.isAutoincrement()) {
            for (PrimaryKeyColumn pkc : table.getPrimaryKey().getPrimaryKeyColumns()) {
                if (pkc.getActualColumn().equals(column)) {
                    pkc.setGenerator(new IncrementGenerator(pkc));
                    insertable = false;
                    break;
                }
            }
        }

        if (column.getActualJavaType() == String.class &&
                (column.getLength() == null || column.getLength() > MULTILINE_THRESHOLD) &&
                isNewConnectionProvider()) {
            Annotation annotation = DatabaseLogic.findAnnotation(column, Multiline.class);
            if (annotation == null) {
                annotation = new Annotation(column, Multiline.class.getName());
                annotation.getValues().add("true");
                column.getAnnotations().add(annotation);
            }
        }

        CrudProperty crudProperty = new CrudProperty();
        crudProperty.setEnabled(enabled);
        crudProperty.setName(column.getActualPropertyName());
        // hongliangpan add
        crudProperty.setLabel(column.getMemo());
        crudProperty.setInsertable(insertable);
        crudProperty.setUpdatable(updatable);
        if (inSummary) {
            crudProperty.setInSummary(true);
            crudProperty.setSearchable(true);
            columnsInSummary++;
        }
        // hongliangpan add 处理扩展字段c_tag1 c_json
        processTagField(crudProperty, column);
        processID(crudProperty, column);

        configuration.getProperties().add(crudProperty);

        return columnsInSummary;
    }

    // hongliangpan add process 处理扩展字段c_tag1 c_json
    private void processID(CrudProperty crudProperty, Column column) {
        if (!column.getColumnName().equalsIgnoreCase("c_id") && !column.getColumnName().equalsIgnoreCase("id")) {
            return;
        }
        crudProperty.setEnabled(false);
        crudProperty.setInsertable(false);
        crudProperty.setUpdatable(false);
        crudProperty.setInSummary(false);
        crudProperty.setSearchable(false);
    }

    // hongliangpan add process 处理扩展字段c_tag1 c_json
    private void processTagField(CrudProperty crudProperty, Column column) {
        if (!column.getColumnName().startsWith("c_tag") && !column.getColumnName().equalsIgnoreCase("c_json")) {
            return;
        }
        crudProperty.setEnabled(false);

        crudProperty.setInsertable(false);
        crudProperty.setUpdatable(false);
        crudProperty.setInSummary(false);
        crudProperty.setSearchable(false);
    }

    protected boolean isUnsupportedProperty(Column column) {
        return column.getJdbcType() == Types.BLOB || column.getJdbcType() == Types.LONGVARBINARY;
    }

    protected final Set<Column> detectedBooleanColumns = new HashSet<Column>();

    protected void detectBooleanColumn(Table table, Column column) {
        if (detectedBooleanColumns.contains(column)) {
            return;
        }
        if (column.getJdbcType() == Types.INTEGER ||
                column.getJdbcType() == Types.DECIMAL ||
                column.getJdbcType() == Types.NUMERIC) {
            logger.info(
                    "Detecting whether numeric column " + column.getQualifiedName() + " is boolean by examining " +
                            "its values...");
            if (column.getColumnName().startsWith("c_is_") || column.getColumnName().startsWith("is_")) {
                detectedBooleanColumns.add(column);
            }
        }
    }

    protected final Map<Table, Boolean> largeResultSet = new HashMap<Table, Boolean>();


    protected void setQueryTimeout(PreparedStatement statement, int seconds) {
        try {
            statement.setQueryTimeout(seconds);
        } catch (Exception e) {
            logger.debug("setQueryTimeout not supported", e);
        }
    }

    public static Long safeGetLong(ResultSet rs, int index) throws SQLException {
        Object object = rs.getObject(index);
        if (object instanceof Number) {
            return ((Number) object).longValue();
        } else {
            return null;
        }
    }

    protected void createChildCrudPage(
            File dir, Template template, String parentName, Collection<Reference> references,
            Reference ref, ArrayList<ChildPage> pages, String dbName)
            throws Exception {
        Column fromColumn = ref.getActualFromColumn();
        Table fromTable = fromColumn.getTable();
        String entityName = fromTable.getActualEntityName();
        String parentProperty = ref.getActualToColumn().getActualPropertyName();
        String linkToParentProperty = fromColumn.getActualPropertyName();
        String childQuery =
                "from " + entityName +
                        " where " + linkToParentProperty +
                        " = %{#" + parentName + "." + parentProperty + "}" +
                        " order by id desc";
        String childDirName = entityName;
        boolean multipleRoles = isMultipleRoles(fromTable, ref, references);
        if (multipleRoles) {
            childDirName += "-as-" + linkToParentProperty;
        }
        File childDir = new File(new File(dir, PageInstance.DETAIL), childDirName);
        String childTitle = Util.guessToWords(childDirName);

        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", parentName);
        bindings.put("parentProperty", parentProperty);
        bindings.put("linkToParentProperty", linkToParentProperty);

        createCrudPage(
                childDir, fromTable, childQuery,
                pages, template, bindings, childTitle, dbName);
    }


    @Override
    public Resolution preparePage() {
        return null;
    }
}
