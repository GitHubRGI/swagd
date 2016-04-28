package com.rgi.geopackage.features;

/**
 * @author Luke Lambert
 */
@SuppressWarnings({"CheckedExceptionClass", "SerializableHasSerializationMethods", "DeserializableClassInSecureContext", "SerializableClassInSecureContext"})
public class WellKnownBinaryFormatException extends Exception
{
    private static final long serialVersionUID = 160039456060440345L;

    /**
     * Constructor
     *
     * @param message
     *             Error message
     */
    public WellKnownBinaryFormatException(final String message)
    {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause
     *             Original error
     */
    public WellKnownBinaryFormatException(final Throwable cause)
    {
        super(cause);
    }
}
