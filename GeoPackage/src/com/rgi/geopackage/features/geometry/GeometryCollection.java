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

package com.rgi.geopackage.features.geometry;

/**
 * A collection of zero or more Geometry instances.
 * <br>
 * <br>
 * GeometryCollection is a generic term for the ST_GeomCollection type defined
 * in <a href="http://www.geopackage.org/spec/#12">ISO/IEC 13249-3:2011</a>,
 * which uses it for the definition of Well Known Text (WKT) and Well Known
 * Binary (WKB) encodings. The SQL type name GEOMCOLLECTION defined in <a
 * href="http://www.geopackage.org/spec/#10">OGC 06-104r4</a> and used in
 * <a href="spatial_ref_sys_data_table_definition">GeoPackage Specification
 * Clause 1.1.2.1.1</a> and <a href=
 * "http://www.geopackage.org/spec/#geometry_types">Annex E of the GeoPackage
 * Specification</a> refers to the SQL BLOB encoding of a GeometryCollection.
 *
 * @see "http://www.geopackage.org/spec/#_footnote_7"
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class GeometryCollection extends Geometry
{

}
