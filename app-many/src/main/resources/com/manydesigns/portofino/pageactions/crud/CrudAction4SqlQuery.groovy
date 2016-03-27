package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.security.*
import net.sourceforge.stripes.action.*
import org.apache.commons.lang.StringUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class CrudPageBySql extends CrudAction4SqlQuery {

    String linkToParentProperty = "$linkToParentProperty";
    String parentName = "$parentName";

    protected void createSetup(Object object) {
        if(!StringUtils.isEmpty(parentName)) {
            object[linkToParentProperty] = ognlContext[parentName].$parentProperty
        }
    }

    protected boolean createValidate(Object object) {
        return true;
    }

    protected void createPostProcess(Object object) {}


    protected void editSetup(Object object) {}

    protected boolean editValidate(Object object) {
        return true;
    }

    protected void editPostProcess(Object object) {}


    protected boolean deleteValidate(Object object) {
        return true;
    }

    protected void deletePostProcess(Object object) {}


    protected Resolution getBulkEditView() {
        return super.getBulkEditView();
    }

    protected Resolution getCreateView() {
        return super.getCreateView();
    }

    protected Resolution getEditView() {
        return super.getEditView();
    }

    protected Resolution getReadView() {
        return super.getReadView();
    }

    protected Resolution getSearchView() {
        return super.getSearchView();
    }

    protected Resolution getEmbeddedSearchView() {
        return super.getEmbeddedSearchView();
    }

    protected Resolution getSearchResultsPageView() {
        return super.getSearchResultsPageView()
    }

}