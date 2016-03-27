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

import com.alibaba.fastjson.JSON;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder4ExportAll;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.buttons.GuardType;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.buttons.annotations.Guard;
import com.manydesigns.portofino.files.TempFile;
import com.manydesigns.portofino.files.TempFileService;
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
import com.manydesigns.portofino.util.DbUtils;
import com.manydesigns.portofino.util.ShortNameUtils;
import com.manydesigns.portofino.utils.ContextUtils;
import jxl.write.WriteException;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.shiro.codec.Base64;
import org.hibernate.exception.ConstraintViolationException;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * hongliangpan add this class
 * <p/>
 * Default AbstractCrudAction implementation. Implements a crud page over a database table, based on a HQL query.
 *
 * @author Paolo Predonzani - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla - alessio.stalla@manydesigns.com
 */
@SupportsPermissions({CrudAction4AppBase.PERMISSION_CREATE, CrudAction4AppBase.PERMISSION_EDIT,
        CrudAction4AppBase.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud")
public class CrudAction4AppBase extends CrudAction4UpdateByRole {

    @Buttons({
            @Button(list = "crud-search", key = "copy", order = 1, type = Button.TYPE_SUCCESS,
                    icon = Button.ICON_PLUS + Button.ICON_WHITE, group = "crud"),
            @Button(list = "crud-read", key = "copy", order = 1, type = Button.TYPE_SUCCESS,
                    icon = Button.ICON_PLUS + Button.ICON_WHITE, group = "crud")
    })
    @RequiresPermissions(permissions = PERMISSION_CREATE)
    public Resolution copy() {
        if (object != null) {
            return copyObjectByReadPage();
        }
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("no.object.was.selected"));
            return new RedirectResolution(returnUrl, false);
        }
        if (selection.length != 1) {
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("no.object.was.selected"));
            return new RedirectResolution(returnUrl, false);
        }
        loadObject(selection[0]);
        setupForm(Mode.CREATE);
        createSetup(object);
        form.readFromObject(object);
        return getCreateView();
    }

    private Resolution copyObjectByReadPage() {
        setupForm(Mode.CREATE);

        createSetup(object);
        form.readFromObject(object);
        return getCreateView();
    }

    @Override
    protected void commitTransaction() {
        session.getTransaction().commit();
    }

    @Override
    protected void preEdit() {
        String popup = context.getRequest().getParameter("popup");
        if (null != popup) {
            popupCloseCallback = "close";
        }
        super.preEdit();
    }

    @Override
    protected void preCreate() {
        String popup = context.getRequest().getParameter("popup");
        if (null != popup) {
            popupCloseCallback = "close";
        }
        super.preCreate();
    }

    protected void doDelete(Object object) {
        try {
            this.session.delete(this.baseTable.getActualEntityName(), object);
        } catch (Exception e) {
            processException(e);
        }
    }

    @Override
    protected void doSave(Object object) {
        try {
            processPassword(object);
            processOrder(object);
            processToken(object);
            processModifyUser(object);
            richPojoInfo(object);
            setIsEnabled(object);
            logger.debug(JSON.toJSONString(object));
            session.save(baseTable.getActualEntityName(), object);

        } catch (Exception e) {
            processException(e);
        }
    }

    protected Resolution getSuccessfulSaveView() {
        returnUrl = getReadUrl4Copy(returnUrl);

        if (StringUtils.isEmpty(returnUrl)) {
            return new RedirectResolution(context.getActionPath());
        } else {
            return new RedirectResolution(returnUrl, false);
        }
    }

    private String getReadUrl4Copy(String url) {
        if (object != null) {
            pk = pkHelper.generatePkStringArray(object);
            String idUrl=getPkForUrl(pk);
            if (idUrl != null && !url.endsWith(idUrl)) {
                String endId = url.substring(url.lastIndexOf("/") + 1);
                if (NumberUtils.isDigits(endId)) {
                    getPkForUrl(new String[]{idUrl});
                    url = url.substring(0, url.lastIndexOf("/") + 1) + idUrl;
                }
            }
        }
        return url;
    }

    protected void addSuccessfulSaveInfoMessage() {
        XhtmlBuffer buffer = new XhtmlBuffer();

        pk = pkHelper.generatePkStringArray(object);
        //hongliangpan modify for copy row
        String readUrl = readUrl=getReadUrl4Copy(context.getActionPath());
        String prettyName = ShortNameUtils.getName(getClassAccessor(), object);
        XhtmlBuffer linkToObjectBuffer = new XhtmlBuffer();
        linkToObjectBuffer.writeAnchor(Util.getAbsoluteUrl(readUrl), prettyName);
        buffer.writeNoHtmlEscape(ElementsThreadLocals.getText("object._.saved", linkToObjectBuffer));

        String createUrl = Util.getAbsoluteUrl(context.getActionPath());
        if (!createUrl.contains("?")) {
            createUrl += "?";
        } else {
            createUrl += "&";
        }
        createUrl += "create=";
        createUrl = appendSearchStringParamIfNecessary(createUrl);
        buffer.write(" ");
        buffer.writeAnchor(createUrl, ElementsThreadLocals.getText("create.another.object"));

        SessionMessages.addInfoMessage(buffer);
    }

    protected void setIsEnabled(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        setDefaultValue(map, "c_is_enabled", true);
        setDefaultValue(map, "c_is_deleted", false);
    }

    protected void richPojoInfo(Object object) {

    }

    protected void setValue(HashMap map, String field, Object value) {
        if (null == value) {
            return;
        }
        map.put(field, value);
    }

    protected void setDefaultValue(HashMap map, String field, Object defaultValue) {
        Object value = map.get(field);
        if (null == value || Strings.isNullOrEmpty(value.toString())) {
            map.put(field, defaultValue);
        }
    }

    public void processException(ConstraintViolationException e) {
        String message = e.getCause().getMessage();
        if (message.indexOf("Duplicate entry") >= 0 && message.indexOf("_name") >= 0) {
            message = "【对象名称已经存在：" + getFieldValue(message) + "】";
        } else if (message.indexOf("Duplicate entry") >= 0) {
            message = "【数据已经存在：" + getFieldValue(message) + "】";
        }
        logger.warn("Constraint violation in update" + message, e);
        throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated") + message);
    }

    public String getFieldValue(String message) {
        return message.substring(message.indexOf("'") + 1, message.indexOf("' for key"));
    }

    @Override
    protected void doUpdate(Object object) {
        try {
            processPassword(object);
            processShortName(object);
            processOrder(object);
            processToken(object);
            processModifyUser(object);
            richPojoInfo(object);
            logger.info(JSON.toJSONString(object));
            session.update(baseTable.getActualEntityName(), object);
        } catch (ConstraintViolationException e) {
            processException(e);
        }
    }

    public void processPassword(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        String password = "c_password";
        if (map.containsKey(password)) {
            Object value = map.get(password);
            value = encryptPassword(value.toString());
            map.put(password, value);
        }
    }

    String encryptPassword(String password) {
        return md5Base64(password);
    }

    String md5Base64(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes("UTF-8"));
            byte[] raw = md.digest();
            return toBase64(raw);
        } catch (NoSuchAlgorithmException e) {
            processException(e);
        } catch (UnsupportedEncodingException e) {
            processException(e);
        }
        return password;
    }

    private void processException(Exception e) {
        logger.error(e.getMessage(), e);
    }

    protected String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes);
    }

    // hongliangpan add
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processShortName(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        String shortName = "c_short_name";
        String name = "c_name";
        if (map.containsKey(shortName) && map.containsKey(name)) {
            Object value = map.get(shortName);
            if (StringUtils.isBlank(String.valueOf(value))) {
                value = map.get(name);
                map.put(shortName, value);
            }
        }
    }

    // hongliangpan add
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processOrder(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        String order = "c_order";
        String id = "c_id";
        Object value = map.get(order);
        if (map.containsKey(order) && map.containsKey(id)) {
            if (null == value || "0".equals(String.valueOf(value))) {
                value = map.get(id);
                if (NumberUtils.isNumber(value.toString())) {
                    map.put(order, value);
                }
            }
        } else if (map.containsKey(order) && (null == value || StringUtils.isBlank((String.valueOf(value))))) {
            String table = this.getBaseTable().getTableName();
            String sql = "SELECT max(c_id)+1 FROM " + table;
            value = QueryUtils.runSql(session, sql).get(0)[0];
            map.put(order, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processToken(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        String token = "c_token";
        Object value = map.get(token);
        if (map.containsKey(token)) {
            if (null == value || "".equals(String.valueOf(value))) {
                map.put(token, UUID.randomUUID().toString());
            }
        }
    }

    // hongliangpan add
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processOrderAfter(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        String order = "c_order";
        String id = "c_id";
        if (map.containsKey(order) && map.containsKey(id)) {
            String table = this.getBaseTable().getTableName();
            String updateSql = "UPDATE " + table + " SET c_order =c_id WHERE c_order <= 0 OR c_order IS NULL";
            QueryUtils.runSql(session, updateSql);
            // TODO
        }
    }

    public void processModifyUser(Object object) {
        if (!(object instanceof HashMap)) {
            return;
        }

        HashMap map = (HashMap) object;
        setDefaultValue(map, "c_create_time", new Date());
        setValue(map, "c_modify_time", new Date());

        String userName = ContextUtils.getLoginUser();
        setDefaultValue(map, "c_create_user", userName);
        setValue(map, "c_modify_user", userName);

    }

    // **************************************************************************
    // Object loading
    // **************************************************************************

    public void loadObjects() {
        super.loadObjects();
    }

    public void loadObjects(boolean isExport) {
        loadObjects();
    }

    // hongliangpan copy from 4.0.10
    // **************************************************************************
    // ExportSearch
    // **************************************************************************
    // --------------------------------------------------------------------------
    // Export
    // --------------------------------------------------------------------------

    protected static final String TEMPLATE_FOP_SEARCH = "export/templateFOP-Search.xsl";
    protected static final String TEMPLATE_FOP_READ = "export/templateFOP-Read.xsl";

    @Button(list = "crud-search", key = "commons.exportExcel", order = 9, group = "crud", icon = " icon-th glyphicon-export ", type = Button.TYPE_DANGER)
    public Resolution exportSearchExcel() {
        try {
            TempFileService fileService = TempFileService.getInstance();
            String fileName = ExportJxlUtils.getFileName(crudConfiguration);
            TempFile tempFile = fileService.newTempFile("application/vnd.ms-excel", fileName);
            OutputStream outputStream = tempFile.getOutputStream();
            exportSearchExcel(outputStream);
            outputStream.flush();
            outputStream.close();
            return fileService.stream(tempFile);
        } catch (Exception e) {
            logger.error("Excel export failed", e);
            SessionMessages.addErrorMessage(getMessage("commons.export.failed"));
            // return new RedirectResolution(getDispatch().getOriginalPath());
            return new RedirectResolution(getReturnUrl());
        }
    }

    public void exportSearchExcel(OutputStream outputStream) {
        setupSearchForm();
        loadObjects(true);
        setupTableForm4ExportAll(Mode.VIEW);

        ExportJxlUtils.writeFileSearchExcel(outputStream, crudConfiguration, tableForm, form, searchForm);
    }

    protected void setupTableForm4ExportAll(Mode mode) {
        TableFormBuilder4ExportAll tableFormBuilder = new TableFormBuilder4ExportAll(classAccessor);
        configureTableFormSelectionProviders(tableFormBuilder);

        int nRows;
        if (objects == null) {
            nRows = 0;
        } else {
            nRows = objects.size();
        }

        configureTableFormBuilder(tableFormBuilder, mode, nRows);
        tableForm = buildTableForm(tableFormBuilder);

        if (objects != null) {
            tableForm.readFromObject(objects);
            refreshTableBlobDownloadHref();
        }
    }


    // **************************************************************************
    // ExportRead
    // **************************************************************************

    @Button(list = "crud-read", key = "commons.exportExcel", order = 9)
    public Resolution exportReadExcel() {
        try {
            TempFileService fileService = TempFileService.getInstance();
            String fileName = ExportJxlUtils.getFileName(crudConfiguration);
            TempFile tempFile = fileService.newTempFile("application/vnd.ms-excel", fileName);
            OutputStream outputStream = tempFile.getOutputStream();
            exportReadExcel(outputStream);
            outputStream.flush();
            outputStream.close();
            return fileService.stream(tempFile);
        } catch (Exception e) {
            logger.error("Excel export failed", e);
            SessionMessages.addErrorMessage(getMessage("commons.export.failed"));
            // return new RedirectResolution(getDispatch().getOriginalPath());
            return new RedirectResolution(getReturnUrl());
        }
    }

    public void exportReadExcel(OutputStream outputStream) throws IOException, WriteException {
        setupSearchForm();

        loadObjects(true);

        setupForm(Mode.VIEW);
        form.readFromObject(object);

        ExportJxlUtils.writeFileReadExcel(outputStream, crudConfiguration, tableForm, form, searchForm);
    }


    // **************************************************************************
    // exportSearchPdf
    // **************************************************************************

    @Button(list = "crud-search", key = "commons.exportPdf", order = 8, group = "crud", icon = " icon-share glyphicon-export ")
    public Resolution exportSearchPdf() {
        try {
            TempFileService fileService = TempFileService.getInstance();
            String fileName = ExportJxlUtils.getFileName(crudConfiguration, "pdf");
            TempFile tempFile = fileService.newTempFile(MimeTypes.APPLICATION_PDF, fileName);
            OutputStream outputStream = tempFile.getOutputStream();
            exportSearchPdf(outputStream);
            outputStream.flush();
            outputStream.close();
            return fileService.stream(tempFile);
        } catch (Exception e) {
            logger.error("PDF export failed", e);
            SessionMessages.addErrorMessage(getMessage("commons.export.failed"));
            // return new RedirectResolution(getDispatch().getOriginalPath());
            return new RedirectResolution(getReturnUrl());
        }
    }

    public void exportSearchPdf(OutputStream outputStream) throws FOPException, IOException, TransformerException {

        setupSearchForm();

        loadObjects(true);

        setupTableForm4ExportAll(Mode.VIEW);

        FopFactory fopFactory = FopFactory.newInstance();
        // hongliangpan add
        try {
            String fopConfig = "fop.xml";
            String filePath = CrudAction.class.getClassLoader().getResource(fopConfig).getPath();
            fopFactory.setUserConfig(filePath);
            // String fonts = "/fonts"; 的上级目录
            fopFactory.setFontBaseURL(new File(filePath).getParent());
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        }
        InputStream xsltStream = null;
        try {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outputStream);

            xsltStream = getSearchPdfXsltStream();

            // Setup XSLT
            TransformerFactory Factory = TransformerFactory.newInstance();
            Transformer transformer = Factory.newTransformer(new StreamSource(xsltStream));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            Reader reader = composeXmlSearch();
            Source src = new StreamSource(reader);

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
            reader.close();

            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }
    }

    /**
     * Returns a stream producing the contents of a XSLT document to produce the PDF export of the current search
     * results.
     */
    protected InputStream getSearchPdfXsltStream() {
        String templateFop = TEMPLATE_FOP_SEARCH;
        return getXsltStream(templateFop);
    }

    /**
     * Returns a XSLT stream by searching for a file first in this action's directory, then at the root of the
     * classpath.
     *
     * @param templateFop the file to search for
     * @return the stream
     */
    protected InputStream getXsltStream(String templateFop) {
        // hongliangpan change dir 原来是 webapp\WEB-INF\pages\t_customer
        File dir = pageInstance.getDirectory();

        File fopFile = new File(dir, templateFop);
        if (fopFile.exists()) {
            logger.debug("Custom FOP template found: {}", fopFile);
            try {
                return new FileInputStream(fopFile);
            } catch (FileNotFoundException e) {
                throw new Error(e);
            }
        } else {
            logger.debug("Using default FOP template: {}", templateFop);
            ClassLoader cl = getClass().getClassLoader();
            return cl.getResourceAsStream(templateFop);
        }
    }

    /**
     * Composes an XML document representing the current search results.
     *
     * @return
     * @throws IOException
     */
    protected Reader composeXmlSearch() throws IOException {
        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        xb.write(crudConfiguration.getSearchTitle());
        xb.closeElement("table");

        double[] columnSizes = setupXmlSearchColumnSizes();

        for (double columnSize : columnSizes) {
            xb.openElement("column");
            xb.openElement("width");
            xb.write(columnSize + "em");
            xb.closeElement("width");
            xb.closeElement("column");
        }

        for (TableForm.Column col : tableForm.getColumns()) {
            xb.openElement("header");
            xb.openElement("nameColumn");
            xb.write(col.getLabel());
            xb.closeElement("nameColumn");
            xb.closeElement("header");
        }

        for (TableForm.Row row : tableForm.getRows()) {
            xb.openElement("rows");
            for (Field field : row) {
                xb.openElement("row");
                xb.openElement("value");
                xb.write(field.getStringValue());
                xb.closeElement("value");
                xb.closeElement("row");
            }
            xb.closeElement("rows");
        }

        xb.closeElement("class");

        return new StringReader(xb.toString());
    }

    /**
     * <p>
     * Returns an array of column sizes (in characters) for the search export.<br />
     * By default, sizes are computed comparing the relative sizes of each column, consisting of the header and the
     * values produced by the search.
     * </p>
     * <p>
     * Users can override this method to compute the sizes using a different algorithm, or hard-coding them for a
     * particular CRUD instance.
     * </p>
     */
    protected double[] setupXmlSearchColumnSizes() {
        double[] headerSizes = new double[tableForm.getColumns().length];
        for (int i = 0; i < headerSizes.length; i++) {
            TableForm.Column col = tableForm.getColumns()[i];
            int length = StringUtils.length(col.getLabel());
            headerSizes[i] = length;
        }

        double[] columnSizes = new double[tableForm.getColumns().length];
        for (TableForm.Row row : tableForm.getRows()) {
            int i = 0;
            for (Field field : row) {
                int size = StringUtils.length(field.getStringValue());
                double relativeSize = ((double) size) / tableForm.getRows().length;
                columnSizes[i++] += relativeSize;
            }
        }

        double totalSize = 0;
        for (int i = 0; i < columnSizes.length; i++) {
            double effectiveSize = Math.max(columnSizes[i], headerSizes[i]);
            columnSizes[i] = effectiveSize;
            totalSize += effectiveSize;
        }
        while (totalSize > 75) {
            int maxIndex = 0;
            double max = 0;
            for (int i = 0; i < columnSizes.length; i++) {
                if (columnSizes[i] > max) {
                    max = columnSizes[i];
                    maxIndex = i;
                }
            }
            columnSizes[maxIndex] -= 1;
            totalSize -= 1;
        }
        while (totalSize < 70) {
            int minIndex = 0;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < columnSizes.length; i++) {
                if (columnSizes[i] < min) {
                    min = columnSizes[i];
                    minIndex = i;
                }
            }
            columnSizes[minIndex] += 1;
            totalSize += 1;
        }
        return columnSizes;
    }

    // **************************************************************************
    // ExportRead
    // **************************************************************************

    /**
     * Composes an XML document representing the current object.
     *
     * @return
     * @throws IOException
     */
    protected Reader composeXmlPort() throws IOException, WriteException {
        setupSearchForm();

        loadObjects();

        setupTableForm(Mode.VIEW);
        setupForm(Mode.VIEW);
        form.readFromObject(object);

        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        xb.write(crudConfiguration.getReadTitle());
        xb.closeElement("table");

        for (FieldSet fieldset : form) {
            xb.openElement("tableData");
            xb.openElement("rows");

            for (Field field : fieldset.fields()) {
                xb.openElement("row");
                xb.openElement("nameColumn");
                xb.write(field.getLabel());
                xb.closeElement("nameColumn");

                xb.openElement("value");
                xb.write(field.getStringValue());
                xb.closeElement("value");
                xb.closeElement("row");

            }
            xb.closeElement("rows");
            xb.closeElement("tableData");
        }

        xb.closeElement("class");

        return new StringReader(xb.toString());
    }

    public void exportReadPdf(File tempPdfFile) throws FOPException, IOException, TransformerException {
        setupSearchForm();

        loadObjects(true);

        setupTableForm(Mode.VIEW);

        FopFactory fopFactory = FopFactory.newInstance();

        // hongliangpan add
        try {
            String fopConfig = "fop.xml";
            String filePath = CrudAction.class.getClassLoader().getResource(fopConfig).getPath();
            fopFactory.setUserConfig(filePath);
            // String fonts = "/fonts"; 的上级目录
            fopFactory.setFontBaseURL(new File(filePath).getParent() + "/fonts");
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        }

        FileOutputStream out = null;
        InputStream xsltStream = null;
        try {
            out = new FileOutputStream(tempPdfFile);

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            xsltStream = getXsltStream(TEMPLATE_FOP_READ);

            // Setup XSLT
            TransformerFactory Factory = TransformerFactory.newInstance();
            Transformer transformer = Factory.newTransformer(new StreamSource(xsltStream));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            Reader reader = composeXmlPort();
            Source src = new StreamSource(reader);

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            reader.close();
            out.flush();
        } catch (Exception e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            IOUtils.closeQuietly(xsltStream);
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }

    @Button(list = "crud-read", key = "commons.exportPdf", order = 8)
    public Resolution exportPdf() {
        try {
            String fileName = ExportJxlUtils.getFileName(crudConfiguration, "pdf");
            final File tmpFile = File.createTempFile("export." + crudConfiguration.getName(), ".read.pdf");
            exportReadPdf(tmpFile);
            FileInputStream fileInputStream = new FileInputStream(tmpFile);
            return new StreamingResolution("application/pdf", fileInputStream) {
                @Override
                protected void stream(HttpServletResponse response) throws Exception {
                    super.stream(response);
                    if (!tmpFile.delete()) {
                        logger.warn("Temporary file {} could not be deleted", tmpFile.getAbsolutePath());
                    }
                }
            }.setFilename(fileName);
        } catch (Exception e) {
            logger.error("PDF export failed", e);
            SessionMessages.addErrorMessage(getMessage("commons.export.failed"));
            // return new RedirectResolution(getDispatch().getOriginalPath());
            return new RedirectResolution(getReturnUrl());

        }
    }

    /**
     * <p>Returns a string corresponding to a key in the resource bundle for the request locale.</p>
     * <p>The string can contain placeholders (see the {@link java.text.MessageFormat} class for details) that will
     * be substituted with values from the <code>args</code> array.</p>
     *
     * @param key  the key to search in the resource bundle.
     * @param args the arguments to be interpolated in the message string.
     *             please use ElementsThreadLocals.getText instead.
     */
    public String getMessage(String key, Object... args) {
        return ElementsThreadLocals.getText(key, args);
    }

    protected void setIsEdit(CrudProperty prop, boolean isAdmin, String field) {
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
}
