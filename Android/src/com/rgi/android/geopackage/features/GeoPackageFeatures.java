/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.android.geopackage.features;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.geopackage.core.ContentFactory;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.core.SpatialReferenceSystem;
import com.rgi.android.geopackage.verification.VerificationIssue;
import com.rgi.android.geopackage.verification.VerificationLevel;

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
        final Statement statement = this.databaseConnection.createStatement();

        // TODO
        try
        {
            // TODO
        }
        finally
        {
            statement.close();
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
                               new ContentFactory<FeatureSet>()
                               {
                                   @Override
                                   public FeatureSet create(final String inTableName, final String dataType, final String identifier, final String description, final String lastChange, final BoundingBox boundingBox, final Integer spatialReferenceSystemIdentifier)
                                   {
                                       return new FeatureSet(inTableName, identifier, description, lastChange, boundingBox, spatialReferenceSystemIdentifier);
                                   }
                               },
                               matchingSpatialReferenceSystem);
    }

    private final Connection     databaseConnection;
    @SuppressWarnings("unused")
    private final GeoPackageCore core;
}
