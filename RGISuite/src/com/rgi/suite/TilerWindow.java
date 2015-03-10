package com.rgi.suite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.store.TileStoreException;
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
    protected void execute() throws Exception
    {
        //final Tiler tiler = new Tiler();
        //tiler.execute();
    }

    @Override
    protected void inputFileChanged(final File file) throws TileStoreException
    {
        this.inputFileName.setText(file.getPath());

        final CoordinateReferenceSystem crs = TilerWindow.getCrs(file);

        this.inputCrs.setEditable(crs == null);

        if(crs != null)
        {
            this.inputCrs.setSelectedItem(crs); // TODO if the store contains an unrecognized CRS the combo box won't change
        }

        this.outputFileName.setText(FileUtility.appendForUnique(String.format("%s%c%s.gpkg",
                                                                              this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation),
                                                                              File.separatorChar,
                                                                              FileUtility.nameWithoutExtension(file))));

        final String name = FileUtility.nameWithoutExtension(file);

        this.tileSetName.setText(name);
        this.tileSetDescription.setText(String.format("Tile store %s (%s) packaged by %s at %s",
                                                      name,
                                                      file.getName(),
                                                      System.getProperty("user.name"),
                                                      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())));
    }

    private static CoordinateReferenceSystem getCrs(final File file) throws RuntimeException
    {
        osr.UseExceptions(); // TODO only do this once
        gdal.AllRegister();  // TODO only do this once

        final Dataset dataset = gdal.Open(file.getAbsolutePath(),
                                          gdalconstConstants.GA_ReadOnly);

        if(dataset == null)
        {
            return null;
        }

        final SpatialReference srs = new SpatialReference(dataset.GetProjection());

        gdal.GDALDestroyDriverManager(); // TODO only do this once

        final String attributePath = "PROJCS|GEOGCS|AUTHORITY";

        final String authority  = srs.GetAttrValue(attributePath, 0);
        final String identifier = srs.GetAttrValue(attributePath, 1);

        if(authority == null || identifier == null)
        {
            return null;    // Failed to get the attribute value for some reason, see: http://gdal.org/java/org/gdal/osr/SpatialReference.html#GetAttrValue(java.lang.String,%20int)
        }

        try
        {
            return new CoordinateReferenceSystem(authority, Integer.parseInt(identifier));
        }
        catch(final NumberFormatException ex)
        {
            return null;    // The authority identifier in the WKT wasn't an integer
        }
    }
}
