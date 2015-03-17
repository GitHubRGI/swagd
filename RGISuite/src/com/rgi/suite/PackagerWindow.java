package com.rgi.suite;

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
        this.pack();
    }

    @Override
    protected void execute(final TileStoreReader tileStoreReader, final TileStoreWriter tileStoreWriter) throws Exception
    {
        final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
        packager.execute();   // TODO monitor errors/progress
    }
}
