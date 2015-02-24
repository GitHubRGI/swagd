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

package com.rgi.geopackage.features;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageFeatures
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param core
     *             Access to GeoPackage's "core" methods
     */
    public GeoPackageFeatures(final Connection databaseConnection, final GeoPackageCore core)
    {
        this.databaseConnection = databaseConnection;
        this.core               = core;
    }

    /**
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return the Feature GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<VerificationIssue> getVerificationIssues(final VerificationLevel verificationLevel)
    {
        return new FeaturesVerifier(this.databaseConnection, verificationLevel).getVerificationIssues();
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
     * @param core the GeoPackage core object
     *
     * @param matchingSpatialReferenceSystem Spatial reference system that returned {@link FeatureSet}s much refer to
     * @return Returns a collection of {@link FeatureSet}s
     * @throws SQLException throws if an SQLException occurs
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
