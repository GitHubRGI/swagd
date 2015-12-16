package com.rgi.geopackage.features;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Luke Lambert
 */
@SuppressWarnings("CheckedExceptionClass")
public class WellKnownBinaryFormatException extends Exception
{
    private static final long serialVersionUID = 160039456060440345L;

    public WellKnownBinaryFormatException(final String message)
    {
        super(message);
    }

    private void writeObject(final ObjectOutputStream stream)
    {
        throw new RuntimeException("Serialization not supported");
    }

    private void readObject(final ObjectInputStream stream)
    {
        throw new RuntimeException("Serialization not supported");
    }
}
