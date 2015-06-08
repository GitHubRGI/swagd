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
package com.rgi.android.geopackage;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.extensions.GeoPackageExtensions;
import com.rgi.android.geopackage.features.GeoPackageFeatures;
import com.rgi.android.geopackage.metadata.GeoPackageMetadata;
import com.rgi.android.geopackage.schema.GeoPackageSchema;
import com.rgi.android.geopackage.tiles.GeoPackageTiles;
import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.verification.ConformanceException;
import com.rgi.android.geopackage.verification.Severity;
import com.rgi.android.geopackage.verification.VerificationIssue;
import com.rgi.android.geopackage.verification.VerificationLevel;

/**
 * Implementation of the <a href="http://www.geopackage.org/spec/">OGC GeoPackage specification</a>
 *
 * @author Luke Lambert
 *
 */
public class GeoPackage implements Closeable
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
     * @throws IOException
     *          when openMode is set to OpenMode.Create, and the file already
     *          exists, openMode is set to OpenMode.Open, and the file does not
     *          exist, or if there is a file read error
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        this(file, VerificationLevel.Fast, OpenMode.OpenOrCreate);
    }

    /**
     * @param file
     *             Location on disk that represents where an existing GeoPackage will opened and/or created
     * @param verificationLevel
     *             Controls the level of verification testing performed on this
     *             GeoPackage.  If verificationLevel is not None
     *             {@link #verify()} is called automatically and will throw if
     *             there are any conformance violations with the severity
     *             {@link Severity#Error}.  Throwing from this method means
     *             that it won't be possible to instantiate a GeoPackage object
     *             based on an SQLite "GeoPackage" file with severe errors.
     * @throws ClassNotFoundException
     *             When the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *             When the verifyConformance parameter is true, and if
     *             there are any conformance violations with the severity
     *             {@link Severity#Error}
     * @throws IOException
     *             When openMode is set to OpenMode.Create, and the file
     *             already exists, openMode is set to OpenMode.Open, and the
     *             file does not exist, or if there is a file read error
     * @throws SQLException
     *             In various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file, final VerificationLevel verificationLevel) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        this(file, verificationLevel, OpenMode.OpenOrCreate);
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
     * @throws IOException
     *          when openMode is set to OpenMode.Create, and the file already
     *          exists, openMode is set to OpenMode.Open, and the file does not
     *          exist, or if there is a file read error
     * @throws SQLException
     *          in various cases where interaction with the JDBC connection fails
     */
    public GeoPackage(final File file, final OpenMode openMode) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        this(file, VerificationLevel.Fast, openMode);
    }

    /**
     * @param file
     *            Location on disk that represents where an existing GeoPackage
     *            will opened and/or created
     * @param verificationLevel
     *            Indicates whether {@link #verify()} should be called
     *            automatically. If verifyConformance is true and
     *            {@link #verify()} is called automatically, it will throw if
     *            there are any conformance violations with the severity
     *            {@link Severity#Error}. Throwing from this method means that
     *            it won't be possible to instantiate a GeoPackage object based
     *            on an SQLite "GeoPackage" file with severe errors.
     * @param openMode
     *            Controls the file creation/opening behavior
     * @throws ClassNotFoundException
     *             when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *             when the verifyConformance parameter is true, and if there
     *             are any conformance violations with the severity
     *             {@link Severity#Error}
     * @throws IOException
     *             when openMode is set to OpenMode.Create, and the file already
     *             exists, openMode is set to OpenMode.Open, and the file does
     *             not exist, or if there is a file read error
     * @throws FileNotFoundException
     *             when openMode is set to OpenMode.Open, and the file does not
     *             exist
     * @throws SQLException
     *             in various cases where interaction with the JDBC connection
     *             fails
     */
    public GeoPackage(final File file, final VerificationLevel verificationLevel, final OpenMode openMode) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        if(file == null)
        {
            throw new IllegalArgumentException("File may not be null");
        }

        if(verificationLevel == null)
        {
            throw new IllegalArgumentException("Verification level may not be null");
        }

        if(openMode == null)
        {
            throw new IllegalArgumentException("Open mode may not be null");
        }

        final boolean isNewFile = !file.exists();

        if(openMode == OpenMode.Create && !isNewFile)
        {
            throw new IOException("File already exists: " + file.getAbsolutePath());
        }

        if(openMode == OpenMode.Open && isNewFile)
        {
           throw new FileNotFoundException(String.format("%s does not exist", file.getAbsolutePath()));
        }

        this.file = file;

        this.verificationLevel = verificationLevel;

        Class.forName("org.sqlite.JDBC");   // Register the driver

        this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath()); // Initialize the database connection

        try
        {
            DatabaseUtility.setPragmaForeignKeys(this.databaseConnection, true);
            DatabaseUtility.setPragmaJournalModeMemory(this.databaseConnection);
            DatabaseUtility.setPragmaSynchronousOff(this.databaseConnection);

            // this was moved below setting the pragmas because is starts a transaction and causes setPragmaSynchronousOff to throw an exception
            this.databaseConnection.setAutoCommit(false);

            this.core       = new GeoPackageCore      (this.databaseConnection, isNewFile);
            this.features   = new GeoPackageFeatures  (this.databaseConnection, this.core);
            this.tiles      = new GeoPackageTiles     (this.databaseConnection, this.core);
            this.schema     = new GeoPackageSchema    (this.databaseConnection);
            this.metadata   = new GeoPackageMetadata  (this.databaseConnection);
            this.extensions = new GeoPackageExtensions(this.databaseConnection, this.core);

            if(isNewFile)
            {
                DatabaseUtility.setApplicationId(this.databaseConnection,
                                                 ByteBuffer.wrap(GeoPackage.GeoPackageSqliteApplicationId)
                                                           .asIntBuffer()
                                                           .get());
                this.databaseConnection.commit();
            }

            if(verificationLevel != VerificationLevel.None)
            {
                this.verify();
            }

            try
            {
                this.sqliteVersion = DatabaseUtility.getSqliteVersion(this.file);
            }
            catch(final IOException ex)
            {
                throw new IOException("Unable to read SQLite version: " + ex.getMessage());
            }
        }
        catch(final SQLException ex)
        {
            this.close();

            // If anything goes wrong, clean up the new file that may have been created
            if(isNewFile && file.exists())
            {
                file.delete();
            }

            throw ex;
        }
        catch(final ConformanceException ex)
        {
            this.close();

            // If anything goes wrong, clean up the new file that may have been created
            if(isNewFile && file.exists())
            {
                file.delete();
            }

            throw ex;
        }
        catch(final IOException ex)
        {
            this.close();

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
     */
    public String getSqliteVersion()
    {
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
     * @throws IOException throws if an SQLException occurs
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            if(this.databaseConnection            != null &&
               this.databaseConnection.isClosed() == false)
            {
                this.databaseConnection.rollback(); // When Connection.close() is called, pending transactions are either automatically committed or rolled back depending on implementation defined behavior.  Make the call explicitly to avoid relying on implementation defined behavior.
                this.databaseConnection.close();
            }
        }
        catch(final SQLException ex)
        {
            throw new IOException(ex);
        }
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @return the GeoPackage requirements this GeoPackage fails to conform to
     * @throws SQLException throws if SQLException occurs
     */
    public List<VerificationIssue> getVerificationIssues() throws SQLException
    {
        return this.getVerificationIssues(false);
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @param continueAfterCoreErrors
     *             If true, GeoPackage subsystem requirement violations will be
     *             reported even if there are fatal violations in the core.
     *             Subsystem verifications assumes that a GeoPackage is at
     *             least minimally operable (e.g. core tables are defined to
     *             the standard), and may behave unexpectedly if the GeoPackage
     *             does not. In this state, the reported failures of GeoPackage
     *             subsystems may only be of minimal value.
     * @return the GeoPackage requirements this GeoPackage fails to conform to
     * @throws SQLException throws if SQLException occurs
     */
    public List<VerificationIssue> getVerificationIssues(final boolean continueAfterCoreErrors) throws SQLException
    {
        final List<VerificationIssue> verificationIssues = new ArrayList<VerificationIssue>();

        verificationIssues.addAll(this.core.getVerificationIssues(this.file, this.verificationLevel));

        // Skip verifying GeoPackage subsystems if there are fatal errors in core
        if(continueAfterCoreErrors ||
           !FunctionalUtility.anyMatch(verificationIssues,
                                       new Predicate<VerificationIssue>()
                                       {
                                           @Override
                                           public boolean apply(final VerificationIssue t)
                                           {
                                               return t.getSeverity() == Severity.Error;
                                           }
                                       }))
        {
            verificationIssues.addAll(this.features  .getVerificationIssues(this.verificationLevel));
            verificationIssues.addAll(this.tiles     .getVerificationIssues(this.verificationLevel));
            verificationIssues.addAll(this.schema    .getVerificationIssues(this.verificationLevel));
            verificationIssues.addAll(this.metadata  .getVerificationIssues(this.verificationLevel));
            verificationIssues.addAll(this.extensions.getVerificationIssues(this.verificationLevel));
        }

        return verificationIssues;
    }

    /**
     * Verifies that the GeoPackage meets the core requirements of the GeoPackage specification
     *
     * @throws ConformanceException throws if the GeoPackage fails to meet the necessary requirements
     * @throws SQLException throws if SQLException occurs
     */
    public void verify() throws ConformanceException, SQLException
    {
        //final long startTime = System.nanoTime();

        final List<VerificationIssue> verificationIssues = this.getVerificationIssues();

        //System.out.println(String.format("GeoPackage took %.2f seconds to verify.", (System.nanoTime() - startTime)/1.0e9));

        if(verificationIssues.size() > 0)
        {
            final ConformanceException conformanceException = new ConformanceException(verificationIssues);

            System.out.println(conformanceException.toString());

            if(FunctionalUtility.anyMatch(verificationIssues,
                                          new Predicate<VerificationIssue>()
                                          {
                                              @Override
                                              public boolean apply(final VerificationIssue t)
                                              {
                                                  return t.getSeverity() == Severity.Error;
                                              }
                                          }))
            {
                throw conformanceException;
            }
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
     * Access to GeoPackage's "features" functionality
     *
     * @return returns a handle to a GeoPackageFeatures object
     */
    public GeoPackageFeatures features()
    {
        return this.features;
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
     * Access to GeoPackage's "schema" functionality
     *
     * @return returns a handle to a GeoPackageTiles object
     */
    public GeoPackageSchema schema()
    {
        return this.schema;
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
     * Access to GeoPackage's "metadata" functionality
     *
     * @return returns a handle to a GeoPackageMetadata object
     */
    public GeoPackageMetadata metadata()
    {
        return this.metadata;
    }

    /**
     * @author Luke Lambert
     *
     */
    public enum OpenMode
    {
        /**
         * Open or Create a GeoPackage
         */
        OpenOrCreate,

        /**
         * Open an Existing GeoPackage
         */
        Open,

        // TODO: OpenReadOnly

        /**
         * Create a new GeoPackage
         */
        Create
    }

    private final File                 file;
    private final Connection           databaseConnection;
    private final String               sqliteVersion;
    private final VerificationLevel    verificationLevel;
    private final GeoPackageCore       core;
    private final GeoPackageFeatures   features;
    private final GeoPackageTiles      tiles;
    private final GeoPackageSchema     schema;
    private final GeoPackageMetadata   metadata;
    private final GeoPackageExtensions extensions;

    private final static byte[] GeoPackageSqliteApplicationId = new byte[] {'G', 'P', '1', '0' };
}
