/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

package com.manydesigns.portofino.actions.admin.tables.forms;

import com.manydesigns.elements.annotations.FieldSize;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.portofino.model.database.DatabaseSelectionProvider;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSelectionProviderForm extends DatabaseSelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected String columns;

    public DatabaseSelectionProviderForm(DatabaseSelectionProvider copyFrom) {
        try {
            BeanUtils.copyProperties(this, copyFrom);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public DatabaseSelectionProvider copyTo(DatabaseSelectionProvider dsp) {
        try {
            BeanUtils.copyProperties(dsp, this);
        } catch (Exception e) {
            throw new Error(e);
        }
        return dsp;
    }

    @Override
    @Required
    @Updatable(false)
    public String getName() {
        return super.getName();
    }

    @Override
    @Required
    public String getToDatabase() {
        return super.getToDatabase();
    }

    @Override
    @Multiline
    public String getHql() {
        return super.getHql();
    }

    @Override
    @Multiline
    public String getSql() {
        return super.getSql();
    }

    @FieldSize(75)
    @Required
    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }
}