package com.rgi.suite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import utility.TileStoreUtility;
import utility.TileStoreUtility.TileStoreTraits;

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
        this.buildUi();
    }

    @Override
    protected void execute(final TileStoreReader tileStoreReader, final TileStoreWriter tileStoreWriter) throws Exception
    {
        final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
        packager.execute();   // TODO monitor errors/progress
    }

    @Override
    protected void inputFileChanged(final File file) throws TileStoreException
    {
        final TileStoreTraits traits = TileStoreUtility.getTraits(file);

        if(traits == null)
        {
            throw new TileStoreException(String.format("%s is not a recognized tile store format.",
                                                       file.isDirectory() ? "Folder" : "File"));
        }

        this.inputCrs.setEditable(!traits.knowsCrs());

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
}
