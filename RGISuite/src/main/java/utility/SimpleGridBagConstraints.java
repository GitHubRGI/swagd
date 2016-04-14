/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package utility;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Convenience class wrapping a basic {@link GridBagConstraints} with common
 * values
 *
 * @author Luke Lambert
 *
 */
public class SimpleGridBagConstraints extends GridBagConstraints
{
    private static final long serialVersionUID = -8883148851290993101L;

    /**
     * Constructor
     *
     * @param gridX
     *             Grid column value
     * @param gridY
     *             Grid row value
     * @param stretch
     *             If true, the control is stretched to take up remaining space on its row
     */
    public SimpleGridBagConstraints(final int gridX, final int gridY, final boolean stretch)
    {
        super(gridX, gridY, 1, 1, stretch ? 1 : 0, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
    }
}
