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

import java.awt.event.ActionEvent;
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
    private JMapViewer map;
    private List<TileStoreRadioButton> buttonList = new ArrayList<>();
    
    public RadioButtonTree(List<TileStoreReader> stores, JMapViewer map)
    {
        this.map = map;
        this.mainGroup = new ButtonGroup();
        
       this.addGroup(stores);
    }

    public void addLayer(TileStoreReader store)
    {
        TileStoreRadioButton button = new TileStoreRadioButton(store);
        this.mainGroup.add(button); 
        button.addActionListener(this.createActionListener()); 
        this.buttonList.add(button);
    }

    public void addGroup(List<TileStoreReader> tileStores)
    {
        tileStores.stream().forEach(store -> {
                                                TileStoreRadioButton button = new TileStoreRadioButton(store);
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
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object source = e.getSource();
                if(source.getClass().equals(TileStoreRadioButton.class))
                {
                    TileStoreRadioButton button = (TileStoreRadioButton) source;
                    
                    button.getTileStore();
                    
                    if(button.isSelected())
                    {
                        //view tiles
                        try
                        {
                            RadioButtonTree.this.map.setTileLoader(new TileStoreLoader(button.getTileStore(), RadioButtonTree.this.map));
                        } 
                        catch (TileStoreException e1)
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
                
            }
        };
    }
  
}
