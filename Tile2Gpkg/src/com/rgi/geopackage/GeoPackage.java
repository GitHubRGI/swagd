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

package com.rgi.geopackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.features.GeoPackageFeatures;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.FailedRequirement;
import com.rgi.geopackage.verification.Severity;

/**
 * Implementation of the <a href="http://www.geopackage.org/spec/">OGC GeoPackage specification</a>
 *
 * @author Luke Lambert
 *
 */
public class GeoPackage implements AutoCloseable
{
    /**
     * @param file
     *          Location on disk that represents where an existing GeoPackage will opened and/or created
     * @throws ClassNotFoundException
     *          when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *          when the verifyConformance parameter is true, and if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}
     * @throws FileAlreadyExistsException
     *          when openMode is set to OpenMode.Create, and the file already exists
     * @throws FileNotFoundException
     *          when openMode is set to OpenMode.Open, and the file does not exist
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file) throws ClassNotFoundException, SQLException, ConformanceException, FileAlreadyExistsException, FileNotFoundException
    {
        this(file, true, OpenMode.OpenOrCreate);
    }

    /**
     * @param file
     *          Location on disk that represents where an existing GeoPackage will opened and/or created
     * @param verifyConformance
     *          Indicates whether {@link #verify()} should be called
     *          automatically.  If verifyConformance is true and
     *          {@link #verify()} is called automatically, it will throw if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}.  Throwing from this method means that
     *          it won't be possible to instantiate a GeoPackage object based
     *          on an SQLite "GeoPackage" file with severe errors.
     * @throws ClassNotFoundException
     *          when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *          when the verifyConformance parameter is true, and if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}
     * @throws FileAlreadyExistsException
     *          when openMode is set to OpenMode.Create, and the file already exists
     * @throws FileNotFoundException
     *          when openMode is set to OpenMode.Open, and the file does not exist
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file, final boolean verifyConformance) throws ClassNotFoundException, SQLException, ConformanceException, FileAlreadyExistsException, FileNotFoundException
    {
        this(file, verifyConformance, OpenMode.OpenOrCreate);
    }

    /**
     * @param file
     *          Location on disk that represents where an existing GeoPackage will opened and/or created
     * @param openMode
     *          Controls the file creation/opening behavior
     * @throws ClassNotFoundException
     *          when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *          when the verifyConformance parameter is true, and if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}
     * @throws FileAlreadyExistsException
     *          when openMode is set to OpenMode.Create, and the file already exists
     * @throws FileNotFoundException
     *          when openMode is set to OpenMode.Open, and the file does not exist
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file, final OpenMode openMode) throws ClassNotFoundException, SQLException, ConformanceException, FileAlreadyExistsException, FileNotFoundException
    {
        this(file, true, openMode);
    }

    /**
     * @param file
     *          Location on disk that represents where an existing GeoPackage will opened and/or created
     * @param verifyConformance
     *          Indicates whether {@link #verify()} should be called
     *          automatically.  If verifyConformance is true and
     *          {@link #verify()} is called automatically, it will throw if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}.  Throwing from this method means that
     *          it won't be possible to instantiate a GeoPackage object based
     *          on an SQLite "GeoPackage" file with severe errors.
     * @param openMode
     *          Controls the file creation/opening behavior
     * @throws ClassNotFoundException
     *          when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *          when the verifyConformance parameter is true, and if
     *          there are any conformance violations with the severity
     *          {@link Severity#Error}
     * @throws FileAlreadyExistsException
     *          when openMode is set to OpenMode.Create, and the file already exists
     * @throws FileNotFoundException
     *          when openMode is set to OpenMode.Open, and the file does not exist
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file, final boolean verifyConformance, final OpenMode openMode) throws ClassNotFoundException, ConformanceException, FileAlreadyExistsException, FileNotFoundException, SQLException
    {
        if(file == null)
        {
            throw new IllegalArgumentException("file is null");
        }

        final boolean isNewFile = !file.exists();

        if(openMode == OpenMode.Create && !isNewFile)
        {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }

        if(openMode == OpenMode.Open && isNewFile)
        {
           throw new FileNotFoundException(String.format("%s does not exist", file.getAbsolutePath()));
        }

        this.file = file;

        Class.forName("org.sqlite.JDBC");   // Register the driver

        this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath()); // Initialize the database connection

        try
        {
            this.databaseConnection.setAutoCommit(false);

            DatabaseUtility.setPragmaForeignKeys(this.databaseConnection, true);

            this.core       = new GeoPackageCore      (this.databaseConnection);
            this.extensions = new GeoPackageExtensions(this.databaseConnection);
            this.tiles      = new GeoPackageTiles     (this.databaseConnection, this.core);
            this.features   = new GeoPackageFeatures  (this.databaseConnection, this.core);

            if(isNewFile)
            {
                DatabaseUtility.setApplicationId(this.databaseConnection,
                                                 ByteBuffer.wrap(GeoPackage.GeoPackageSqliteApplicationId)
                                                           .asIntBuffer()
                                                           .get());
                this.databaseConnection.commit();

                this.core.createDefaultTables();
            }

            if(verifyConformance)
            {
                this.verify();
            }
        }
        catch(final SQLException | ConformanceException ex)
        {
            if(this.databaseConnection != null && this.databaseConnection.isClosed() == false)
            {
                this.databaseConnection.close();
            }

            // If anything goes wrong, clean up the new file that may have been created
            if(isNewFile && file.exists())
            {
                file.delete();
            }

            throw ex;
        }
    }

    /**
     * The file associated with this GeoPackage
     *
     * @return the file
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * The version of the SQLite database associated with this GeoPackage
     *
     * @return the sqliteVersion
     * @throws IOException if the database file backing this GeoPackage cannot be opened or read from
     */
    public String getSqliteVersion() throws IOException
    {
        if(this.sqliteVersion == null)
        {
            this.sqliteVersion = DatabaseUtility.getSqliteVersion(this.file);
        }
        return this.sqliteVersion;
    }

    /**
     * The application id of the SQLite database
     *
     * @return Returns the application id of the SQLite database.  For a GeoPackage, by its standard, this must be 0x47503130 ("GP10" in ASCII)
     * @throws SQLException Throws if there is an SQL error
     */
    public int getApplicationId() throws SQLException
    {
        return DatabaseUtility.getApplicationId(this.databaseConnection);
    }

    /**
     * Closes the connection to the underlying SQLite database file
     *
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException
    {
        if(this.databaseConnection            != null &&
           this.databaseConnection.isClosed() == false)
        {
            this.databaseConnection.rollback(); // When Connection.close() is called, pending transactions are either automatically committed or rolled back depending on implementaiton defined behavior.  Make the call explicitly to avoid relying on implementaiton defined behavior.
            this.databaseConnection.close();
        }
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @return the GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements() throws SQLException
    {
        final List<FailedRequirement> failedRequirements = new ArrayList<>();

        failedRequirements.addAll(this.core      .getFailedRequirements(this.file));
        failedRequirements.addAll(this.extensions.getFailedRequirements());
        failedRequirements.addAll(this.tiles     .getFailedRequirements());
        failedRequirements.addAll(this.features  .getFailedRequirements());

        return failedRequirements;
    }

    /**
     * Verifies that the GeoPackage meets the core requirements of the GeoPackage specification
     *
     * @throws ConformanceException
     */
    public void verify() throws ConformanceException, SQLException
    {
        //final long startTime = System.nanoTime();

        final Collection<FailedRequirement> failedRequirements = this.getFailedRequirements();

        //System.out.println(String.format("GeoPackage took %.2f seconds to verify.", (System.nanoTime() - startTime)/1.0e9));

        if(failedRequirements.stream().anyMatch(failedRequirement -> failedRequirement.getRequirement().severity() == Severity.Error))
        {
            throw new ConformanceException(failedRequirements);
        }

        if(failedRequirements.size() > 0)
        {
            System.err.println(String.format("GeoPackage failed to meet the following requirements:\n %s",
                                             failedRequirements.stream()
                                                               .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                                                               .map(failedRequirement -> String.format("(%s) Requirement %d: \"%s\"\n%s",
                                                                                                       failedRequirement.getRequirement().severity(),
                                                                                                       failedRequirement.getRequirement().number(),
                                                                                                       failedRequirement.getRequirement().text(),
                                                                                                       failedRequirement.getReason()))
                                                               .collect(Collectors.joining("\n"))));
        }
    }

    /**
     * Access to GeoPackage's "core" functionality
     *
     * @return returns a handle to a GeoPackageCore object
     */
    public GeoPackageCore core()
    {
        return this.core;
    }

    /**
     * Access to GeoPackage's "extensions" functionality
     *
     * @return returns a handle to a GeoPackageExetensions object
     */
    public GeoPackageExtensions extensions()
    {
        return this.extensions;
    }

    /**
     * Access to GeoPackage's "tiles" functionality
     *
     * @return returns a handle to a GeoPackageTiles object
     */
    public GeoPackageTiles tiles()
    {
        return this.tiles;
    }

    /**
     * Access to GeoPackage's "features" functionality
     *
     * @return returns a handle to a GeoPackageFeatures object
     */
    public GeoPackageFeatures features()
    {
        return this.features;
    }

    public enum OpenMode
    {
        OpenOrCreate,
        Open,
        Create
    }

    private final File                 file;
    private final Connection           databaseConnection;
    private final GeoPackageCore       core;
    private final GeoPackageExtensions extensions;
    private final GeoPackageTiles      tiles;
    private final GeoPackageFeatures   features;

    private String sqliteVersion;

    private final static byte[] GeoPackageSqliteApplicationId = new byte[] {'G', 'P', '1', '0' };
}
