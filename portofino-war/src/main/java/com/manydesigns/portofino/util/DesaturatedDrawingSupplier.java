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

package com.manydesigns.portofino.util;

import org.jfree.chart.plot.DrawingSupplier;

import java.awt.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DesaturatedDrawingSupplier implements DrawingSupplier {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private final DrawingSupplier inner;

    // desaturated
    private final Paint[] paintArray = {
        new Color(0xcc5252), // saturazione 60, luminosità 80
        new Color(0x52cc52),
        new Color(0x5252cc),

        new Color(0xd9d957), // saturazione 60, luminosità 85
        new Color(0x57d9d9),
        new Color(0xd957d9),

        new Color(0xd49455), // saturazione 60, luminosità 83
        new Color(0x94d455),
        new Color(0x55d494),

        new Color(0x5594d4), // saturazione 60, luminosità 83
        new Color(0x9455d4),
        new Color(0xd45594)
    };

    private int index;

    public DesaturatedDrawingSupplier(DrawingSupplier inner) {
        this.inner = inner;
        index = 0;
    }

    public Paint getNextPaint() {
        Paint result = paintArray[index++];
        if (index == paintArray.length) {
            index = 0;
        }
        return result;
    }

    public Paint getNextOutlinePaint() {
        return inner.getNextOutlinePaint();
    }

    public Paint getNextFillPaint() {
        return inner.getNextFillPaint();
    }

    public Stroke getNextStroke() {
        return inner.getNextStroke();
    }

    public Stroke getNextOutlineStroke() {
        return inner.getNextOutlineStroke();
    }

    public Shape getNextShape() {
        return inner.getNextShape();
    }

}
