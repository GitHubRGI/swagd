package com.rgi.suite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
public class TilerWindow extends JFrame
{
    private static final long serialVersionUID = -3488202344008846021L;

    private final Settings settings;

    private TileStoreWriterAdapter tileStoreWriterAdapter = null;

    protected final JPanel contentPanel = new JPanel();

    // Input stuff
    private final JPanel     inputPanel          = new JPanel(new GridBagLayout());
    private final JTextField inputFileName       = new JTextField();
    private final JButton    inputFileNameButton = new JButton("\u2026");

    private final JComboBox<CoordinateReferenceSystem> crsComboBox = new JComboBox<>(new DefaultComboBoxModel<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                                                 .stream()
                                                                                                                                 .sorted()
                                                                                                                                 .toArray(CoordinateReferenceSystem[]::new)));
    // Output stuff
    private final JPanel outputPanel = new JPanel(new GridBagLayout());
    private final JComboBox<TileStoreWriterAdapter> outputStoreType = new JComboBox<>(new DefaultComboBoxModel<>());

    // Navigation stuff
    private final JPanel  navigationPanel = new JPanel(new GridBagLayout());
    private final JButton okButton        = new JButton("OK");
    private final JButton cancelButton    = new JButton("Cancel");

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
        this.setLayout(new BorderLayout());
        this.setResizable(false);

        this.settings = settings;

        this.outputStoreType.addItem(new GeoPackageTileStoreWriterAdapter(settings));
        this.outputStoreType.addItem(new TmsTileStoreWriterAdapter       (settings));

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        this.add(this.contentPanel,   BorderLayout.CENTER);
        this.add(this.navigationPanel,BorderLayout.SOUTH);

        // Input stuff
        this.inputFileName.setEditable(false);

        this.inputPanel .setBorder(BorderFactory.createTitledBorder("Input"));

        this.inputFileNameButton.addActionListener(e -> { final String startDirectory = this.settings.get(LastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);

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
                                                                  this.error(ex.getMessage());
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

        this.tileWidthSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(SettingsWindow.TileWidthSettingName,
                                                                                     Integer::parseInt,
                                                                                     SettingsWindow.DefaultTileWidth),
                                                            128,
                                                            2048));

        this.tileHeightSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(SettingsWindow.TileHeightSettingName,
                                                                                      Integer::parseInt,
                                                                                      SettingsWindow.DefaultTileHeight),
                                                             128,
                                                             2048));


        this.clearColorButton = new SwatchButton("");
        this.clearColorButton.setColor(this.settings.get(SettingsWindow.NoDataColorSettingName,
                                                         SettingsWindow::colorFromString,
                                                         SettingsWindow.DefaultNoDataColor));

        this.clearColorButton.addActionListener(e -> { final Color color = JColorChooser.showDialog(this,
                                                                                                    "Choose No Data color...",
                                                                                                    this.clearColorButton.getColor());

                                                       if(color != null)
                                                       {
                                                           this.clearColorButton.setColor(color);
                                                           this.settings.set(SettingsWindow.NoDataColorSettingName, SettingsWindow.colorToString(color));
                                                           this.settings.save();
                                                       }
                                                     });
        this.buildOutputContent();

        this.contentPanel.add(this.outputPanel);

        this.buildNavigationPanel();

        this.pack();
    }

    private void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void warn(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName,
                                      JOptionPane.WARNING_MESSAGE);
    }

    private void error(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName,
                                      JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("serial")
    protected class SimpleGridBagConstraints extends GridBagConstraints
    {
        public SimpleGridBagConstraints(final int gridX, final int gridY, final boolean stretch)
        {
            super(gridX, gridY, 1, 1, stretch ? 1 : 0, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        }
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

    private void buildNavigationPanel()
    {
        this.cancelButton.addActionListener(e -> { this.closeFrame(); });

        this.okButton.addActionListener(e -> { try
                                               {
                                                   this.okButton.setEnabled(false);
                                                   this.execute();
                                                   this.closeFrame();
                                               }
                                               catch(final Exception ex)
                                               {
                                                   this.okButton.setEnabled(true);
                                                   ex.printStackTrace();
                                                   this.error("An error has occurred: " + ex.getMessage());
                                               }
                                             });

        // Add buttons to pane
        final Insets insets = new Insets(10, 10, 10, 10);
        final int    fill   = GridBagConstraints.NONE;

        this.navigationPanel.add(this.okButton,     new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
        this.navigationPanel.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
    }

    private void execute() throws Exception
    {
        final int tileWidth  = (int)this.tileWidthSpinner .getValue();
        final int tileHeight = (int)this.tileHeightSpinner.getValue();

        final Color color = this.clearColorButton.getColor();

        // Save UI values - TODO put these in the event handlers
        this.settings.set(SettingsWindow.TileWidthSettingName,  Integer.toString(tileWidth));
        this.settings.set(SettingsWindow.TileHeightSettingName, Integer.toString(tileHeight));

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
}
