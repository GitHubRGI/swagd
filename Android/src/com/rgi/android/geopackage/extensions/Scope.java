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

package com.rgi.android.geopackage.extensions;

/**
 * GeoPackage Extension Scopes
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_requirement-84" type="cite">
 * The scope column value in a gpkg_extensions row SHALL be lowercase
 * "read-write" for an extension that affects both readers and writers, or
 * "write-only" for an extension that affects only writers.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#gpkg_extensions_cols">OGC® GeoPackage Encoding Standard - Table 17. GeoPackage Extensions Table or View Definition (Table or View Name: gpkg_extensions)</a>
 * @see <a href="http://www.geopackage.org/spec/#_requirement-84">OGC® GeoPackage Encoding Standard - Requirement 84</a>
 *
 * @author Luke Lambert
 *
 */
public enum Scope
{
    /**
     * The scope column in GeoPackage Extensions with a String value of "read-write"
     * http://www.geopackage.org/spec/#gpkg_extensions_cols
     */
    ReadWrite("read-write"),
    /**
     * The scope column in GeoPackage Extensions with a String value of
     * "write-only" http://www.geopackage.org/spec/#gpkg_extensions_cols
     */
    WriteOnly("write-only");

    Scope(final String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return this.text;
    }

    /**
     * @param text the String "read-write" or "write-only"
     * @return the associated Scope object with the given parameter
     */
    public static Scope fromText(final String text)
    {
        if(text == null)
        {
            throw new IllegalArgumentException("Text may not be null");
        }

        final String lowerText = text.toLowerCase();

        if(lowerText.equals("read-write"))
        {
            return ReadWrite;
        }
        else if(lowerText.equals("write-only"))
        {
            return WriteOnly;
        }

        throw new IllegalArgumentException("Scope must either be \"read-write\" or \"write-only\".");
    }

    private final String text;
}
