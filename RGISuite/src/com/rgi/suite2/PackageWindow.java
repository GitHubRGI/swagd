package com.rgi.suite2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
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

import com.rgi.common.Range;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
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

    private final JPanel navPane;
    private final JPanel contentPane;

    private final JTextField inputFileName;
    private final JComboBox  inputCrs;
    private final JTextField tileSetName;
    private final JTextField tileSetDescription;
    private final JTextField outputFileName;

    /**
     * Constructor
     */
    public PackageWindow()
    {
        this.setTitle("Packaging Settings");
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(400, 200));
        this.setResizable(false);

        this.contentPane = new JPanel(new GridBagLayout());
        this.navPane     = new JPanel(new GridBagLayout());

        this.inputFileName      = new JTextField();
        this.inputCrs           = new JComboBox<>(new DefaultComboBoxModel<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                              .stream()
                                                                                              .toArray(CoordinateReferenceSystem[]::new)));
        this.tileSetName        = new JTextField();
        this.tileSetDescription = new JTextField();
        this.outputFileName     = new JTextField();

        this.buildContentPane();
        this.buildNavPane();

        this.add(this.contentPane, BorderLayout.CENTER);
        this.add(this.navPane,     BorderLayout.SOUTH);
    }

    private void buildContentPane()
    {


        final JButton inputFileNameButton = new JButton("\u2026");

        inputFileNameButton.addActionListener(e -> { final JFileChooser fileChooser = new JFileChooser();

                                                      fileChooser.setMultiSelectionEnabled(false);
                                                      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                      final int option = fileChooser.showOpenDialog(this.contentPane);

                                                      if(option == JFileChooser.APPROVE_OPTION)
                                                      {
                                                          this.inputFileName.setText(fileChooser.getSelectedFile().getPath());

                                                          // SET READER / INPUT CRS (BOTH IF HINT NEEDED OR REAL)
                                                      }
                                                    });

        final int    anchor = GridBagConstraints.WEST;
        final int    fill   = GridBagConstraints.HORIZONTAL;
        final Insets insets = new Insets(5, 5, 5, 5);

        // Input tile store file
        this.contentPane.add(new JLabel("Input tile store:"),     new GridBagConstraints(0, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.inputFileName,                  new GridBagConstraints(1, 0, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(inputFileNameButton,                 new GridBagConstraints(2, 0, 1, 1, 0, 1, anchor, fill, insets, 0, 0));

        // Tile set name
        this.contentPane.add(new JLabel("Tile Set Name:"),        new GridBagConstraints(0, 1, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetName,                    new GridBagConstraints(1, 1, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Tile set description
        this.contentPane.add(new JLabel("Tile Set Description:"), new GridBagConstraints(0, 2, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.tileSetDescription,             new GridBagConstraints(1, 2, 1, 1, 1, 1, anchor, fill, insets, 0, 0));

        // Output file name
        this.contentPane.add(new JLabel("Output File Name:"),     new GridBagConstraints(0, 3, 1, 1, 0, 1, anchor, fill, insets, 0, 0));
        this.contentPane.add(this.outputFileName,                 new GridBagConstraints(1, 3, 1, 1, 1, 1, anchor, fill, insets, 0, 0));
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
            JOptionPane.showMessageDialog(this, "Packaging", "File contains no recognized file store types.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO handle multiple readers?
        try(final TileStoreReader tileStoreReader = readers.iterator().next())
        {
            final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();

            if(zoomLevels.size() == 0)
            {
                JOptionPane.showMessageDialog(this, "Packaging", "Input tile store contains no zoom levels", JOptionPane.WARNING_MESSAGE);
                return;
            }

            final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);

            final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());

            final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
            final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);

            final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;
            final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;

            final File gpkgFile = new File(this.outputFileName.getText());  // TODO !!IMPORTANT!! append the user prefs for output directory

            try(final TileStoreWriter tileStoreWriter = new GeoPackageWriter(gpkgFile,
                                                                             tileStoreReader.getCoordinateReferenceSystem(),
                                                                             this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                             this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                                                             this.tileSetDescription.getText(),
                                                                             tileStoreReader.getBounds(),
                                                                             new ZoomTimesTwo(zoomLevelRange.getMinimum(),
                                                                                              zoomLevelRange.getMaximum(),
                                                                                              minZoomLevelMatrixWidth,
                                                                                              minZoomLevelMatrixHeight),
                                                                             new MimeType("image/png"),                     // TODO use user prefs
                                                                             null))                                         // TODO use user prefs
            {
                final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
                packager.execute(); // TODO monitor errors/progress
            }
        }
    }
}
