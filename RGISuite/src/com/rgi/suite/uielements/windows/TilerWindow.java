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

package com.rgi.suite.uielements.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.gdal.osr.SpatialReference;

import utility.GdalUtility;
import utility.SimpleGridBagConstraints;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.packager.Packager;
import com.rgi.suite.Settings;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreWriterAdapter;
import com.rgi.suite.uielements.PowerOfTwoSpinnerModel;
import com.rgi.suite.uielements.ProgressDialog;
import com.rgi.suite.uielements.SwatchButton;


/**
 * Gather additional information for tiling, and tile
 *
 * @author Luke D. Lambert
 *
 */
public class TilerWindow extends NavigationWindow
{
    private static final long   serialVersionUID      = -3488202344008846021L;
    private static final String TileWidthSettingName  = "ui.tiler.tileWidth";
    private static final String TileHeightSettingName = "ui.tiler.tileHeight";
    private static final String ClearColorSettingName = "ui.tiler.clearColor";

    private final Settings settings;

    private TileStoreWriterAdapter tileStoreWriterAdapter = null;

    // Input stuff
    private final JPanel     inputPanel            = new JPanel(new GridBagLayout());
    private final JTextField inputFileName         = new JTextField();
    private final JButton    inputFileNameButton   = new JButton("\u2026");
    private final JLabel     nativeReferenceSystem = new JLabel();

    private final JComboBox<CoordinateReferenceSystem> referenceSystems = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                           .stream()
                                                                                                           .sorted()
                                                                                                           .toArray(CoordinateReferenceSystem[]::new));
    // Output stuff
    private final JPanel outputPanel = new JPanel(new GridBagLayout());
    private final JComboBox<TileStoreWriterAdapter> outputStoreType = new JComboBox<>();

    private static final String LastInputLocationSettingName = "tiling.lastInputLocation";

    private final JSpinner     tileWidthSpinner;
    private final JSpinner     tileHeightSpinner;
    private final SwatchButton clearColorButton = new SwatchButton("");

    /**
     * Constructor
     *
     * @param settings
     *             Settings used to hint user preferences
     */
    public TilerWindow(final Settings settings)
    {
        this.setTitle(this.processName() + " Settings");
        this.setResizable(false);

        this.settings = settings;

        this.referenceSystems.setSelectedItem(null);

        this.outputStoreType.addItem(new GeoPackageTileStoreWriterAdapter(settings));
        this.outputStoreType.addItem(new TmsTileStoreWriterAdapter       (settings));

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        // Input stuff
        this.inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        this.inputFileNameButton.addActionListener(e -> { final String startDirectory = this.settings.get(LastInputLocationSettingName, System.getProperty("user.home"));

                                                          final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                          fileChooser.setMultiSelectionEnabled(false);
                                                          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                                                          final int option = fileChooser.showOpenDialog(this);

                                                          if(option == JFileChooser.APPROVE_OPTION)
                                                          {
                                                              final File file = fileChooser.getSelectedFile();

                                                              try
                                                              {
                                                                  this.inputFileName.setText(file.getAbsolutePath());

                                                                  this.settings.set(LastInputLocationSettingName, file.getParent());
                                                                  this.settings.save();

                                                                  final SpatialReference srs = GdalUtility.getDatasetSpatialReference(file);

                                                                  final CoordinateReferenceSystem crs = GdalUtility.getCoordinateReferenceSystem(srs);

                                                                  String crsName;

                                                                  if(crs != null)
                                                                  {
                                                                      crsName = crs.toString();
                                                                  }
                                                                  else
                                                                  {
                                                                      crsName = GdalUtility.getName(srs);
                                                                      if(crsName == null)
                                                                      {
                                                                          crsName = "<none specified>";
                                                                      }
                                                                  }

                                                                  this.nativeReferenceSystem.setText(crsName);
                                                                  this.referenceSystems.setSelectedItem(crs);

                                                                  this.tileStoreWriterAdapter.hint(file);
                                                              }
                                                              catch(final Exception ex)
                                                              {
                                                                  this.error(ex);
                                                              }
                                                          }
                                                        });

        this.contentPanel.add(this.inputPanel);

        this.buildInputContent();

        // Output Stuff
        this.outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

        this.outputStoreType.addActionListener(e -> { this.buildOutputContent();
                                                      try
                                                      {
                                                          this.tileStoreWriterAdapter.hint(new File(this.inputFileName.getText()));
                                                      }
                                                      catch(final Exception ex)
                                                      {
                                                          ex.printStackTrace();
                                                      }
                                                    });

        this.tileWidthSpinner = new JSpinner(new PowerOfTwoSpinnerModel(this.settings.get(TileWidthSettingName,
                                                                                          Integer::parseInt,
                                                                                          256),
                                                                        128,
                                                                        2048));

        this.tileWidthSpinner.setEnabled(false);

        this.tileHeightSpinner = new JSpinner(new PowerOfTwoSpinnerModel(this.settings.get(TileHeightSettingName,
                                                                                           Integer::parseInt,
                                                                                           256),
                                                                         128,
                                                                         2048));

        this.tileHeightSpinner.setEnabled(false);


        this.clearColorButton.setEnabled(false);
        this.clearColorButton.setPreferredSize(new Dimension(220, 20));
        this.clearColorButton.setColor(this.settings.get(ClearColorSettingName,
                                                         string -> colorFromString(string),
                                                         new Color(0, 0, 0, 0)));

        this.clearColorButton.addActionListener(e -> { final Color color = JColorChooser.showDialog(this,
                                                                                                    "Choose No Data color...",
                                                                                                    this.clearColorButton.getColor());

                                                       if(color != null)
                                                       {
                                                           this.clearColorButton.setColor(color);
                                                           this.settings.set(ClearColorSettingName, colorToString(color));
                                                           this.settings.save();
                                                       }
                                                     });
        this.buildOutputContent();

        this.contentPanel.add(this.outputPanel);

        this.pack();
    }

    private void buildInputContent()
    {
        this.inputPanel.removeAll();

        this.inputPanel.add(new JLabel("File:"),      new SimpleGridBagConstraints(0, 0, false));
        this.inputPanel.add(this.inputFileName,       new SimpleGridBagConstraints(1, 0, true));
        this.inputPanel.add(this.inputFileNameButton, new SimpleGridBagConstraints(2, 0, false));

        this.inputPanel.add(new JLabel("Native reference System:"), new SimpleGridBagConstraints(0, 1, false));
        this.inputPanel.add(this.nativeReferenceSystem,            new SimpleGridBagConstraints(1, 1, true));

        this.inputPanel.add(new JLabel("Output reference System:"), new SimpleGridBagConstraints(0, 2, false));
        this.inputPanel.add(this.referenceSystems,                  new SimpleGridBagConstraints(1, 2, true));
    }

    private void buildOutputContent()
    {
        this.outputPanel.removeAll();

        // Output tile store type
        this.outputPanel.add(new JLabel("Format:"), new SimpleGridBagConstraints(0, 0, false));
        this.outputPanel.add(this.outputStoreType,  new SimpleGridBagConstraints(1, 0, true));

        this.outputPanel.add(new JLabel("Tile width:"), new SimpleGridBagConstraints(0, 1, false));
        this.outputPanel.add(this.tileWidthSpinner,     new SimpleGridBagConstraints(1, 1, true));

        this.outputPanel.add(new JLabel("Tile height:"), new SimpleGridBagConstraints(0, 2, false));
        this.outputPanel.add(this.tileHeightSpinner,     new SimpleGridBagConstraints(1, 2, true));

        this.outputPanel.add(new JLabel("Clear color:"), new SimpleGridBagConstraints(0, 3, false));
        this.outputPanel.add(this.clearColorButton,      new SimpleGridBagConstraints(1, 3, true));

        this.tileStoreWriterAdapter = (TileStoreWriterAdapter)this.outputStoreType.getSelectedItem();

        int rowCount = 4;
        for(final Collection<JComponent> row : this.tileStoreWriterAdapter.getWriterParameterControls())
        {
            int columnCount = 0;
            for(final JComponent column : row)
            {
                // This is a work-around to resize (and then stretch) the middle column to fit our input form layout
                if(columnCount == 1)
                {
                    column.setPreferredSize(new Dimension(220, 25));
                }

                this.outputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1));

                ++columnCount;
            }

            ++rowCount;
        }

        this.revalidate();
        this.outputPanel.repaint();
        this.pack();
    }

    @Override
    protected String processName()
    {
        return "Tiling";
    }

    @Override
    protected boolean execute() throws Exception
    {
        final int tileWidth  = (int)this.tileWidthSpinner .getValue();
        final int tileHeight = (int)this.tileHeightSpinner.getValue();

        final Dimensions<Integer> tileDimensions = new Dimensions<>(tileWidth, tileHeight);

        final CoordinateReferenceSystem crs = (CoordinateReferenceSystem)this.referenceSystems.getSelectedItem();

        final Color noDataColor = this.clearColorButton.getColor();

        this.settings.set(TileWidthSettingName,  Integer.toString(tileWidth));
        this.settings.set(TileHeightSettingName, Integer.toString(tileHeight));
        this.settings.save();

        if(this.inputFileName.getText().isEmpty())
        {
            this.warn("Please select an input file.");
            return false;
        }

        if(this.referenceSystems.getSelectedItem() == null)
        {
            this.warn("Please select an output reference system");
            return false;
        }

        // This spawns a modal dialog and blocks this thread
        ProgressDialog.trackProgress(this,
                                     this.processName() + "...",
                                     taskMonitor -> { final File file = new File(this.inputFileName.getText());

                                                      try(final TileStoreReader tileStoreReader = new RawImageTileReader(file, tileDimensions, noDataColor, crs))
                                                      {
                                                          try(final TileStoreWriter tileStoreWriter = this.tileStoreWriterAdapter.getTileStoreWriter(tileStoreReader))
                                                          {
                                                              (new Packager(taskMonitor,
                                                                            tileStoreReader,
                                                                            tileStoreWriter)).execute();
                                                          }
                                                          catch(final Exception ex)
                                                          {
                                                              this.tileStoreWriterAdapter.removeStore();
                                                              throw ex;
                                                          }

                                                          return null;
                                                      }
                                                    });

        return true;
    }

    private static Color colorFromString(final String string)
    {
        final Pattern colorPattern = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+)");

        final Matcher colorMatcher = colorPattern.matcher(string);

        if(colorMatcher.matches())
        {
            try
            {
                return new Color(Integer.parseInt(colorMatcher.group(1)),
                                 Integer.parseInt(colorMatcher.group(2)),
                                 Integer.parseInt(colorMatcher.group(3)),
                                 Integer.parseInt(colorMatcher.group(4)));
            }
            catch(final IllegalArgumentException ex)
            {
                // Do nothing, fall through to return null
            }
        }

        return null;
    }

    private static String colorToString(final Color color)
    {
        return color == null ? null
                             : String.format("%d,%d,%d,%d",
                                             color.getRed(),
                                             color.getGreen(),
                                             color.getBlue(),
                                             color.getAlpha());
    }
}
