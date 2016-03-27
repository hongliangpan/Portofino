package com.manydesigns.portofino.pageactions.crud;

import com.glodon.app.base.util.DateTimeUtils;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.manydesigns.elements.fields.DateField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.NumericField;
import com.manydesigns.elements.fields.PasswordField;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.SearchForm;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.utils.ContextUtils;
import com.manydesigns.portofino.utils.DateUtils;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hongliangpan@gmail.com on 2015/12/26.
 */
public class ExportJxlUtils {
    public static final Logger logger = LoggerFactory.getLogger(ExportJxlUtils.class);
    public static final int TITLE_ROW_SIEZ = 3;

    public static void writeFileReadExcel(OutputStream outputStream,
                                          CrudConfiguration crudConfiguration,
                                          TableForm tableForm, Form form,SearchForm searchForm) throws IOException, WriteException {
        WritableWorkbook workbook = null;
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setUseTemporaryFileDuringWrite(false);
            workbook = Workbook.createWorkbook(outputStream, workbookSettings);
            WritableSheet sheet = workbook.createSheet(crudConfiguration.getReadTitle(), workbook.getNumberOfSheets());

            setHeader(sheet,tableForm, searchForm);

            int i = 1;
            for (FieldSet fieldset : form) {
                int j = 0;
                for (Field field : fieldset.fields()) {
                    addFieldToCell(sheet, i + TITLE_ROW_SIEZ, j, field);
                    j++;
                }
                i++;
            }
            workbook.write();
        } catch (IOException e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            logger.warn("RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            logger.warn("WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            } catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }

    public static void writeFileSearchExcel(OutputStream outputStream, CrudConfiguration crudConfiguration,
                                            TableForm tableForm, Form form,SearchForm searchForm) {
        WritableWorkbook workbook = null;
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setUseTemporaryFileDuringWrite(false);
            workbook = Workbook.createWorkbook(outputStream, workbookSettings);
            String title = crudConfiguration.getSearchTitle();
            if (StringUtils.isBlank(title)) {
                title = "export";
            }

            WritableSheet sheet = workbook.createSheet(title, 0);
            setHeader( sheet,tableForm,searchForm);

            int i = 1;
            for (TableForm.Row row : tableForm.getRows()) {
                exportRows(sheet, i, row);
                i++;
            }

            workbook.write();
        } catch (IOException e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            logger.warn("RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            logger.warn("WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            } catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }

    private static void setCellWidth(WritableSheet sheet, TableForm tableForm) throws WriteException {
        int columnWidth[] = new int[sheet.getColumns()];
        for (TableForm.Row row : tableForm.getRows()) {
            for (int j = 0; j < row.size(); j++) {
                Field field = row.get(j);
                int width;
                if (Strings.isNullOrEmpty(field.getStringValue())) {
                    width = field.getLabel().length() + getChineseNum(field.getLabel());
                } else {
                    width = field.getStringValue().length() + getChineseNum(field.getStringValue());    ///汉字占2个单位长度
                }
                if (columnWidth[j] < width) {  ///求取到目前为止的最佳列宽
                    columnWidth[j] = width;
                }
            }
        }

        for (int i = 0; i < columnWidth.length; i++) {    ///设置每列宽
            sheet.setColumnView(i, columnWidth[i] + 2);
        }

    }

    private static void setHeader(WritableSheet sheet,TableForm tableForm, SearchForm searchForm) throws WriteException {
        // 整个sheet中的网格线 hongliangpan add
        sheet.getSettings().setShowGridLines(true);

        addHeaderToSearchSheet(sheet, tableForm);

        /*CellView cellView = new CellView();
        cellView.setAutosize(true); //设置自动大小
        for (int i = 0; i < sheet.getColumns(); i++) {
            sheet.setColumnView(i, cellView);//根据内容自动设置列宽
        }*/
        setCellWidth(sheet, tableForm);
    }

    public static void addHeaderToSearchSheet(WritableSheet sheet, TableForm tableForm) throws WriteException {
        setTitle(sheet, tableForm);

        WritableCellFormat formatCell = getHeaderCellStyle();
        int l = 0;
        for (TableForm.Column col : tableForm.getColumns()) {
            sheet.addCell(new jxl.write.Label(l, TITLE_ROW_SIEZ, col.getLabel(), formatCell));
            l++;
        }
    }

    private static void setTitle(WritableSheet sheet, TableForm tableForm) throws WriteException {
        Label label = new Label(0, 0, sheet.getName().replace("搜索", ""));
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 20,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE);
        WritableCellFormat format = new WritableCellFormat(font);
        format.setAlignment(Alignment.CENTRE);
        format.setBorder(Border.NONE, BorderLineStyle.THIN, Colour.BLACK);
        label.setCellFormat(format);
        sheet.addCell(label);
        int columnLength = tableForm.getColumns().length;
        sheet.mergeCells(0, 0, columnLength -1, 0);

        String secondTitle = "行数:" + tableForm.getRows().length + ",时间:" + DateTimeUtils.datetimeToString(new Date());
        String user = ContextUtils.getLoginUser();
        if (!Strings.isNullOrEmpty(user)) {
            user = "用户:" + user + ",";
            secondTitle = user + secondTitle;
        }
        label = new Label(0, 1, secondTitle);

        font = new WritableFont(WritableFont.createFont("宋体"), 15,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE);
        format = new WritableCellFormat(font);
        format.setAlignment(Alignment.RIGHT);
        format.setBorder(Border.NONE, BorderLineStyle.THIN, Colour.BLACK);
        label.setCellFormat(format);
        sheet.addCell(label);
        sheet.mergeCells(0, 1, columnLength -1, 1);
        sheet.mergeCells(0, 2, columnLength -1, 2);
    }


    private static void exportRows(WritableSheet sheet, int i, TableForm.Row row) throws WriteException {
        int j = 0;
        for (Field field : row) {
            addFieldToCell(sheet, i + TITLE_ROW_SIEZ, j, field);
            j++;
        }
    }

    private static void addHeaderToReadSheet(WritableSheet sheet, Form form) throws WriteException {
        WritableCellFormat formatCell = getHeaderCellStyle();
        int i = 0;
        for (FieldSet fieldset : form) {
            for (Field field : fieldset.fields()) {
                sheet.addCell(new jxl.write.Label(i, 0, field.getLabel(), formatCell));
                i++;
            }
        }
    }


    private static void addFieldToCell(WritableSheet sheet, int i, int j, Field field) throws WriteException {
        if (field instanceof NumericField) {
            NumericField numField = (NumericField) field;
            if (numField.getValue() != null) {
                Number number;
                BigDecimal decimalValue = numField.getValue();
                if (numField.getDecimalFormat() == null) {
                    number = new Number(j, i, decimalValue == null ? null : decimalValue.doubleValue());
                } else {

                    if (numField.getScale() == 0 || !numField.getStringValue().contains(".")) {
                        NumberFormat numberFormat = new NumberFormat("####");
                        WritableCellFormat cellFormat = new WritableCellFormat(numberFormat);
                        richCellFormat(cellFormat);
                        number = new Number(j, i, decimalValue == null ? null : decimalValue.longValue(),
                                cellFormat);
                    } else {
                        NumberFormat numberFormat = new NumberFormat(numField.getDecimalFormat().toPattern());
                        WritableCellFormat cellFormat = new WritableCellFormat(numberFormat);
                        richCellFormat(cellFormat);
                        number = new Number(j, i, decimalValue == null ? null : decimalValue.doubleValue(),
                                cellFormat);
                    }
                }
                sheet.addCell(number);
            }
        } else if (field instanceof PasswordField) {
            jxl.write.Label label = new jxl.write.Label(j, i, PasswordField.PASSWORD_PLACEHOLDER);
            WritableCellFormat cellFormat = new WritableCellFormat();
            richCellFormat(cellFormat);
            label.setCellFormat(cellFormat);
            sheet.addCell(label);
        } else if (field instanceof DateField) {
            DateField dateField = (DateField) field;
            DateTime dateCell;
            Date date = dateField.getValue();
            if (date != null) {
                DateFormat dateFormat = new DateFormat(dateField.getDatePattern());
                WritableCellFormat cellFormat = new WritableCellFormat(dateFormat);
                richCellFormat(cellFormat);
                dateCell = new DateTime(j, i, dateField.getValue() == null ? null : dateField.getValue(), cellFormat);
                sheet.addCell(dateCell);
            }
        } else {
            jxl.write.Label label = new jxl.write.Label(j, i, field.getStringValue());
            WritableCellFormat cellFormat = new WritableCellFormat();
            richCellFormat(cellFormat);
            label.setCellFormat(cellFormat);
            sheet.addCell(label);
        }

    }

    public static WritableCellFormat richCellFormat(WritableCellFormat format) {
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 10,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE);
        format.setFont(font);
        try {
            // 设置单元格背景色：表体为白色
            //format.setBackground(Colour.WHITE);
            // 设置表头表格边框样式
            // 整个表格线为细线、黑色
            format.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        } catch (WriteException e) {
            logger.error("表体单元格样式设置失败！", e);
        }
        return format;
    }

    public static WritableCellFormat getHeaderCellStyle() {
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 12,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE);
        WritableCellFormat headerFormat = new WritableCellFormat(
                NumberFormats.TEXT);
        try {
            // 添加字体设置
            headerFormat.setFont(font);
            // 设置单元格背景色：表头为黄色
            headerFormat.setBackground(Colour.LIGHT_ORANGE);
            // 设置表头表格边框样式
            // 整个表格线为细线、黑色
            headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
            // 表头内容水平居中显示
            headerFormat.setAlignment(Alignment.CENTRE);
        } catch (WriteException e) {
            logger.error("表头单元格样式设置失败！", e);
        }
        return headerFormat;
    }


    public static String getFileName(CrudConfiguration crudConfiguration) throws UnsupportedEncodingException {
        return getFileName(crudConfiguration,"xls");
    }

    public static String getFileName(CrudConfiguration crudConfiguration,String type) throws UnsupportedEncodingException {
        // crudConfiguration.getSearchTitle() 中文处理或用英文
        String fileName = crudConfiguration.getSearchTitle().replaceAll("搜索","") + "_" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        fileName = new String(fileName.getBytes(), "ISO8859-1") + "."+type;
        return fileName;
    }
    public static WritableCellFormat getBodyCellStyle() {
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 10,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE);
        WritableCellFormat bodyFormat = new WritableCellFormat(font);
        try {
            // 设置单元格背景色：表体为白色
            bodyFormat.setBackground(Colour.WHITE);
            // 设置表头表格边框样式
            // 整个表格线为细线、黑色
            bodyFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        } catch (WriteException e) {
            logger.error("表体单元格样式设置失败！", e);
        }
        return bodyFormat;
    }

    public static int getChineseNum(String context) {    ///统计context中是汉字的个数
        int lenOfChinese = 0;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");    //汉字的Unicode编码范围
        Matcher m = p.matcher(context);
        while (m.find()) {
            lenOfChinese++;
        }
        return lenOfChinese;
    }
}
