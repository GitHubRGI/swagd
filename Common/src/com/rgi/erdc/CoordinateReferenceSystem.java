package com.rgi.erdc;

public class CoordinateReferenceSystem
{
    /**
     * @param authority
     * @param identifier
     */
    public CoordinateReferenceSystem(final String authority, final int identifier)
    {
        if(authority == null || authority.isEmpty())
        {
            throw new IllegalArgumentException("Authority string may not be null or empty");
        }

        this.authority  = authority.toUpperCase();
        this.identifier = identifier;
    }

    /**
     * @return the authority
     */
    public String getAuthority()
    {
        return this.authority;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String toString()
    {
        return String.format("%s:%d",
                             this.authority,
                             this.identifier);
    }

    private final String authority;
    private final int    identifier;
}
