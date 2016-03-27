import com.manydesigns.portofino.security.*
import net.sourceforge.stripes.action.*
import com.manydesigns.portofino.pageactions.crud.*

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction4AppBase {
    //hongliangpan modity to CrudAction4Appbase
    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    protected void createSetup(Object object) {}

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

    //**************************************************************************
    // View hooks
    //**************************************************************************

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

    protected Resolution getEmbeddedReadView() {
        return super.getEmbeddedReadView()
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

    protected Resolution getConfigurationView() {
        return super.getConfigurationView()
    }

    protected Resolution getSuccessfulSaveView() {
        return super.getSuccessfulSaveView()
    }

    protected Resolution getSuccessfulUpdateView() {
        return super.getSuccessfulUpdateView()
    }

    protected Resolution getSuccessfulDeleteView() {
        return super.getSuccessfulDeleteView()
    }


}