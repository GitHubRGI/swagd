package com.rgi.suite;

import java.io.File;
import java.util.Collection;

import utility.TileStoreUtility;
import utility.TileStoreUtility.TileStoreTraits;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.FileUtility;


/**
 * Gather additional information for tiling, and tile
 *
 * @author Luke D. Lambert
 *
 */
public class TilerWindow extends TileStoreCreationWindow
{
    private static final long serialVersionUID = -3488202344008846021L;

    private static final String LastInputLocationSettingName = "tiling.lastInputLocation";

    /**
     * Constructor
     * @param settings
     *             Settings used to hint user preferences
     */
    public TilerWindow(final Settings settings)
    {
        super("Tiling", settings, LastInputLocationSettingName);
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

//        this.outputFileName.setText(FileUtility.appendForUnique(String.format("%s%c%s.gpkg",
//                                                                              this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation),
//                                                                              File.separatorChar,
//                                                                              FileUtility.nameWithoutExtension(file))));

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

//        this.tileSetName.setText(name);
//        this.tileSetDescription.setText(String.format("Tile store %s (%s) packaged by %s at %s",
//                                                      name,
//                                                      file.getName(),
//                                                      System.getProperty("user.name"),
//                                                      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())));
    }
}
