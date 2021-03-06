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

package com.manydesigns.portofino.chart;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.*;

import java.awt.*;
import java.util.Locale;
/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ChartBarGenerator extends Chart2DGenerator {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected JFreeChart createChart(ChartDefinition chartDefinition,
                                     CategoryDataset dataset, PlotOrientation plotOrientation) {
        return ChartFactory.createBarChart(
                chartDefinition.getName(), chartDefinition.getXAxisName(), chartDefinition.getYAxisName(), dataset,
                plotOrientation, true, true, true);
    }

    // hongliangpan override
    public JFreeChart generate(ChartDefinition chartDefinition, Persistence persistence, Locale locale) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        java.util.List<Object[]> result;
        String query = chartDefinition.getQuery();
        logger.info(query);
        Session session = persistence.getSession(chartDefinition.getDatabase());
        result = QueryUtils.runSql(session, query);
        for (Object[] current : result) {
            ComparableWrapper x = new ComparableWrapper((Comparable) current[0]);
            ComparableWrapper y = new ComparableWrapper((Comparable) current[1]);
            if (current.length > 3) {
                x.setLabel(current[3].toString());
            }
            if (current.length > 4) {
                y.setLabel(current[4].toString());
            }
            dataset.setValue((Number) current[2], x, y);
        }

        PlotOrientation plotOrientation = PlotOrientation.HORIZONTAL;
        if (chartDefinition.getActualOrientation() == ChartDefinition.Orientation.VERTICAL) {
            plotOrientation = PlotOrientation.VERTICAL;
        }

        JFreeChart chart = createChart(chartDefinition, dataset, plotOrientation);

        chart.setAntiAlias(antiAlias);

        // impostiamo il bordo invisibile
        // eventualmente e' il css a fornirne uno
        chart.setBorderVisible(borderVisible);

        // impostiamo il titolo
        TextTitle title = chart.getTitle();
        title.setFont(titleFont);
        title.setMargin(10.0, 0.0, 0.0, 0.0);

        // ottieni il Plot
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        CategoryItemRenderer renderer = plot.getRenderer();
        String urlExpression = chartDefinition.getUrlExpression();
        if (!StringUtils.isBlank(urlExpression)) {
            CategoryURLGenerator urlGenerator = new ChartBarUrlGenerator(chartDefinition.getUrlExpression());
            renderer.setBaseItemURLGenerator(urlGenerator);
        } else {
            renderer.setBaseItemURLGenerator(null);
        }
        renderer.setBaseOutlinePaint(Color.BLACK);

        // ///////////////
        if (renderer instanceof BarRenderer || renderer instanceof BarRenderer3D) {
            BarRenderer barRenderer = (BarRenderer) renderer;

            barRenderer.setDrawBarOutline(true);
            barRenderer.setShadowVisible(false);
            barRenderer.setBarPainter(new StandardBarPainter());

            // hongliangpan add
            barRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            barRenderer.setBaseItemLabelsVisible(true);
            // 显示每个柱的数值，并修改该数值的字体属性 setSeriesItemLabelGenerator
            barRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            // 设置bar的最小宽度，以保证能显示数值
            barRenderer.setMinimumBarLength(0.02);
            // 最大宽度
            barRenderer.setMaximumBarWidth(0.05);
            // 设置不能在柱子上正常显示的那些数值的显示方式，将这些数值显示在柱子外面
            // 设置柱子上显示的数据旋转90度,最后一个参数为旋转的角度值/3.14
            ItemLabelPosition itemLabelPositionFallback = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
                    TextAnchor.BASELINE_LEFT, TextAnchor.HALF_ASCENT_LEFT, -1.57D);
            // 设置不能正常显示的柱子label的position
            // barRenderer.setPositiveItemLabelPositionFallback(itemLabelPositionFallback);
            // barRenderer.setNegativeItemLabelPositionFallback(itemLabelPositionFallback);

            barRenderer.setItemLabelsVisible(true);

            // 设置柱子边框颜色
            barRenderer.setBaseOutlinePaint(Color.BLACK);
            // 设置柱子边框可见
            barRenderer.setDrawBarOutline(true);

            // 设置每个地区所包含的平行柱的之间距离
            barRenderer.setItemMargin(0.0);

            // 显示每个柱的数值，并修改该数值的字体属性
            barRenderer.setIncludeBaseInRange(true);
            barRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            barRenderer.setBaseItemLabelsVisible(true);
            // 设置柱的透明度
            plot.setForegroundAlpha(1.0f);
        }

        // ///////////////

        // il plot ha sfondo e bordo trasparente
        // (quindi si vede il colore del chart)
        plot.setBackgroundPaint(transparentColor);
        plot.setOutlinePaint(transparentColor);

        // Modifico il toolTip
        // plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} = {1} ({2})"));

        // imposta il messaggio se non ci sono dati
        plot.setNoDataMessage(ElementsThreadLocals.getText("no.data.available"));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));

        // Category axis
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setAxisLinePaint(Color.BLACK);
        categoryAxis.setLabelFont(axisFont);
        categoryAxis.setAxisLineVisible(true);
        categoryAxis.setTickLabelFont(new Font("宋体",Font.BOLD,10));//设置x轴坐标上的字体
        //categoryAxis.setLabelFont(new Font("宋体",Font.BOLD,20));//设置x轴上的标题的字体
        // impostiamo la rotazione dell'etichetta
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            CategoryLabelPosition pos = new CategoryLabelPosition(RectangleAnchor.TOP_LEFT, TextBlockAnchor.TOP_RIGHT,
                    TextAnchor.TOP_RIGHT, -Math.PI / 4.0, CategoryLabelWidthType.CATEGORY, 100);
            CategoryLabelPositions positions = new CategoryLabelPositions(pos, pos, pos, pos);
            categoryAxis.setCategoryLabelPositions(positions);
            categoryAxis.setMaximumCategoryLabelWidthRatio(6.0f);
            height = 333;
        } else {
            categoryAxis.setMaximumCategoryLabelWidthRatio(0.4f);

            // recuperiamo 8 pixel a sinistra
            plot.setInsets(new RectangleInsets(4.0, 0.0, 4.0, 8.0));

            height = 74;

            // contiamo gli elementi nel dataset
            height += 23 * dataset.getColumnCount();

            height += 57;
        }

        Axis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAxisLinePaint(Color.BLACK);
        rangeAxis.setLabelFont(axisFont);

        DrawingSupplier supplier = new DesaturatedDrawingSupplier(plot.getDrawingSupplier());
        plot.setDrawingSupplier(supplier);

        // impostiamo il titolo della legenda
        String legendString = chartDefinition.getLegend();
        Title subtitle = new TextTitle(legendString, legendFont, Color.BLACK, RectangleEdge.BOTTOM,
                HorizontalAlignment.CENTER, VerticalAlignment.CENTER, new RectangleInsets(0, 0, 0, 0));
        subtitle.setMargin(0, 0, 5, 0);
        chart.addSubtitle(subtitle);

        // impostiamo la legenda
        LegendTitle legend = chart.getLegend();
        legend.setBorder(0, 0, 0, 0);
        legend.setItemFont(legendItemFont);
        int legendMargin = 10;
        legend.setMargin(0.0, legendMargin, legendMargin, legendMargin);
        legend.setBackgroundPaint(transparentColor);

        // impostiamo un gradiente orizzontale
        Paint chartBgPaint = new GradientPaint(0, 0, new Color(255, 253, 240), 0, height, Color.WHITE);
        chart.setBackgroundPaint(chartBgPaint);
        return chart;
    }

}
