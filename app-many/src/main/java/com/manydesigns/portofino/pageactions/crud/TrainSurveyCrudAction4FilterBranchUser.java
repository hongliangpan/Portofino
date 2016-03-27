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

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.manydesigns.elements.Mode;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.annotations.SupportsDetail;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;

import java.util.HashMap;
import java.util.List;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * <p/>
 */
@SupportsPermissions({CrudAction4FilterProductUser.PERMISSION_CREATE, CrudAction4FilterProductUser.PERMISSION_EDIT,
        CrudAction4FilterProductUser.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template_new_page.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud")
public class TrainSurveyCrudAction4FilterBranchUser extends CrudAction4FilterBranchUser {

    protected void doDelete(Object object) {
        try {
            this.session.delete(this.baseTable.getActualEntityName(), object);
        } catch (Exception e) {
            processException(e);
        }
    }

    protected void createSetup(Object object) {
        HashMap map = (HashMap) object;
        map.put("c_branch_id", getLoginUserBranchId());
        map.put("c_servicer", getLoginUserId());

        setFieldLongValueByParam(map, "c_train_id", "0");

        setFieldLongValueByParam(map, "c_user_id", "0");
        setFieldValueByParam(map, "c_phone", "");
        setFieldValueByParam(map, "c_user_name", "");
        setFieldValueByParam(map, "c_qq", "");
        setFieldValueByParam(map, "c_product", "gfyc");
    }

    private void setFieldValueByParam(HashMap map, String field, String defaultValue) {
        String fieldValue = context.getRequest().getParameter(field);
        if (Strings.isNullOrEmpty(fieldValue)) {
            fieldValue = defaultValue;
        }
        map.put(field, fieldValue);
    }

    private void setFieldLongValueByParam(HashMap map, String field, String defaultValue) {
        String fieldValue = context.getRequest().getParameter(field);
        if (Strings.isNullOrEmpty(fieldValue)) {
            fieldValue = defaultValue;
        }
        map.put(field, Long.parseLong(fieldValue));
    }

    protected void editSetup(Object object) {
        HashMap map = (HashMap) object;
        setDefaultValue(map, "c_branch_id", getLoginUserBranchId());
        setDefaultValue(map, "c_servicer", getLoginUserId());
        String product = context.getRequest().getParameter("c_product");
        if (Strings.isNullOrEmpty(product)) {
            product = "gfyc";
        }
        setDefaultValue(map, "c_product", product);
    }

    protected void preCreate() {
        super.preCreate();
        boolean isAdmin = isAdmin();
        isAdmin = false;
        String userId = context.getRequest().getParameter("c_user_id");
        boolean notHaveUserId = Strings.isNullOrEmpty(userId) ||isAdmin;

        List<CrudProperty> properties = getCrudConfiguration().getProperties();
        for (CrudProperty prop : properties) {
            setIsEdit(prop, notHaveUserId, "c_branch_id");
            setIsEdit(prop, notHaveUserId, "c_user_id");
            setIsEdit(prop, notHaveUserId, "c_user_name");
            setIsEdit(prop, notHaveUserId, "c_phone");
            setIsEdit(prop, notHaveUserId, "c_qq");
        }

        this.setupForm(Mode.CREATE);
        this.object = this.classAccessor.newInstance();
        this.createSetup(this.object);
        this.form.readFromObject(this.object);
    }
}
