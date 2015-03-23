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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

import com.rgi.common.tile.store.TileStoreReader;
//This is a work in progress do not rely on this code
@SuppressWarnings("javadoc")
public class JMapTree extends JPanel
{
    /** Serial Version UID */
    private static final long serialVersionUID = 3050203054402323972L;

    private final JMapViewer map;
    private final RadioButtonTree tree;
    private final JPanel treePanel;
    private final JSplitPane splitPane;

    public JMapTree(final List<TileStoreReader> stores, final JMapViewer map){
        this(stores, false, map);
    }
    public JMapTree(final List<TileStoreReader> stores, final boolean treeVisible, final JMapViewer map){
        super();
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        this.map = map;
        this.tree = new RadioButtonTree(stores, this.map);
        this.treePanel = new JPanel();
//        this.treePanel.setLayout(new BorderLayout());
//        this.treePanel.add(this.tree, BorderLayout.CENTER);
//        this.treePanel.add(new JLabel("<html><center>Use right mouse button to<br />show/hide texts</center></html>"), BorderLayout.SOUTH);


//        this.splitPane.setOneTouchExpandable(true);
//        this.splitPane.setDividerLocation(150);

        //Provide minimum sizes for the two components in the split pane
//        Dimension minimumSize = new Dimension(100, 50);
        //tree.setMinimumSize(minimumSize);
//        this.map.setMinimumSize(minimumSize);
        this.createRefresh();
        this.setLayout(new BorderLayout());
        this.setTreeVisible(treeVisible);
    }
    public JMapViewer getViewer(){
        return this.map;
    }
    public RadioButtonTree getTree(){
        return this.tree;
    }

    public void addLayer(final TileStoreReader store)
    {
        this.tree.addLayer(store);
    }

    public void setTreeVisible(final boolean visible){
        this.removeAll();
        this.revalidate();
        if(visible){
            this.splitPane.setLeftComponent(this.treePanel);
            this.splitPane.setRightComponent(this.map);
            this.add(this.splitPane, BorderLayout.CENTER);
        } else
        {
            this.add(this.map, BorderLayout.CENTER);
        }
        this.repaint();
    }
    private void createRefresh(){
        this.tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(final TreeModelEvent e) {
                JMapTree.this.repaint();
            }
            @Override
            public void treeNodesInserted(final TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void treeNodesRemoved(final TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void treeStructureChanged(final TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
}
