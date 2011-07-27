/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.annotations.impl;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.fields.search.SelectSearchField;

import java.lang.annotation.Annotation;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@SuppressWarnings({"ClassExplicitlyAnnotation"})
public class SelectImpl implements Select {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private SelectField.DisplayMode displayMode;
    private SelectSearchField.DisplayMode searchDisplayMode;
    private String[] values;
    private String[] labels;

    public SelectImpl(SelectField.DisplayMode displayMode, SelectSearchField.DisplayMode searchDisplayMode,
                      String[] values, String[] labels) {
        this.displayMode = displayMode;
        this.searchDisplayMode=searchDisplayMode;
        this.values = values;
        this.labels = labels;
    }

    public SelectField.DisplayMode displayMode() {
        return displayMode;
    }

    public SelectSearchField.DisplayMode searchDisplayMode() {
        return searchDisplayMode;
    }

    public String[] values() {
        return values;
    }

    public String[] labels() {
        return labels;
    }

    public Class<? extends Annotation> annotationType() {
        return Select.class;
    }
}
