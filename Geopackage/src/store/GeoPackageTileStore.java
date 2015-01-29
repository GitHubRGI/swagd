

package store;

import java.sql.SQLException;

import com.rgi.common.coordinates.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinates.referencesystem.profile.CrsProfileFactory;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.TileSet;

/**
 * @author Luke Lambert
 *
 */
abstract class GeoPackageTileStore
{
    protected GeoPackageTileStore(final GeoPackage geoPackage,
                                  final TileSet    tileSet) throws SQLException
    {
        if(geoPackage == null)
        {
            throw new IllegalArgumentException("GeoPackage may not be null");
        }

        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null or empty");
        }

        final SpatialReferenceSystem srs = geoPackage.core().getSpatialReferenceSystem(tileSet.getSpatialReferenceSystemIdentifier());

        this.geoPackage  = geoPackage;
        this.tileSet     = tileSet;
        if(srs == null)
        {
        	throw new IllegalArgumentException("SRS may not be null or empty");
        }
        this.crsProfile = CrsProfileFactory.create(srs.getOrganization(), srs.getOrganizationSrsId());
    }

    protected final GeoPackage  geoPackage;
    protected final TileSet     tileSet;
    protected final CrsProfile crsProfile;

}
