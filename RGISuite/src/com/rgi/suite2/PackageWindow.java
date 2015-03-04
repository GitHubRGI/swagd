package com.rgi.suite2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.rgi.packager.Packager;

/**
 * Gather additional information during Packaging workflow.
 *
 * @author Steven D. Lander
 *
 */
public class PackageWindow extends JFrame
{
    private static final long serialVersionUID = -3488202344008846021L;

    private final JPanel navPane;
    private final JPanel contentPane;

    private final JTextField tileSetName;
    private final JTextField tileSetDescription;
    private final JTextField outputFileName;

    public PackageWindow()
    {
        this.setTitle("Packaging Settings");
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(400, 200));
        this.setResizable(false);

        this.contentPane = new JPanel(new GridBagLayout());
        this.navPane     = new JPanel(new GridBagLayout());

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
        // Initial UI values
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        // Add the tile set name elements

        gbc.gridy = 0;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Set Name:"), gbc);
        gbc.weightx = 1;

        this.contentPane.add(this.tileSetName, gbc);

        // Add the tile set description elements

        ++gbc.gridy;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Set Description:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.tileSetDescription, gbc);

        // Add the output geopackage name elements
        final JButton outputFileNameButton = new JButton("\u2026");
        ++gbc.gridy;
        final Insets i = outputFileNameButton.getMargin();
        final Insets j = new Insets(i.top, 1, i.bottom, 1);
        outputFileNameButton.setMargin(j);
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output File Name:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.outputFileName, gbc);
        gbc.weightx = 0;
        this.contentPane.add(outputFileNameButton, gbc);
        outputFileNameButton.addActionListener(e -> { final JFileChooser fc = new JFileChooser();

                                                      fc.setMultiSelectionEnabled(false);
                                                      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

                                                      fc.addChoosableFileFilter(new FileFilter()
                                                                                {
                                                                                    @Override
                                                                                    public String getDescription()
                                                                                    {
                                                                                        return "GeoPackage Files (*.gpkg)";
                                                                                    }

                                                                                    @Override
                                                                                    public boolean accept(final File pathname)
                                                                                    {
                                                                                        return pathname.getName().toLowerCase().endsWith(".gpkg");
                                                                                    }
                                                                                });

                                                      final int option = fc.showOpenDialog(this.contentPane);

                                                      if(option == JFileChooser.APPROVE_OPTION)
                                                      {
                                                          this.outputFileName.setText(fc.getSelectedFile().getPath());
                                                      }
                                                    });
    }

    protected void buildNavPane()
    {
        // Navigation buttons go here.  These will either cancel and return
        // to the main window or continue on to build the geopackage.

        // move to next step
        final JButton okButton = new JButton(new AbstractAction()
                                             {
                                                 private static final long serialVersionUID = -5059914828508260038L;

                                                 @Override
                                                 public void actionPerformed(final ActionEvent event)
                                                 {
                                                     PackageWindow.this.makePackage();
                                                     PackageWindow.this.closeFrame();
                                                 }
                                             });

        okButton.setText("OK");
        //nextButton.setHideActionText(true);

        // cancel packaging workflow
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
        //cancelButton.setHideActionText(true);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        // Add buttons to pane
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        this.navPane.add(cancelButton, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        this.navPane.add(okButton, gbc);
    }

    private void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void makePackage()
    {
//        // TODO: Create new geopackage or append to existing one
//        // Get file/directory from settings
//        final File[] files = opts.getFiles(Setting.FileSelection);
//        // Create a new geopackage file
//        final File gpkgFile = new File(opts.get(Setting.OutputFileName));
//
//        if(gpkgFile.exists())
//        {
//            if(!gpkgFile.delete())
//            {
//                this.fireError(new Exception("Unable to overwrite existing geopackage file: " + gpkgFile.getAbsolutePath()));
//            }
//        }
//
//      try
//      {
//            CrsProfile crsProfile = new SphericalMercatorCrsProfile(); // TODO: this should be the default.
//            switch (opts.get(Setting.CrsProfile))
//            {
//                case "SphericalMercator":
//                case "WebMercator":
//                    crsProfile = new SphericalMercatorCrsProfile();
//                    break;
//                case "WorldMercator":
//                    crsProfile = new EllipsoidalMercatorCrsProfile();
//                    break;
//                case "ScaledWorldMercator":
//                case "Geodetic":
//                case "Raster":
//                default:
//                    // TODO: need to clean up this mess
//                    this.fireError(new Exception("Unsupported output profile or no profile supplied."));
//            }
//
//            // TODO: Figure out what the file selection is and create a reader
//            final TileStoreReader tileStoreReader = new TmsReader(crsProfile, files[0].toPath());
//
//            final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();
//
//            if(zoomLevels.size() == 0)
//            {
//                System.err.println("Input tile store contains no zoom levels");
//                return;
//            }
//
//            final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);
//
//            final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());
//
//            final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
//            final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);
//
//            final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;
//            final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;
//
//            // Create a new geopackage writer with things like table name and description
//            final GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkgFile,
//                                                                     crsProfile.getCoordinateReferenceSystem(),
//                                                                     opts.get(Setting.TileSetName),
//                                                                     opts.get(Setting.TileSetName),
//                                                                     opts.get(Setting.TileSetDescription),
//                                                                     tileStoreReader.getBounds(),
//                                                                     new ZoomTimesTwo(zoomLevelRange.getMinimum(),
//                                                                                      zoomLevelRange.getMaximum(),
//                                                                                      minZoomLevelMatrixWidth,
//                                                                                      minZoomLevelMatrixHeight),
//                                                                     new MimeType("image/png"),
//                                                                     null);

        final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
    }
}
