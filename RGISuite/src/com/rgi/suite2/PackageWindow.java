package com.rgi.suite2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.swing.AbstractAction;
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
import utility.TileStoreUtility.TileStoreTraits;

import com.rgi.common.Range;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.FileUtility;
import com.rgi.packager.Packager;


/**
 * Gather additional information for packaging, and package
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class PackageWindow extends JFrame
{
    private static final long serialVersionUID = -3488202344008846021L;

    private final Settings settings;

    private final JPanel contentPane = new JPanel(new GridBagLayout());
    private final JPanel navPane     = new JPanel(new GridBagLayout());

    private final JTextField inputFileName      = new JTextField();
    private final JTextField tileSetName        = new JTextField();
    private final JTextField tileSetDescription = new JTextField();
    private final JTextField outputFileName     = new JTextField();

    private final JComboBox<CoordinateReferenceSystem> inputCrs = new JComboBox<>(new DefaultComboBoxModel<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                                              .stream()
                                                                                                                              .sorted()
                                                                                                                              .toArray(CoordinateReferenceSystem[]::new)));

    private static final String LastInputLocationSettingName = "package.lastInputLocation";

    /**
     * Constructor
     * @param settings
     *             Settings used for
     */
    public PackageWindow(final Settings settings)
    {
        this.setTitle("Packaging Settings");
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(600, 240));
        this.setResizable(false);

        this.settings = settings;

        this.buildContentPane();
        this.buildNavPane();

        this.add(this.contentPane, BorderLayout.CENTER);
        this.add(this.navPane,     BorderLayout.SOUTH);
    }

    private void buildContentPane()
    {
        final JButton inputFileNameButton = new JButton("\u2026");

        this.inputFileName.setEnabled(false);
        this.inputCrs     .setEnabled(false);

        inputFileNameButton.addActionListener(e -> { final String startDirectory = PackageWindow.this.settings.get(LastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);

                                                     final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                     fileChooser.setMultiSelectionEnabled(false);
                                                     fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                     final int option = fileChooser.showOpenDialog(this.contentPane);

                                                     if(option == JFileChooser.APPROVE_OPTION)
                                                     {
                                                         final File file = fileChooser.getSelectedFile();

                                                         try
                                                         {
                                                             PackageWindow.this.inputFileChanged(file);
                                                         }
                                                         catch(final Exception ex)
                                                         {
                                                             JOptionPane.showMessageDialog(this,
                                                                                           ex.getMessage(),
                                                                                           "Packaging",
                                                                                           JOptionPane.ERROR_MESSAGE);
                                                         }
                                                     }
                                                    });

        final int    anchor = GridBagConstraints.WEST;
        final int    fill   = GridBagConstraints.HORIZONTAL;
        final Insets insets = new Insets(5, 5, 5, 5);

        // Input tile store file
        this.contentPane.add(new JLabel("Input tile store:"),     new GridBagConstraints(0, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.inputFileName,                  new GridBagConstraints(1, 0, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(inputFileNameButton,                 new GridBagConstraints(2, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));

        // Input CRS
        this.contentPane.add(new JLabel("Input CRS:"),            new GridBagConstraints(0, 1, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.inputCrs,                       new GridBagConstraints(1, 1, 1, 1, 0, 1, anchor, fill, insets, 0, 0));

        // Tile set name
        this.contentPane.add(new JLabel("Tile Set Name:"),        new GridBagConstraints(0, 2, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetName,                    new GridBagConstraints(1, 2, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Tile set description
        this.contentPane.add(new JLabel("Tile Set Description:"), new GridBagConstraints(0, 3, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetDescription,             new GridBagConstraints(1, 3, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Output file name
        this.contentPane.add(new JLabel("Output File Name:"),     new GridBagConstraints(0, 4, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.outputFileName,                 new GridBagConstraints(1, 4, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
    }

    private void buildNavPane()
    {
         final JButton okButton = new JButton(new AbstractAction()
                                             {
                                                 private static final long serialVersionUID = -5059914828508260038L;

                                                 @Override
                                                 public void actionPerformed(final ActionEvent event)
                                                 {
                                                     try
                                                    {
                                                        PackageWindow.this.makePackage();
                                                        PackageWindow.this.closeFrame();
                                                    }
                                                    catch(final Exception ex)
                                                    {
                                                        ex.printStackTrace();
                                                        JOptionPane.showMessageDialog(PackageWindow.this, "Packaging", "An error has occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                                    }
                                                 }
                                             });

        okButton.setText("OK");

        final JButton cancelButton = new JButton(new AbstractAction()
                                                 {
                                                     private static final long serialVersionUID = -4389758606354266920L;

                                                     @Override
                                                     public void actionPerformed(final ActionEvent event)
                                                     {
                                                         PackageWindow.this.closeFrame();
                                                     }
                                                 });

        cancelButton.setText("Cancel");

        // Add buttons to pane
        final Insets insets = new Insets(10, 10, 10, 10);
        final int    fill   = GridBagConstraints.NONE;

        this.navPane.add(okButton,     new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
        this.navPane.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
    }


    private void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void makePackage() throws Exception
    {
        final Collection<TileStoreReader> readers = TileStoreUtility.getStores(null, new File(this.inputFileName.getText()));   // TODO !!IMPORTANT!! need to pick the crs

        if(readers.isEmpty())
        {
            throw new TileStoreException("File contains no recognized file store types.");
        }

        // TODO handle multiple readers?
        try(final TileStoreReader tileStoreReader = readers.iterator().next())
        {
            final File gpkgFile = new File(this.outputFileName.getText());

            final TileScheme tileScheme = PackageWindow.getRelativeZoomTimesTwoTileScheme(tileStoreReader);

            final MimeType mimeType = new MimeType("image/" + this.settings.get(SettingsWindow.OutputImageFormatSettingName, SettingsWindow.DefaultOutputCrs)); // TODO get from UI?

            try(final TileStoreWriter tileStoreWriter = new GeoPackageWriter(gpkgFile,
                                                                             tileStoreReader.getCoordinateReferenceSystem(),
                                                                             this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                             this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                             this.tileSetDescription.getText(),
                                                                             tileStoreReader.getBounds(),
                                                                             tileScheme,
                                                                             mimeType,
                                                                             null))                         // TODO use user preferences
            {
                final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
                packager.execute();   // TODO monitor errors/progress
            }
        }
    }

    private void inputFileChanged(final File file) throws TileStoreException
    {
        this.settings.set(LastInputLocationSettingName, file.getParent());
        this.settings.save();

        final TileStoreTraits traits = TileStoreUtility.getTraits(file);

        if(traits == null)
        {
            throw new TileStoreException(String.format("%s is not a recognized tile store format.",
                                                       file.isDirectory() ? "Folder" : "File"));
        }

        this.inputCrs.setEnabled(!traits.knowsCrs());

        this.outputFileName.setText(FileUtility.appendForUnique(String.format("%s%c%s.gpkg",
                                                                              this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation),
                                                                              File.separatorChar,
                                                                              FileUtility.nameWithoutExtension(file))));

        this.inputFileName.setText(file.getPath());

        String name = FileUtility.nameWithoutExtension(file);

        if(traits.knowsCrs())
        {
            final Collection<TileStoreReader> readers = TileStoreUtility.getStores(null, file);

            if(readers.isEmpty())
            {
                throw new TileStoreException(String.format("%s contains no tile sets.",
                                                           file.isDirectory() ? "Folder" : "File"));
            }

            // TODO handle multiple readers?
            // TODO store TileStoreReaders so we don't recreate them later?
            try(final TileStoreReader tileStoreReader = readers.iterator().next())
            {
                // TODO if the store contains an unrecognized CRS the combo box won't change
                this.inputCrs.setSelectedItem(tileStoreReader.getCoordinateReferenceSystem());

                name = tileStoreReader.getName();
            }
            catch(final Exception ex)
            {
               // Only thrown by the automatic .close() call of the TileStoreReader
            }
        }

        this.tileSetName.setText(name);
        this.tileSetDescription.setText(String.format("Tile store %s (%s) packaged by %s at %s",
                                                      name,
                                                      file.getName(),
                                                      System.getProperty("user.name"),
                                                      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())));
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
