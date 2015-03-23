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
package com.rgi.suite;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractSpinnerModel;
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

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreWriterAdapter;

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
    private final JPanel     inputPanel          = new JPanel(new GridBagLayout());
    private final JTextField inputFileName       = new JTextField();
    private final JButton    inputFileNameButton = new JButton("\u2026");

    private final JComboBox<CoordinateReferenceSystem> crsComboBox = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                      .stream()
                                                                                                      .sorted()
                                                                                                      .toArray(CoordinateReferenceSystem[]::new));
    // Output stuff
    private final JPanel outputPanel = new JPanel(new GridBagLayout());
    private final JComboBox<TileStoreWriterAdapter> outputStoreType = new JComboBox<>();

    private final String processName = "Tiling";

    private static final String LastInputLocationSettingName = "tiling.lastInputLocation";

    private final JSpinner     tileWidthSpinner;
    private final JSpinner     tileHeightSpinner;
    private final SwatchButton clearColorButton;

    /**
     * Constructor
     *
     * @param settings
     *             Settings used to hint user preferences
     */
    public TilerWindow(final Settings settings)
    {
        this.setTitle(this.processName + " Settings");
        this.setResizable(false);

        this.settings = settings;

        this.outputStoreType.addItem(new GeoPackageTileStoreWriterAdapter(settings));
        this.outputStoreType.addItem(new TmsTileStoreWriterAdapter       (settings));

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        // Input stuff
        this.inputFileName.setEditable(false);

        this.inputPanel .setBorder(BorderFactory.createTitledBorder("Input"));

        this.inputFileNameButton.addActionListener(e -> { final String startDirectory = this.settings.get(LastInputLocationSettingName, System.getProperty("user.home"));

                                                          final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                          fileChooser.setMultiSelectionEnabled(false);
                                                          fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                          final int option = fileChooser.showOpenDialog(this);

                                                          if(option == JFileChooser.APPROVE_OPTION)
                                                          {
                                                              final File file = fileChooser.getSelectedFile();

                                                              try
                                                              {
                                                                  this.inputFileName.setText(file.getAbsolutePath());

                                                                  this.settings.set(LastInputLocationSettingName, file.getParent());
                                                                  this.settings.save();

                                                                  this.buildInputContent();
                                                                  this.tileStoreWriterAdapter.hint(file);
                                                              }
                                                              catch(final Exception ex)
                                                              {
                                                                  this.error(this.processName, ex.getMessage());
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

        this.tileWidthSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(TileWidthSettingName,
                                                                                     Integer::parseInt,
                                                                                     256),
                                                            128,
                                                            2048));

        this.tileHeightSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(TileHeightSettingName,
                                                                                      Integer::parseInt,
                                                                                      256),
                                                             128,
                                                             2048));


        this.clearColorButton = new SwatchButton("");
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

        this.inputPanel.add(new JLabel("Reference System:"), new SimpleGridBagConstraints(0, 1, false));
        this.inputPanel.add(this.crsComboBox,                new SimpleGridBagConstraints(1, 1, true));
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
                if(columnCount == 1) // TODO; This is a HACK
                {
                    column.setPreferredSize(new Dimension(220, 25));
                }

                this.outputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1)); // TODO; last parameter is similarly a hack for that second column

                ++columnCount;
            }

            ++rowCount;
        }

        this.revalidate();
        this.outputPanel.repaint();
        this.pack();
    }

    @Override
    protected void execute() throws Exception
    {
        final int tileWidth  = (int)this.tileWidthSpinner .getValue();
        final int tileHeight = (int)this.tileHeightSpinner.getValue();

        final Color color = this.clearColorButton.getColor();

        // Save UI values - TODO put these in the event handlers
        this.settings.set(TileWidthSettingName,  Integer.toString(tileWidth));
        this.settings.set(TileHeightSettingName, Integer.toString(tileHeight));

        this.settings.save();

//        final Tiler tiler = new Tiler(new File(this.inputFileName.getText()),
//                                      this.tileStoreWriterAdapter.getTileStoreWriter(),
//                                      new Dimensions<>(tileWidth,
//                                                       tileHeight),
//                                      color);
//        tiler.execute();
    }


    @SuppressWarnings("serial")
    private class PowerOfTwoSpinner extends AbstractSpinnerModel
    {
        private final int binaryLogOfMinimum;
        private final int binaryLogOfMaximum;

        private int binaryLogOfValue;

        public PowerOfTwoSpinner(final int initial, final int minimum, final int maximum)
        {
            this.binaryLogOfMinimum = this.binaryLog(minimum);
            this.binaryLogOfMaximum = this.binaryLog(maximum);

            this.setValue(initial);
        }

        @Override
        public Object getValue()
        {
            return (int)Math.pow(2, this.binaryLogOfValue);
        }

        @Override
        public void setValue(final Object value)
        {
            this.binaryLogOfValue = this.binaryLog((int)value);
            this.fireStateChanged();
        }

        @Override
        public Object getNextValue()
        {
            final Object foo = this.binaryLogOfValue >= this.binaryLogOfMaximum ? null
                                                                    : (int)Math.pow(2, this.binaryLogOfValue+1);

            return foo;
        }

        @Override
        public Object getPreviousValue()
        {
            return this.binaryLogOfValue <= this.binaryLogOfMinimum ? null
                                                                    : (int)Math.pow(2, this.binaryLogOfValue-1);
        }

        private int binaryLog(final int val)
        {
            return (int)(Math.log(val) / Math.log(2));
        }
    }

    private static CoordinateReferenceSystem getCrs(@SuppressWarnings("unused") final File file) throws RuntimeException
    {
        return null;

        // TODO requires GDAL to work for this project
        //osr.UseExceptions(); // TODO only do this once
        //gdal.AllRegister();  // TODO only do this once
        //
        //final Dataset dataset = gdal.Open(file.getAbsolutePath(),
        //                                  gdalconstConstants.GA_ReadOnly);
        //
        //if(dataset == null)
        //{
        //    return null;
        //}
        //
        //final SpatialReference srs = new SpatialReference(dataset.GetProjection());
        //
        //gdal.GDALDestroyDriverManager(); // TODO only do this once
        //
        //final String attributePath = "PROJCS|GEOGCS|AUTHORITY";   // https://gis.stackexchange.com/questions/20298/
        //
        //final String authority  = srs.GetAttrValue(attributePath, 0);
        //final String identifier = srs.GetAttrValue(attributePath, 1);
        //
        //if(authority == null || identifier == null)
        //{
        //    return null;    // Failed to get the attribute value for some reason, see: http://gdal.org/java/org/gdal/osr/SpatialReference.html#GetAttrValue(java.lang.String,%20int)
        //}
        //
        //try
        //{
        //    return new CoordinateReferenceSystem(authority, Integer.parseInt(identifier));
        //}
        //catch(final NumberFormatException ex)
        //{
        //    return null;    // The authority identifier in the WKT wasn't an integer
        //}
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
