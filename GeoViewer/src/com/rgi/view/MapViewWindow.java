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
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.TileStoreLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

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
    private final JLabel      currentZoomLevelValue   = new JLabel("");
    private final JLabel      unitsPerPixelXValue     = new JLabel("");
    private final JLabel      unitsPerPixelYValue     = new JLabel("");
    private final JLabel      coordinatePositionValue = new JLabel("");
    private final ButtonGroup mainGroup               = new ButtonGroup();
    private final JPanel      eastPanel               = new JPanel();
    private final JPanel      eastPanelSouthComponents= new JPanel();


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

        if(tileStoreReaders.isEmpty())
        {
            throw new IllegalArgumentException("There must be at least one Tile Store reader to display.");
        }

        this.tileStoreReaders = tileStoreReaders;
        this.viewer = new JMapViewer();

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

        //this adds a button to set the display to the center at the lowest integer zoom level
        final JButton backToCenterButton = new JButton("Center");
        backToCenterButton.setHorizontalAlignment(SwingConstants.LEFT);
        this.addCenterButton(backToCenterButton);

        //add Listener for the current coordinate
        this.mouseCoordinateListener();

        //create North panel and add components
        final JPanel northPanel = new JPanel();
        final JPanel panelTop = new JPanel();

        //West Panel
        final JPanel westPanel = new JPanel();

        //Set list of tileStore Radio Buttons
        this.eastPanel.setLayout(new BorderLayout());
        this.setListOfTileStores(westPanel);
        this.add(northPanel, BorderLayout.NORTH);

        northPanel.setLayout(new BorderLayout());
        northPanel.add(panelTop, BorderLayout.NORTH);

        panelTop.add(backToCenterButton);
        panelTop.add(showTileGrid);

        this.setSize(950, 800);
        this.repaint();

    }

    private void mouseCoordinateListener()
    {
        this.viewer.addMouseMotionListener(new MouseMotionListener()
        {

            @Override
            public void mouseMoved(final MouseEvent e)
            {
                final Coordinate latLong = MapViewWindow.this.viewer.getPosition(e.getPoint());
                MapViewWindow.this.updateCoordinate(latLong);
            }

            @Override
            public void mouseDragged(final MouseEvent e)
            {
                //no desired action required
            }
        });
    }

    protected void updateCoordinate(final Coordinate latLong)
    {
        this.coordinatePositionValue.setText(String.format("Latitude: %f    Longitude: %f", latLong.getLat(), latLong.getLon()));
    }

    private void setListOfTileStores(final JPanel westPanel)
    {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        //west panel with the radio buttons
        westPanel.add(this.createRadioButtons());
        //east panel that has the viewer
        this.eastPanel.add(this.viewer, BorderLayout.CENTER);
        //east panel that has zoom/meters per pixel/ and lat long at the bottom of the screen
        this.eastPanelSouthComponents.setLayout(new GridLayout(1, 1, 0, 5));
        this.eastPanelSouthComponents.add(this.currentZoomLevelValue);
        this.eastPanelSouthComponents.add(this.unitsPerPixelXValue);
        this.eastPanelSouthComponents.add(this.coordinatePositionValue);
        this.eastPanel.add(this.eastPanelSouthComponents, BorderLayout.SOUTH);

        splitPane.setLeftComponent(westPanel);
        splitPane.setRightComponent(this.eastPanel);
        this.add(splitPane, BorderLayout.CENTER);

        splitPane.setOneTouchExpandable(true);

        //Provide minimum sizes for the two components in the split pane
        final Dimension minimumSize = new Dimension(100, 50);
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
            final Dimensions<Integer> tileDimensions = this.getSelectedStore().getImageDimensions();

            if(tileDimensions == null)
            {
                throw new IllegalArgumentException("Tile dimensions must be defined.");//added for coverity scan
            }

            final double tileSizeX   = tileDimensions.getWidth();
            final int    matrixWidth = this.getSelectedStore().getTileScheme().dimensions(currentZoom).getWidth();

            final Double unitsPerPixelValueXCalculation = boundsWidth /(tileSizeX * matrixWidth);
            this.unitsPerPixelXValue.setText(String.format("Meters/Pixel: %.7f", unitsPerPixelValueXCalculation));
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
        this.currentZoomLevelValue.setText(String.format("Zoom Level: %s", this.viewer.getZoom()));
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
