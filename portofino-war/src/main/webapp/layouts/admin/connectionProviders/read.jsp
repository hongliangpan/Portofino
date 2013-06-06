<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        Connection provider: <c:out value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="connectionProviders-read" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletHeader">
        <h4>Connection provider: <c:out value="${actionBean.databaseName}"/></h4>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
        <c:if test="${not empty actionBean.connectionProvider.database.schemas}">
            <div class="horizontalSeparator"></div>
            <h4><fmt:message key="layouts.admin.connectionProviders.read.schemas"/></h4>
            <c:forEach var="schema" items="${actionBean.connectionProvider.database.schemas}"
                       varStatus="status">
                <c:out value="${schema.schemaName}" /><c:if test="${!status.last}">, </c:if>
            </c:forEach>
        </c:if>
        <c:if test="${actionBean.detectedValuesForm != null}">
            <style type="text/css">
                #detectedValuesForm label {
                    width: 200px;
                }
                #detectedValuesForm .controls {
                    margin-left: 220px;
                }
            </style>
            <div class="horizontalSeparator"></div>
            <h4><fmt:message key="layouts.admin.connectionProviders.read.detected_values"/></h4>
            <div id="detectedValuesForm">
                <mde:write name="actionBean" property="detectedValuesForm"/>
            </div>
        </c:if>
        <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="connectionProviders-read" cssClass="contentButton" />
        <script type="text/javascript">
            $("button[name=delete]").click(function() {
                return confirm ('<fmt:message key="commons.confirm" />');
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>