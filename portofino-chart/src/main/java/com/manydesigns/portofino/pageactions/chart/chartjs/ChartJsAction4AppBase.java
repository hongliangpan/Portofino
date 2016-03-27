package com.manydesigns.portofino.pageactions.chart.chartjs;

import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.chart.jfreechart.JFreeChartAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * sql 配置规则<br>
 * sql 通过页面配置<br>
 * 1=1 替换成 1=1 AND 区域过滤
 */
public class ChartJsAction4AppBase extends ChartJsAction {

    @Button(list = "pageHeaderButtons", titleKey = "dataList", order = 1, icon = Button.ICON_PLUS)
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution dataList() {
        if (this.chartConfiguration != null) {
            sqlCache();
        }
        super.execute();
        return new ForwardResolution("/m/chart/chartjs/data_list.jsp");
    }

    @DefaultHandler
    public Resolution execute() {
        context.getResponse().setContentType("text/html;charset=UTF-8");
        if (this.chartConfiguration != null) {
            sqlCache();
        }
        return super.execute();
    }

    public void sqlCache() {
        String sql = chartConfiguration.getQueryOriginal();
        String query = formatSql(sql);
        chartConfiguration.setQuery(query);
        context.getRequest().setAttribute("sql", query);
        //chartConfiguration.setLegend(" ");
    }

    public String formatSql(String sqlTemplate) {
        return sqlTemplate;
    }

    public String replace(String sql) {
        if (sql.indexOf("1=1") < 0 && sql.indexOf("1 = 1") < 0) {
            return sql;
        }
        return sql.replaceAll("1=1", "{0}").replaceAll("1 = 1", "{0}");
    }
}
