/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.erdc.gpkg.features;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.rgi.erdc.gpkg.core.GeoPackageCore;
import com.rgi.erdc.gpkg.core.SpatialReferenceSystem;
import com.rgi.erdc.gpkg.verification.FailedRequirement;

public class GeoPackageFeatures
{
    public GeoPackageFeatures(final Connection databaseConnection, final GeoPackageCore core)
    {
        this.databaseConnection = databaseConnection;
        this.core               = core;
    }

    /**
     * @return the Feature GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements()
    {
        return new FeaturesVerifier(this.databaseConnection).getFailedRequirements();
    }

    @SuppressWarnings("unused")
    private void createFeaturesTables() throws SQLException
    {
        // TODO
        try(Statement statement = this.databaseConnection.createStatement())
        {
            // TODO
        }
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "features" data_type that also match the supplied spatial reference system
     *
     * @param matchingSpatialReferenceSystem Spatial reference system that returned {@link FeatureSet}s much refer to
     * @return Returns a collection of {@link FeatureSet}s
     * @throws SQLException
     */
    public static Collection<FeatureSet> getFeatureSets(final GeoPackageCore core, final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        return core.getContent(FeatureSet.FeatureContentType,
                               (tableName, dataType, identifier, description, lastChange, boundingBox, spatialReferenceSystem) -> new FeatureSet(tableName, identifier, description, lastChange, boundingBox, spatialReferenceSystem),
                               matchingSpatialReferenceSystem);
    }

    private final Connection     databaseConnection;
    @SuppressWarnings("unused")
    private final GeoPackageCore core;
}
