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
 * @see <a href="http://www.geopackage.org/spec/#_table_data_values_8">GeoPackage specification - 2.4.2.1.2. Table Data Values</a>
 *
 */
public enum Scope
{
    // http://www.geopackage.org/spec/#metadata_scopes
    Undefined           ("undefined",             "NA", "Metadata information scope is undefined"),
    FieldSession        ("fieldSession",         "012", "Information applies to the field session"),
    CollectionSession   ("collectionSession",    "004", "Information applies to the collection session"),
    Series              ("series",               "006", "Information applies to the (dataset) series"),
    Dataset             ("dataset",              "005", "Information applies to the (geographic feature) dataset"),
    FeatureType         ("featureType",          "010", "Information applies to a feature type (class)"),
    Feature             ("feature",              "009", "Information applies to a feature (instance)"),
    AttributeType       ("attributeType",        "002", "Information applies to the attribute class"),
    Attribute           ("attribute",            "001", "Information applies to the characteristic of a feature (instance)"),
    Tile                ("tile",                 "016", "Information applies to a tile a spatial subset of geographic data"),
    Model               ("model",                "015", "Information applies to a copy or imitation of an existing or hypothetical object"),
    Catalog             ("catalog",               "NA", "Metadata applies to a feature catalog"),
    Schema              ("schema",                "NA", "Metadata applies to an application schema"),
    Taxonomy            ("taxonomy",              "NA", "Metadata applies to a taxonomy or knowledge system"),
    Software            ("software",             "013", "Information applies to a computer program or routine"),
    Service             ("service",              "014", "Information applies to a capability which a service provider entity makes available to a service user entity through a set of interfaces that define a behaviour such as a use case"),
    CollectionHardware  ("collectionHardware",   "003", "Information applies to the collection hardware class"),
    NonGeographicDataset("nonGeographicDataset", "007", "Information applies to non-geographic data"),
    DimensionGroup      ("dimensionGroup",       "008", "Information applies to a dimension group");

    Scope(final String name, final String code, final String description)
    {
        this.name        = name;
        this.code        = code;
        this.description = description;
    }

    public static Scope fromString(final String inName)
    {
        return Stream.of(Scope.values())
                     .filter(scope -> scope.getName().equalsIgnoreCase(inName))
                     .findFirst()
                     .orElse(null);
    }

    /**
     * @return the name
     */
    public String getName()
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
