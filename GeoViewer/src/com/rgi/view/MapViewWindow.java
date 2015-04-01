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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.TileStoreLoader;
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

    private final Collection<TileStoreReader> tileStoreReaders;

    JMapViewer viewer;
    boolean treeSelected = false;

    private final JLabel currentZoomLevelValue   = new JLabel("");
    private final JLabel unitsPerPixelXLabel     = new JLabel("Units/PixelX: ");
    private final JLabel unitsPerPixelYLabel     = new JLabel("Units/PixelY: ");
    private final JLabel unitsPerPixelXValue     = new JLabel("");
    private final JLabel unitsPerPixelYValue     = new JLabel("");
    private final JLabel coordinatePosition      = new JLabel("Current Position: ");
    private final       JLabel coordinatePositionValue = new JLabel("");
    private final ButtonGroup mainGroup          = new ButtonGroup();

    /**
     * Constructor
     *
     * @param tileStoreReaders
     *             Tile stores to display
     */
    public MapViewWindow(final Collection<TileStoreReader> tileStoreReaders)
    {
        super("Tile Viewer");

        if(tileStoreReaders == null)
        {
            throw new IllegalArgumentException("Tile store reader collection may not be null");
        }

        this.tileStoreReaders = tileStoreReaders;
        this.viewer = new JMapViewer();

        // TODO when tree is working use this to display the tileStores
        //this.treeMap   = new JMapTree(stores);
        //this.viewer    = this.treeMap.getViewer();
        //this.treeMap.setName("TileSets");

        this.addWindowListener(new WindowAdapter()
                              {
                                  @Override
                                  public void windowClosing(final WindowEvent windowEvent)
                                  {
                                      MapViewWindow.this.cleanUpResources();
                                  }
                              });

        this.viewer.addJMVListener(this);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        new DefaultMapController(this.viewer).setMovementMouseButton(MouseEvent.BUTTON1);

        //add tile grid checkbox
        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        this.addCheckboxForTileGridLines(showTileGrid);
       // this.viewer.setTileSource(new TileStoreTileSource(this.tileStore)); // TODO - investigate which method is causing the viewer to not work//its lat/long to tilex/y and visa versa

        //This will display the zoom level and resolution
        final JLabel currentZoomLevelLabel = new JLabel("Zoom Level: ");

        //this adds a button to set the display to the center at the lowest integer zoom level
        final JButton backToCenterButton = new JButton("Center");
        this.addCenterButton(backToCenterButton);

        //add Listener for the current coordinate
        this.mouseCoordinateListener();

        //set tree visible //TODO this will be added when tree is working
        // this.treeMap.setTreeVisible(true);
       // this.treeMap.addLayer(element)

        //create listener for tree
       // createTreeListener(this.treeMap);

        //create North panel and add components
        final JPanel northPanel = new JPanel();
        final JPanel panelTop = new JPanel();
        final JPanel panelBottom = new JPanel();

        //West Panel
        final JPanel westPanel = new JPanel();

        //Set list of tileStore Radio Buttons
        this.setListOfTileStores(westPanel);
        this.add(northPanel, BorderLayout.NORTH);

        northPanel.setLayout(new BorderLayout());
        northPanel.add(panelTop, BorderLayout.NORTH);
        northPanel.add(panelBottom, BorderLayout.SOUTH);

//        this.viewer.setLayout(new BorderLayout());//TODO
//        JPanel southPanel = new JPanel();
//        southPanel.add(this.coordinatePosition);
//        southPanel.add(this.coordinatePositionValue);
//        this.viewer.add(southPanel, BorderLayout.SOUTH);

        panelBottom.add(showTileGrid);
        panelBottom.add(this.coordinatePosition);
        panelBottom.add(this.coordinatePositionValue);
        panelTop.add(backToCenterButton);
        panelTop.add(currentZoomLevelLabel);
        panelTop.add(this.currentZoomLevelValue);
        panelTop.add(this.unitsPerPixelXLabel);
        panelTop.add(this.unitsPerPixelXValue);
        panelTop.add(this.unitsPerPixelYLabel);
        panelTop.add(this.unitsPerPixelYValue);
        this.setSize(800, 800);
        this.repaint();

    }

    private void mouseCoordinateListener()
    {
        this.viewer.addMouseMotionListener(new MouseMotionListener()
        {

            @Override
            public void mouseMoved(final MouseEvent e)
            {
                Coordinate latLong = MapViewWindow.this.viewer.getPosition(e.getPoint());
                MapViewWindow.this.updateCoordinate(latLong);
            }

            @Override
            public void mouseDragged(final MouseEvent e)
            {
                // TODO Auto-generated method stub

            }
        });
    }

    protected void updateCoordinate(final Coordinate latLong)
    {
        this.coordinatePositionValue.setText(String.format("%f %f", latLong.getLat(), latLong.getLon()));
    }

    private void setListOfTileStores(final JPanel westPanel)
    {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        westPanel.add(this.createRadioButtons());

        splitPane.setLeftComponent(westPanel);
        splitPane.setRightComponent(this.viewer);
        this.add(splitPane, BorderLayout.CENTER);

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

      //Provide minimum sizes for the two components in the split pane
        final Dimension minimumSize = new Dimension(100, 50);
      //tree.setMinimumSize(minimumSize);
       this.viewer.setMinimumSize(minimumSize);

        this.repaint();
    }

    private JPanel createRadioButtons()
    {
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(this.tileStoreReaders.size(), 1));
        final List<TileStoreRadioButton> buttonList = new ArrayList<>();

        this.tileStoreReaders.stream().forEach(store -> {
                                                final TileStoreRadioButton button = new TileStoreRadioButton(store);
                                                this.mainGroup.add(button);
                                                buttonPanel.add(button);
                                                button.addActionListener(this.createActionListener());
                                                buttonList.add(button);
                                             });

        this.mainGroup.setSelected(buttonList.get(0).getModel(), true);
        try
        {
            this.viewer.setTileLoader(new TileStoreLoader(this.getSelectedStore(), this.viewer));
            this.viewer.setTileSource(new TileStoreTileSource(this.getSelectedStore()));

        }
        catch (final TileStoreException e)
        {
            e.printStackTrace();
        }
        this.setInitialDisplayPosition(this.getSelectedStore());

        return buttonPanel;
    }

    private ActionListener createActionListener()
    {
        return e -> {
            final Object source = e.getSource();
            if(source.getClass() == (TileStoreRadioButton.class))
            {
                final TileStoreRadioButton button = (TileStoreRadioButton) source;

                if(button.isSelected())
                {
                    //view tiles
                    try
                    {
                        MapViewWindow.this.viewer.setTileLoader(new TileStoreLoader(button.getTileStore(), MapViewWindow.this.viewer));
                        MapViewWindow.this.viewer.setTileSource(new TileStoreTileSource(button.getTileStore()));
                        MapViewWindow.this.setInitialDisplayPosition(button.getTileStore());

                    }
                    catch (final TileStoreException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        };
    }

    private void addCenterButton(final JButton backToCenterButton)
    {
        backToCenterButton.addActionListener(e -> {
                MapViewWindow.this.setInitialDisplayPosition(this.getSelectedStore());
                MapViewWindow.this.updateZoomParameters();
        });

    }

    private TileStoreReader getSelectedStore()
    {
        final Enumeration<AbstractButton> selectedTileStore = this.mainGroup.getElements();

       while(selectedTileStore.hasMoreElements())
       {
           final TileStoreRadioButton button = (TileStoreRadioButton) selectedTileStore.nextElement();

           if(button.isSelected())
           {
               return button.store;
           }
       }
            return null;
    }

    private void addCheckboxForTileGridLines(final JCheckBox showTileGrid)
    {
        showTileGrid.setSelected(this.viewer.isTileGridVisible());
        showTileGrid.addActionListener(e -> MapViewWindow.this.viewer.setTileGridVisible(showTileGrid.isSelected()));
    }

    private void updateUnitsPerPixel()
    {
        try
        {
            final int currentZoom = this.viewer.getZoom();

            final double boundsWidth = this.getSelectedStore().getBounds().getWidth();
            final double tileSizeX   = this.getSelectedStore().getImageDimensions().getWidth();
            final int    matrixWidth = this.getSelectedStore().getTileScheme().dimensions(currentZoom).getWidth();

            final double boundsHeight = this.getSelectedStore().getBounds().getHeight();
            final double tileSizeY    = this.getSelectedStore().getImageDimensions().getHeight();
            final int    matrixHeight = this.getSelectedStore().getTileScheme().dimensions(currentZoom).getHeight();

            final Double unitsPerPixelValueXCalculation = boundsWidth /(tileSizeX * matrixWidth);
            final Double unitsPerPixelValueYCalculation = boundsHeight/(tileSizeY * matrixHeight);

            //if calculations are equal only display one scale
            if(isEqual(unitsPerPixelValueXCalculation, unitsPerPixelValueYCalculation))
            {
                this.unitsPerPixelXLabel.setText("Units/Pixel: ");
                this.unitsPerPixelYLabel.setVisible(false);
                this.unitsPerPixelYValue.setVisible(false);
                this.unitsPerPixelXValue.setText(String.format("%.7f", unitsPerPixelValueXCalculation));
            }
            else
            {
                //if not equal show both for x and y
                this.unitsPerPixelXLabel.setText("Units/PixelX: ");//change label to specify X

                this.unitsPerPixelXValue.setText(String.format("%.7f", unitsPerPixelValueXCalculation));// place value of x
                this.unitsPerPixelYValue.setText(String.format("%.7f", unitsPerPixelValueYCalculation));//place value of Y

                this.unitsPerPixelYLabel.setVisible(true);//set y label visible
                this.unitsPerPixelYValue.setVisible(true);//set y value visible
            }

        }
        catch (final Exception e)
        {
            this.unitsPerPixelXValue.setText("Unable To Calculate at this zoom level");

            if(this.unitsPerPixelYValue.isVisible())
            {
                this.unitsPerPixelYValue.setText("Unable To Calculate at this zoom level");
            }
        }
    }

    private static boolean isEqual(final Double first, final Double second)
    {
        final double EPSILON = 0.0000001;
        return first == null ? second == null: Math.abs(Double.valueOf(first) - Double.valueOf(second)) <= EPSILON;
    }

    @Override
    public void processCommand(final JMVCommandEvent command)
    {
        if(command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
           command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE))
        {
            this.updateZoomParameters();
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

    private void updateZoomParameters()
    {
        this.updateUnitsPerPixel();
        this.currentZoomLevelValue.setText(String.format("%s", this.viewer.getZoom()));
    }

    private void setInitialDisplayPosition(final TileStoreReader store)
    {
        try
        {
            final CrsProfile profile = CrsProfileFactory.create(store.getCoordinateReferenceSystem());
            MapViewWindow.this.center = profile.toGlobalGeodetic(store.getBounds().getCenter());

            if(!store.getZoomLevels().isEmpty())    // TODO attn Jen: error message?
            {

                MapViewWindow.this.minZoomLevel = Collections.min(store.getZoomLevels());

                MapViewWindow.this.viewer
                                  .setDisplayPosition(new Coordinate(this.center.getY(),
                                                                     this.center.getX()),
                                                                     this.minZoomLevel);
                this.updateZoomParameters();
            }
        }
        catch(final TileStoreException ex)
        {
            ex.printStackTrace();
        }
    }

    private static final long serialVersionUID = 1337L;
}
