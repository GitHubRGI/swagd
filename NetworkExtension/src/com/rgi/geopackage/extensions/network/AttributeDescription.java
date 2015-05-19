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
                                final String         units,
                                final DataType       dataType,
                                final String         description,
                                final AttributedType attributedType)
    {
        this.identifier       = identifier;
        this.networkTableName = networkTableName;
        this.name             = name;
        this.units            = units;
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
     * @return the name
     */
    public String getUnits()
    {
        return this.units;
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
    private final String         units;
    private final DataType       dataType;
    private final String         description;
    private final AttributedType attributedType;
}
