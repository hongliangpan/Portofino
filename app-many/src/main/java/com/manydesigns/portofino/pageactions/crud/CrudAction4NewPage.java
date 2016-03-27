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

import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.annotations.SupportsDetail;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;

/**
 * hongliangpan add this class
 * <p/>
 * 添加页面，非向导方式
 * <p/>
 */
@SupportsPermissions({CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT,
        CrudAction.PERMISSION_DELETE})
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template_new_page.groovy")
@ConfigurationClass(CrudConfiguration.class)
@SupportsDetail
@PageActionName("Crud4NewPage")
public class CrudAction4NewPage extends CrudAction4AppBase {
    @Override
    protected boolean saveConfiguration(Object configuration) {
        //TODO 如果是第一次 修改配置，如没有lable
        boolean result = super.saveConfiguration(configuration);
        //hongliangpan add
        createQueryPage();
        return result;
    }

    //hongliangpan add
    public void createQueryPage() {
        CrudConfiguration configuration = (CrudConfiguration) crudConfiguration;


        ModelHelper.syncTable(configuration);

        ModelHelper.createCrudPageAndRemark(pageInstance, configuration, "CrudAction4NewPage.groovy");
    }



}
