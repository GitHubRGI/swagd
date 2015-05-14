package com.rgi.geopackage.extensions.network;

public class AttributeDescription
{
    /**
     * @param identifier
     * @param networkTableName
     * @param name
     * @param dataType
     * @param description
     * @param attributedType
     */
    public AttributeDescription(final int            identifier,
                                final String         networkTableName,
                                final String         name,
                                final DataType       dataType,
                                final String         description,
                                final AttributedType attributedType)
    {
        this.identifier       = identifier;
        this.networkTableName = networkTableName;
        this.name             = name;
        this.dataType         = dataType;
        this.description      = description;
        this.attributedType   = attributedType;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the networkTableName
     */
    public String getNetworkTableName()
    {
        return this.networkTableName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the data type
     */
    public DataType getDataType()
    {
        return this.dataType;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the attributedType
     */
    public AttributedType getAttributedType()
    {
        return this.attributedType;
    }

    public <T> boolean dataTypeAgrees(final T value)
    {
        return this.dataTypeAgrees(value.getClass());
    }

    public boolean dataTypeAgrees(final Class<?> clazz)
    {
        switch(this.dataType)
        {
            case Blob:    return byte[] .class.isAssignableFrom(clazz);
            case Integer: return Integer.class.isAssignableFrom(clazz);
            case Real:    return Double .class.isAssignableFrom(clazz) ||
                                 Float  .class.isAssignableFrom(clazz);
            case Text:    return String .class.isAssignableFrom(clazz);

            default: throw new RuntimeException("Bad enum value for DataType");
        }
    }

    private final int            identifier;
    private final String         networkTableName;
    private final String         name;
    private final DataType       dataType;
    private final String         description;
    private final AttributedType attributedType;
}
