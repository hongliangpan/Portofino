<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ReloadModelAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.reload-model.title"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="reload-model-bar" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletHeader">
        <h4><fmt:message key="layouts.admin.reload-model.title"/></h4>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <p>
            <fmt:message key="layouts.admin.reload-model.text"/>
            <portofino:buttons list="reload-model" cssClass="portletButton" />
        </p>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="reload-model-bar" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>