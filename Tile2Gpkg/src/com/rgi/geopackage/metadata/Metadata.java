package com.rgi.geopackage.metadata;

import java.net.URI;

import javax.activation.MimeType;

public class Metadata
{
    /**
     * @param identifier
     * @param scope
     * @param standardUri
     * @param mimeType
     * @param metadata
     */
    protected Metadata(final int      identifier,
                       final Scope    scope,
                       final URI      standardUri,
                       final MimeType mimeType,
                       final String   metadata)
    {
        if(scope == null)
        {
            throw new IllegalArgumentException("Scope may not be null");
        }

        if(standardUri == null)
        {
            throw new IllegalArgumentException("Standard URI may not be null");
        }

        if(mimeType == null)
        {
            throw new IllegalArgumentException("Mime type may not be null");
        }

        if(metadata == null)
        {
            throw new IllegalArgumentException("Metadata may not be null");
        }

        this.identifier  = identifier;
        this.scope       = scope;
        this.standardUri = standardUri;
        this.mimeType    = mimeType;
        this.metadata    = metadata;
    }


    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the scope
     */
    public Scope getScope()
    {
        return this.scope;
    }

    /**
     * @return the standardUri
     */
    public URI getStandardUri()
    {
        return this.standardUri;
    }

    /**
     * @return the mimeType
     */
    public MimeType getMimeType()
    {
        return this.mimeType;
    }

    /**
     * @return the metadata
     */
    public String getMetadata()
    {
        return this.metadata;
    }

    private final int      identifier;
    private final Scope    scope;
    private final URI      standardUri;
    private final MimeType mimeType;
    private final String   metadata;
}
