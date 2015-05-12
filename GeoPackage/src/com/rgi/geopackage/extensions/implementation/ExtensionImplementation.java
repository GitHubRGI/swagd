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

package com.rgi.geopackage.extensions.implementation;

import java.sql.Connection;
import java.sql.SQLException;

import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.Scope;

/**
 * @author Luke Lambert
 *
 */
public abstract class ExtensionImplementation
{
    public ExtensionImplementation(final Connection databaseConnection, final GeoPackageExtensions geoPackageExtensions) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException();
        }

        if(geoPackageExtensions == null)
        {
            throw new IllegalArgumentException();
        }

        this.databaseConnection   = databaseConnection;
        this.geoPackageExtensions = geoPackageExtensions;

        this.extension = this.geoPackageExtensions.getExtension(this.getTableName(),
                                                                this.getColumnName(),
                                                                this.getExtensionName());
    }

    public abstract String getTableName();

    public abstract String getColumnName();

    public abstract String getExtensionName();

    public abstract String getDefinition();

    public abstract Scope getScope();

    protected void lazyAddExtensionEntry() throws SQLException
    {
        if(this.extension == null)
        {
            this.extension = this.geoPackageExtensions.addExtension(this.getTableName(),
                                                                    this.getColumnName(),
                                                                    this.getExtensionName(),
                                                                    this.getDefinition(),
                                                                    this.getScope());
        }
    }

    protected final Connection           databaseConnection;
    protected final GeoPackageExtensions geoPackageExtensions;

    private Extension extension;
}
