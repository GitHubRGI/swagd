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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JTree;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.TileStoreLoader;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;


//This is a work in progress do not rely on this code
@SuppressWarnings("javadoc")
public class RadioButtonTree extends JTree
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ButtonGroup mainGroup;
    private RadioButtonTree tree;
    private final JMapViewer map;
    private final List<TileStoreRadioButton> buttonList = new ArrayList<>();

    public RadioButtonTree(final List<TileStoreReader> stores, final JMapViewer map)
    {
        this.map = map;
        this.mainGroup = new ButtonGroup();

       this.addGroup(stores);
    }

    public void addLayer(final TileStoreReader store)
    {
        final TileStoreRadioButton button = new TileStoreRadioButton(store);
        this.mainGroup.add(button);
        button.addActionListener(this.createActionListener());
        this.buttonList.add(button);
    }

    public void addGroup(final List<TileStoreReader> tileStores)
    {
        tileStores.stream().forEach(store -> {
                                                final TileStoreRadioButton button = new TileStoreRadioButton(store);
                                                this.mainGroup.add(button);
                                                this.buttonList.add(button);

                                                button.addActionListener(this.createActionListener());
                                             });


    }

    public RadioButtonTree getTree()
    {
        return this.tree;
    }

    public ButtonGroup getGroup()
    {
        return this.mainGroup;
    }

    public ActionListener createActionListener()
    {
        return e ->
        {
            final Object source = e.getSource();
            if(source.getClass().equals(TileStoreRadioButton.class))
            {
                final TileStoreRadioButton button = (TileStoreRadioButton) source;

                button.getTileStore();

                if(button.isSelected())
                {
                    //view tiles
                    try
                    {
                        RadioButtonTree.this.map.setTileLoader(new TileStoreLoader(button.getTileStore(), RadioButtonTree.this.map));
                    }
                    catch (final TileStoreException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                else
                {
                    //tile disappear

                }
            }

        };
    }

}
