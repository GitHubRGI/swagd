package com.rgi.suite;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import store.GeoPackageWriter;
import utility.TileStoreUtility;

import com.rgi.common.Range;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.tile.store.tms.TmsWriter;


/**
 * Gather additional information for packaging, and package
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public abstract class TileStoreCreationWindow extends JFrame
{
    private static final long serialVersionUID = -3488202344008846021L;

    protected final Settings settings;

    protected final JPanel contentPane = new JPanel(new GridBagLayout());
    protected final JPanel navPane     = new JPanel(new GridBagLayout());

    protected final JTextField inputFileName      = new JTextField();
    protected final JTextField tileSetName        = new JTextField();
    protected final JTextField tileSetDescription = new JTextField();
    protected final JTextField outputFileName     = new JTextField();
    protected final JButton    okButton           = new JButton("OK");
    protected final JButton    cancelButton       = new JButton("Cancel");

    protected final JComboBox<CoordinateReferenceSystem> inputCrs = new JComboBox<>(new DefaultComboBoxModel<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                                                .stream()
                                                                                                                                .sorted()
                                                                                                                                .toArray(CoordinateReferenceSystem[]::new)));

    protected static final String[] StoreTypes = new String[]{ "GeoPackage", "TMS" };

    // TODO: stop using strings for this combo box type
    protected final JComboBox<String> outputStoreType = new JComboBox<>(new DefaultComboBoxModel<>(StoreTypes));

    protected final String lastInputLocationSettingName;
    protected final String processName;

    /**
     * Constructor
     *
     * @param processName
     *             Concrete implementation's functional name (e.g. Packaging, Tiling, etc)
     * @param settings
     *             Settings used to hint user preferences
     * @param lastInputLocationSettingName
     *             The name of the setting to use to read/store the last input location
     */
    public TileStoreCreationWindow(final String processName, final Settings settings, final String lastInputLocationSettingName)
    {
        this.processName = processName;

        this.setTitle(processName + " Settings");
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(600, 300));
        this.setResizable(false);

        this.settings = settings;
        this.lastInputLocationSettingName = lastInputLocationSettingName;

        this.buildContentPane();
        this.buildNavPane();

        this.add(this.contentPane, BorderLayout.CENTER);
        this.add(this.navPane,     BorderLayout.SOUTH);
    }

    protected abstract void inputFileChanged(final File file) throws Exception;

    protected abstract void execute(final TileStoreReader tileStoreReader, final TileStoreWriter tileStoreWriter) throws Exception;

    private void buildNavPane()
    {
        this.cancelButton.addActionListener(e -> { TileStoreCreationWindow.this.closeFrame(); });

        this.okButton.addActionListener(e -> { try
                                               {
                                                   TileStoreCreationWindow.this.execute();
                                                   TileStoreCreationWindow.this.closeFrame();
                                               }
                                               catch(final Exception ex)
                                               {
                                                   ex.printStackTrace();
                                                   JOptionPane.showMessageDialog(TileStoreCreationWindow.this, this.processName, "An error has occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                               }
                                             });

        // Add buttons to pane
        final Insets insets = new Insets(10, 10, 10, 10);
        final int    fill   = GridBagConstraints.NONE;

        this.navPane.add(this.okButton,     new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
        this.navPane.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
    }

    protected String getLastInputLocation()
    {
        return TileStoreCreationWindow.this.settings.get(this.lastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);
    }

    protected void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    protected void outputStoreTypeChanged()
    {
        // TODO eventually we won't be using strings to pick the output type
        final String outputType = (String)this.outputStoreType.getSelectedItem();

        if(outputType.equals(StoreTypes[0]))    // GeoPackage
        {
            this.tileSetName       .setEnabled(true);
            this.tileSetDescription.setEnabled(true);
        }
        else if(outputType.equals(StoreTypes[1]))   // TMS
        {
            this.tileSetName       .setEnabled(false);
            this.tileSetDescription.setEnabled(false);

            this.tileSetName       .setText("");
            this.tileSetDescription.setText("");
        }
    }

    private void buildContentPane()
    {
        final JButton inputFileNameButton = new JButton("\u2026");

        this.inputFileName.setEditable(false);
        this.inputCrs     .setEditable(false);

        inputFileNameButton.addActionListener(e -> { final String startDirectory = TileStoreCreationWindow.this.settings.get(this.lastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);

                                                     final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                     fileChooser.setMultiSelectionEnabled(false);
                                                     fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                     final int option = fileChooser.showOpenDialog(this.contentPane);

                                                     if(option == JFileChooser.APPROVE_OPTION)
                                                     {
                                                         final File file = fileChooser.getSelectedFile();

                                                         this.settings.set(this.lastInputLocationSettingName, file.getParent());
                                                         this.settings.save();

                                                         try
                                                         {
                                                             TileStoreCreationWindow.this.inputFileChanged(file);
                                                         }
                                                         catch(final Exception ex)
                                                         {
                                                             JOptionPane.showMessageDialog(this,
                                                                                           ex.getMessage(),
                                                                                           this.processName,
                                                                                           JOptionPane.ERROR_MESSAGE);
                                                         }
                                                     }
                                                   });

        this.outputStoreType.addActionListener(e -> { TileStoreCreationWindow.this.outputStoreTypeChanged(); });

        final int    anchor = GridBagConstraints.WEST;
        final int    fill   = GridBagConstraints.HORIZONTAL;
        final Insets insets = new Insets(5, 5, 5, 5);

        // Input tile store file
        this.contentPane.add(new JLabel("Input file:"),         new GridBagConstraints(0, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.inputFileName,                new GridBagConstraints(1, 0, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(inputFileNameButton,               new GridBagConstraints(2, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));

        // Input CRS
        this.contentPane.add(new JLabel("Input CRS:"),          new GridBagConstraints(0, 1, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.inputCrs,                     new GridBagConstraints(1, 1, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Output tile store type
        this.contentPane.add(new JLabel("Output store type:"),  new GridBagConstraints(0, 2, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.outputStoreType,              new GridBagConstraints(1, 2, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Tile set name
        this.contentPane.add(new JLabel("Output name:"),        new GridBagConstraints(0, 3, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetName,                  new GridBagConstraints(1, 3, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Tile set description
        this.contentPane.add(new JLabel("Output description:"), new GridBagConstraints(0, 4, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetDescription,           new GridBagConstraints(1, 4, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Output file name
        this.contentPane.add(new JLabel("Output filename:"),    new GridBagConstraints(0, 5, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.outputFileName,               new GridBagConstraints(1, 5, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
    }

    private void execute() throws Exception
    {
        final File file = new File(this.outputFileName.getText());

        // TODO handle multiple readers?
        try(final TileStoreReader tileStoreReader = this.getReaders().iterator().next())
        {
            final MimeType mimeType = new MimeType("image/" + this.settings.get(SettingsWindow.OutputImageFormatSettingName, SettingsWindow.DefaultOutputCrs)); // TODO get from UI?

            // TODO eventually we won't be using strings to pick the output type
            final String outputType = (String)this.outputStoreType.getSelectedItem();

            if(outputType.equals(StoreTypes[0]))    // GeoPackage
            {


                final TileScheme tileScheme = getRelativeZoomTimesTwoTileScheme(tileStoreReader);

                try(final TileStoreWriter tileStoreWriter = new GeoPackageWriter(file,
                                                                                 tileStoreReader.getCoordinateReferenceSystem(),
                                                                                 this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                                 this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                                 this.tileSetDescription.getText(),
                                                                                 tileStoreReader.getBounds(),
                                                                                 tileScheme,
                                                                                 mimeType,
                                                                                 null))                         // TODO use user preferences
                {
                    this.execute(tileStoreReader, tileStoreWriter);
                }
            }
            else if(outputType.equals(StoreTypes[1]))   // TMS
            {
                try(TileStoreWriter tileStoreWriter = new TmsWriter((CoordinateReferenceSystem)this.inputCrs.getSelectedItem(),
                                                                    file.toPath(),
                                                                    mimeType,
                                                                    null))     // TODO image write params
                {
                    this.execute(tileStoreReader, tileStoreWriter);
                }
            }
        }
    }

    private Collection<TileStoreReader> getReaders() throws TileStoreException
    {
        final Collection<TileStoreReader> readers = TileStoreUtility.getStores(null, new File(this.inputFileName.getText()));   // TODO !!IMPORTANT!! need to pick the crs

        if(readers.isEmpty())
        {
            throw new TileStoreException("File contains no recognized file store types.");
        }

        return readers;
    }

    private static TileScheme getRelativeZoomTimesTwoTileScheme(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();

        if(zoomLevels.size() == 0)
        {
            throw new TileStoreException("Input tile store contains no zoom levels");
        }

        final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);

        final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());

        final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
        final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);

        final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;
        final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;

        return new ZoomTimesTwo(zoomLevelRange.getMinimum(),
                                zoomLevelRange.getMaximum(),
                                minZoomLevelMatrixWidth,
                                minZoomLevelMatrixHeight);
    }
}