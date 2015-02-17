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

package com.rgi.geopackage.metadata;

import java.util.stream.Stream;

/**
 * GeoPackage Metadata Scopes
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_table_data_values_8" type="cite">
 * The initial contents of this table were obtained from the ISO 19115,
 * Annex B B.5.25 MD_ScopeCode code list, which was extended for use in the
 * GeoPackage specification by addition of entries with "NA" as the scope
 * code column in Metadata Table Definition.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#_table_data_values_8">OGC® GeoPackage Encoding Standard - 2.4.2.1.2. Table Data Values</a>
 *
 * @author Luke Lambert
 */

public enum Scope
{
    /**
     * Scope with the following values: "undefined", "NA",
     * "Metadata information scope is undefined"
     */
    // http://www.geopackage.org/spec/#metadata_scopes
    Undefined           ("undefined",             "NA", "Metadata information scope is undefined"),
    /**
     * Scope with the following values: "fieldSession", "012",
     * "Information applies to the field session"
     */
    FieldSession        ("fieldSession",         "012", "Information applies to the field session"),
    /**
     * Scope with the following values: "collectionSession", "004",
     * "Information applies to the collection session"
     */
    CollectionSession   ("collectionSession",    "004", "Information applies to the collection session"),
    /**
     * Scope with the following values: "collectionSession", "004",
     * "Information applies to the collection session"
     */
    Series              ("series",               "006", "Information applies to the (dataset) series"),
    /**
     * Scope with the following values: "dataset", "005",
     * "Information applies to the (geographic feature) dataset"
     */
    Dataset             ("dataset",              "005", "Information applies to the (geographic feature) dataset"),
    /**
     * Scope with the following values: "featureType", "010",
     * "Information applies to a feature type (class)"
     */
    FeatureType         ("featureType",          "010", "Information applies to a feature type (class)"),
    /**
     * Scope with the following values: "feature", "009",
     * "Information applies to a feature (instance)"
     */
    Feature             ("feature",              "009", "Information applies to a feature (instance)"),
    /**
     * Scope with the following values: "attributeType", "002",
     * "Information applies to the attribute class"
     */
    AttributeType       ("attributeType",        "002", "Information applies to the attribute class"),
    /**
     * Scope with the following values: "attribute", "001",
     * "Information applies to the characteristic of a feature (instance)"
     */
    Attribute           ("attribute",            "001", "Information applies to the characteristic of a feature (instance)"),
    /**
     * Scope with the following values: "tile", "016",
     * "Information applies to a tile a spatial subset of geographic data"
     */
    Tile                ("tile",                 "016", "Information applies to a tile a spatial subset of geographic data"),
    /**
     * Scope with the following values: "model", "015",
     * "Information applies to a copy or imitation of an existing or hypothetical object"
     */
    Model               ("model",                "015", "Information applies to a copy or imitation of an existing or hypothetical object"),
    /**
     *  Scope with the following values:
     *  "catalog", "NA", "Metadata applies to a feature catalog"
     */
    Catalog             ("catalog",               "NA", "Metadata applies to a feature catalog"),
    /**
     * Scope with the following values: "schema", "NA",
     * "Metadata applies to an application schema"
     */
    Schema              ("schema",                "NA", "Metadata applies to an application schema"),
    /**
     * Scope with the following values: "taxonomy", "NA",
     * "Metadata applies to a taxonomy or knowledge system"
     */
    Taxonomy            ("taxonomy",              "NA", "Metadata applies to a taxonomy or knowledge system"),
    /**
     * Scope with the following values: "software", "013",
     * "Information applies to a computer program or routine"
     */
    Software            ("software",             "013", "Information applies to a computer program or routine"),
    /**
     * Scope with the following values: ("service", "014",
     * "Information applies to a capability which a service provider entity makes available to a service user entity through a set of interfaces that define a behaviour such as a use case"
     */
    Service             ("service",              "014", "Information applies to a capability which a service provider entity makes available to a service user entity through a set of interfaces that define a behaviour such as a use case"),
    /**
     * Scope with the following values: "collectionHardware", "003",
     * "Information applies to the collection hardware class"
     */
    CollectionHardware  ("collectionHardware",   "003", "Information applies to the collection hardware class"),
    /**
     * Scope with the following values: "nonGeographicDataset", "007",
     * "Information applies to non-geographic data"
     */
    NonGeographicDataset("nonGeographicDataset", "007", "Information applies to non-geographic data"),
    /**
     * Scope with the following values: "dimensionGroup", "008",
     * "Information applies to a dimension group"
     */
    DimensionGroup      ("dimensionGroup",       "008", "Information applies to a dimension group");

    private Scope(final String name, final String code, final String description)
    {
        this.name        = name;
        this.code        = code;
        this.description = description;
    }

    /**
     * @param inName the name of the Scope value in form of a string
     * @return the Scope object that corresponds to the inName
     */
    public static Scope fromString(final String inName)
    {
        return Stream.of(Scope.values())
                     .filter(scope -> scope.toString().equalsIgnoreCase(inName))
                     .findFirst()
                     .orElse(null);
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * @return the code
     */
    public String getCode()
    {
        return this.code;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    private final String name;
    private final String code;
    private final String description;
}
