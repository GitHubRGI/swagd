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
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.TileStoreLoader;
import org.openstreetmap.gui.jmapviewer.checkBoxTree.CheckBoxNodeData;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;

import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * View a supported tile store within a map viewer.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 * @author Jenifer Cochran
 *
 */
public class MapViewWindow extends JFrame implements JMapViewerEventListener
{
    private com.rgi.common.coordinate.Coordinate<Double> center = new com.rgi.common.coordinate.Coordinate<>(0.0, 0.0);

    private int minZoomLevel = 0;

    @Deprecated
    private final TileStoreReader tileStore;

    private final Collection<TileStoreReader> tileStoreReaders;

    private final JLabel currentZoomLevelValue = new JLabel("");
    private final JLabel unitsPerPixelXLabel   = new JLabel("Units/PixelX: ");
    private final JLabel unitsPerPixelYLabel   = new JLabel("Units/PixelY: ");
    private final JLabel unitsPerPixelXValue   = new JLabel("");
    private final JLabel unitsPerPixelYValue   = new JLabel("");

    /**
     * @param tileStoreReaders
     *             Tile stores to display
     * @throws TileStoreException Thrown when the file is not supported for viewing.
     */
    public MapViewWindow(final Collection<TileStoreReader> tileStoreReaders) throws TileStoreException
    {
        super("Tile Viewer");

        if(tileStoreReaders == null || tileStoreReaders.isEmpty())
        {
            throw new IllegalArgumentException("Tile store reader collection may not be null or empty");
        }

        this.tileStoreReaders = tileStoreReaders;

        
        this.tileStore = this.pickTileStore(location);
        this.treeMap   = new JMapViewerTree(this.tileStore.getName());
        
        this.addWindowListener(new WindowAdapter()
                              {
                                  @Override
                                  public void windowClosing(final WindowEvent windowEvent)
                                  {
                                      MapViewWindow.this.cleanUpResources();
                                  }
                              });

        this.treeMap.getViewer().addJMVListener(this);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);

        new DefaultMapController(this.treeMap.getViewer()).setMovementMouseButton(MouseEvent.BUTTON1);

        for(final TileStoreReader tileStoreReader : this.tileStoreReaders)
        {
            this.treeMap.getViewer().setTileLoader(new TileStoreLoader(tileStoreReader, this.treeMap.getViewer()));
        }

        //Set the initial display position
        setInitialDisplayPosition();
        
        //add tile grid checkbox
        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        addCheckboxForTileGridLines(showTileGrid);
        
        //this adds a button to set the display to the center at the lowest integer zoom level
        final JButton backToCenterButton = new JButton("Center");
        addCenterButton(backToCenterButton);
        
        //this.treeMap.getViewer().setTileSource(new TileStoreTileSource(tileStore)); // TODO - investigate which method is causing the viewer to not work

        //This will display the zoom level and resolution
        JLabel currentZoomLevelLabel = new JLabel("Zoom Level: ");
        updateZoomParameters();
        
        //add data Hierarchy
        final JCheckBox dataHierarchyLayers = new JCheckBox("Data Hierarchy visible");
        addDataHierarchy(dataHierarchyLayers);
        
        //create listener for tree checkbox
        createTreeListener(this.treeMap);
        

        //create panels and add components
        final JPanel panel = new JPanel();
        final JPanel panelTop = new JPanel();
        final JPanel panelBottom = new JPanel();
        
        this.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);
        
        
        panelBottom.add(showTileGrid);
        panelBottom.add(dataHierarchyLayers);
        panelTop.add(backToCenterButton);
        panelTop.add(currentZoomLevelLabel);
        panelTop.add(this.currentZoomLevelValue);
        panelTop.add(this.unitsPerPixelXLabel);
        panelTop.add(this.unitsPerPixelXValue);
        panelTop.add(this.unitsPerPixelYLabel);
        panelTop.add(this.unitsPerPixelYValue);

        this.add(this.treeMap, BorderLayout.CENTER);
    }

    private void createTreeListener(JMapViewerTree tree)
    {
        tree.getTree().getModel().addTreeModelListener(new TreeModelListener(){

            @Override
            public void treeNodesChanged(TreeModelEvent e)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree
                        .getTree().getLastSelectedPathComponent();

                if (node.equals(tree.getTree().rootNode()))
                {
                    tree.getViewer().setVisible(data(node).isSelected());
                    tree.setTreeVisible(true);
                    repaint();
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e)
            {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e)
            {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
        });
    }
    
    private static CheckBoxNodeData data(DefaultMutableTreeNode node){
        return node==null?null:(CheckBoxNodeData)node.getUserObject();
    }

    private void addDataHierarchy(JCheckBox dataHierarchyLayers)
    {
        dataHierarchyLayers.addActionListener(e -> MapViewWindow.this.treeMap.setTreeVisible(dataHierarchyLayers.isSelected()));

    }

    private void addCenterButton(JButton backToCenterButton)
    {
        backToCenterButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                MapViewWindow.this.treeMap.getViewer().setDisplayPosition(new Coordinate(MapViewWindow.this.center.getY(),
                                                                                         MapViewWindow.this.center.getX()),
                                                                                         MapViewWindow.this.minZoomLevel);
                updateZoomParameters();
            }   
        });
    }

    private void addCheckboxForTileGridLines(JCheckBox showTileGrid)
    {
        showTileGrid.setSelected(this.treeMap.getViewer().isTileGridVisible());
        showTileGrid.addActionListener(new ActionListener() 
                                                          {
                                                            @Override
                                                            public void actionPerformed(ActionEvent e) 
                                                              {
                                                                MapViewWindow.this.treeMap.getViewer().setTileGridVisible(showTileGrid.isSelected());
                                                              }
                                                          });
    }

    private void updateUnitsPerPixel()
    {
        try
        {
            int currentZoom = this.treeMap.getViewer().getZoom();
            
            double boundsWidth = this.tileStore.getBounds().getWidth();
            double tileSizeX   = this.tileStore.getImageDimensions().getWidth();
            int    matrixWidth = this.tileStore.getTileScheme().dimensions(currentZoom).getWidth();
            
            double boundsHeight = this.tileStore.getBounds().getHeight();
            double tileSizeY    = this.tileStore.getImageDimensions().getHeight();
            int    matrixHeight = this.tileStore.getTileScheme().dimensions(currentZoom).getHeight();
            
            Double unitsPerPixelValueXCalculation = boundsWidth /(tileSizeX * matrixWidth);
            Double unitsPerPixelValueYCalculation = boundsHeight/(tileSizeY * matrixHeight);
            
            //if calculations are equal only display one scale
            if(isEqual(unitsPerPixelValueXCalculation, unitsPerPixelValueYCalculation))
            {
                this.unitsPerPixelXLabel.setText("Units/Pixel: ");
                this.unitsPerPixelYLabel.setVisible(false);
                this.unitsPerPixelYValue.setVisible(false);
                this.unitsPerPixelXValue.setText(String.format("%.4f", unitsPerPixelValueXCalculation));
            }
            else
            {
                //if not equal show both for x and y
                this.unitsPerPixelXLabel.setText("Units/PixelX: ");//change label to specify X
                
                this.unitsPerPixelXValue.setText(String.format("%.4f", unitsPerPixelValueXCalculation));// place value of x
                this.unitsPerPixelYValue.setText(String.format("%.4f", unitsPerPixelValueYCalculation));//plave value of Y
                
                this.unitsPerPixelYLabel.setVisible(true);//set y label visible
                this.unitsPerPixelYValue.setVisible(true);//set y value visible
            }
            
        } 
        catch (TileStoreException | IllegalArgumentException e)
        {
            this.unitsPerPixelXValue.setText("Unable To Calculate at this zoom level");
            
            if(this.unitsPerPixelYValue.isVisible())
            {
                this.unitsPerPixelYValue.setText("Unable To Calculate at this zoom level");
            }
        }
    }
    
    private static boolean isEqual(Double first, Double second)
    {
        final double EPSILON = 0.0000001;
        return first == null ? second == null: Math.abs(Double.valueOf(first) - Double.valueOf(second)) <= EPSILON;
    }

    @Override
    public void processCommand(final JMVCommandEvent command)
    {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

    private void cleanUpResources()
    {
        for(final TileStoreReader tileStoreReader : this.tileStoreReaders)
        {
            try
            {
                tileStoreReader.close();
            }
            catch(final Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private JMapViewer map()
    {
        return this.treeMap.getViewer();
    }
    
    private void updateZoomParameters() {
            this.updateUnitsPerPixel();
            this.currentZoomLevelValue.setText(String.format("%s", map().getZoom()));
    }
    private void setInitialDisplayPosition()
    {
        final CrsProfile profile = CrsProfileFactory.create(this.tileStore.getCoordinateReferenceSystem());
        try
        {
            this.center = profile.toGlobalGeodetic(this.tileStore.getBounds().getCenter());
            this.minZoomLevel = Collections.min(this.tileStore.getZoomLevels());

            this.treeMap.getViewer()
                        .setDisplayPosition(new Coordinate(this.center.getY(),
                                                           this.center.getX()),
                                                           this.minZoomLevel);
        }
        catch(final TileStoreException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static final long serialVersionUID = 1337L;

    private final JMapViewerTree treeMap;
}
