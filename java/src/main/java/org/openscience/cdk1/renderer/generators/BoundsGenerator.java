/* Copyright (C) 2009  Gilleain Torrance <gilleain@users.sf.net>
 *               2009  Stefan Kuhn <shk3@users.sf.net>
 *
 * Contact: cdk-devel@list.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk1.renderer.generators;


import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk1.annotations.TestClass;
import org.openscience.cdk1.annotations.TestMethod;
import org.openscience.cdk1.interfaces.IMoleculeSet;
import org.openscience.cdk1.interfaces.IReaction;
import org.openscience.cdk1.renderer.BoundsCalculator;
import org.openscience.cdk1.renderer.RendererModel;
import org.openscience.cdk1.renderer.elements.ElementGroup;
import org.openscience.cdk1.renderer.elements.IRenderingElement;
import org.openscience.cdk1.renderer.elements.RectangleElement;
import org.openscience.cdk1.renderer.generators.parameter.AbstractGeneratorParameter;


/**
 * Produce a bounding rectangle for various chem objects.
 * 
 * @author maclean
 * @cdk.module renderextra
 * @cdk.githash
 */
@TestClass("org.openscience.cdk1.renderer.generators.BoundsGeneratorTest")
public class BoundsGenerator implements IGenerator<IReaction> {

	/**
	 * The color of the box drawn at the bounds of a
	 * molecule, molecule set, or reaction.
	 */
    public static class BoundsColor extends
    AbstractGeneratorParameter<Color> {
    	/** {@inheritDoc}} */
        public Color getDefault() {
            return Color.LIGHT_GRAY;
        }
    }
    private IGeneratorParameter<Color> boundsColor = new BoundsColor();

    public BoundsGenerator() {}

	/** {@inheritDoc}} */
	@Override
    @TestMethod("testEmptyReaction")
    public IRenderingElement generate(IReaction reaction, RendererModel model) {
        ElementGroup elementGroup = new ElementGroup();
        IMoleculeSet reactants = reaction.getReactants();
        if (reactants != null) {
            elementGroup.add(this.generate(reactants));
        }
        
        IMoleculeSet products = reaction.getProducts();
        if (products != null) {
            elementGroup.add(this.generate(products));
        }
        
        return elementGroup;
    }

    private IRenderingElement generate(
            IMoleculeSet moleculeSet) {
        Rectangle2D totalBounds = BoundsCalculator.calculateBounds(moleculeSet);
        
        return new RectangleElement(totalBounds.getMinX(),
                                    totalBounds.getMinY(),
                                    totalBounds.getMaxX(),
                                    totalBounds.getMaxY(),
                                    boundsColor.getValue());
    }

	/** {@inheritDoc}} */
	@Override
	@TestMethod("testGetParameters")
	public List<IGeneratorParameter<?>> getParameters() {
        return Arrays.asList(
            new IGeneratorParameter<?>[] {
            	boundsColor
            }
        );
    }
}
