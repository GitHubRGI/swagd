package com.rgi.geopackage.metadata;

public enum ReferenceScope
{
    GeoPackage("geopackage"),
    Table     ("table"),
    Column    ("column"),
    Row       ("row"),
    RowCol    ("row/col");

    /**
     * @return the text
     */
    public String getText()
    {
        return this.text;
    }

    public static ReferenceScope fromText(final String text)
    {
        switch(text.toLowerCase())
        {
            case "geopackage": return ReferenceScope.GeoPackage;
            case "table":      return ReferenceScope.Table;
            case "column":     return ReferenceScope.Column;
            case "row":        return ReferenceScope.Row;
            case "row/col":    return ReferenceScope.RowCol;

            default: throw new IllegalArgumentException("Text does not match any valid values for GeoPackage reference scopes");
        }
    }

    public static boolean isRowScope(final ReferenceScope scope)
    {
        return scope == ReferenceScope.Row || scope == ReferenceScope.RowCol;
    }

    public static boolean isColumnScope(final ReferenceScope scope)
    {
        return scope == ReferenceScope.Column || scope == ReferenceScope.RowCol;
    }

    ReferenceScope(final String text)
    {
        this.text = text;
    }

    private final String text;
}
