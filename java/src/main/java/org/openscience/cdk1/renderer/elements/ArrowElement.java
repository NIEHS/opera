/* Copyright (C) 2009  Stefan Kuhn <shk3@users.sf.net>
 *               2011  Egon Willighagen <egonw@users.sf.net>
 *  
 * Contact: cdk-devel@list.sourceforge.net
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk1.renderer.elements;

import java.awt.Color;

import org.openscience.cdk1.annotations.TestClass;
import org.openscience.cdk1.annotations.TestMethod;

/**
 * {@link IRenderingElement} for linear arrows. 
 *
 * @cdk.module renderbasic
 */
@TestClass("org.openscience.cdk1.renderer.elements.ArrowElementTest")
public class ArrowElement implements IRenderingElement {

    public final double startX;
    public final double startY;
    public final double endX;
    public final double endY;
    public final double width;
    public final Color color;
    public final boolean direction;

    /**
     * Constructor for an arrow element, based on starting point, end point, width,
     * direction, and color.
     *
     * @param startX    X coordinate of the point where the arrow starts.
     * @param startY    Y coodrinate of the point where the arrow starts.
     * @param endX      X coordinate of the point where the arrow ends.
     * @param endY      Y coordinate of the point where the arrow ends.
     * @param width     width of the arrow line.
     * @param direction true is the arrow points from start to end, false if from end to start
     * @param color     {@link Color} of the arrow
     */
    public ArrowElement(double startX, double startY, double endX, double endY,
            double width, boolean direction, Color color) {
        this.endX = startX;
        this.endY = startY;
        this.startX = endX;
        this.startY = endY;
        this.width = width;
        this.color = color;
        this.direction = direction;
    }

    /** {@inheritDoc} */
    @TestMethod("testAccept")
    public void accept(IRenderingVisitor v) {
        v.visit(this);
    }
}
