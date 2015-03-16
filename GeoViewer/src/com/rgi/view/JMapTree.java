/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
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

    private JMapViewer map;
    private RadioButtonTree tree;
    private JPanel treePanel;
    private JSplitPane splitPane;

    public JMapTree(List<TileStoreReader> stores, JMapViewer map){
        this(stores, false, map);
    }
    public JMapTree(List<TileStoreReader> stores, boolean treeVisible, JMapViewer map){
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
        createRefresh();
        setLayout(new BorderLayout());
        setTreeVisible(treeVisible);
    }
    public JMapViewer getViewer(){
        return this.map;
    }
    public RadioButtonTree getTree(){
        return this.tree;
    }
    
    public void addLayer(TileStoreReader store)
    {
        this.tree.addLayer(store);
    }
    
    public void setTreeVisible(boolean visible){
        removeAll();
        revalidate();
        if(visible){
            this.splitPane.setLeftComponent(this.treePanel);
            this.splitPane.setRightComponent(this.map);
            add(this.splitPane, BorderLayout.CENTER);
        }else add(this.map, BorderLayout.CENTER);
        repaint();
    }
    private void createRefresh(){
        this.tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(final TreeModelEvent e) {
                repaint();
            }
            @Override
            public void treeNodesInserted(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void treeNodesRemoved(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void treeStructureChanged(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
}
