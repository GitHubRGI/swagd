package com.rgi.suite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.swing.JOptionPane;

import store.GeoPackageWriter;
import utility.TileStoreUtility;
import utility.TileStoreUtility.TileStoreTraits;

import com.rgi.common.Range;
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
public class PackagerWindow extends TileStoreCreationWindow
{
    private static final long serialVersionUID = -6391017242212287246L;

    private static final String LastInputLocationSettingName = "package.lastInputLocation";

    /**
     * Constructor
     *
     * @param settings
     *             Settings used to hint user preferences
     */
    public PackagerWindow(final Settings settings)
    {
        super("Packaging", settings, LastInputLocationSettingName);

        this.okButton.addActionListener(e -> { try
                                               {
                                                   PackagerWindow.this.makePackage();
                                                   PackagerWindow.this.closeFrame();
                                               }
                                               catch(final Exception ex)
                                               {
                                                   ex.printStackTrace();
                                                   JOptionPane.showMessageDialog(PackagerWindow.this, this.processName, "An error has occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                               }
                                             });
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

            final TileScheme tileScheme = PackagerWindow.getRelativeZoomTimesTwoTileScheme(tileStoreReader);

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

    @Override
    protected void inputFileChanged(final File file) throws TileStoreException
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
