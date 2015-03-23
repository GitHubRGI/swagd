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
package com.rgi.view;

import javax.swing.JRadioButton;

import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Jenifer Cochran
 *
 */
public class TileStoreRadioButton extends JRadioButton
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TileStoreReader store;

    /**
     * Creates a RadioButton that has the TileStoreReader information attached to it
     * @param store The TileStoreReader that with the tiles associated with this button
     */
    public TileStoreRadioButton(final TileStoreReader store)
    {
        super(store.getName());
        this.store  = store;
    }

    /**
     * @return the TileStoreReader that reads the tiles with this button
     */
    public TileStoreReader getTileStore()
    {
        return this.store;
    }
}
