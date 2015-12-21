package com.rgi.geopackage.features;

import java.util.function.BiConsumer;

/**
 * http://www.geopackage.org/spec/#gpb_spec
 *
 * @author Luke Lambert
 */
public enum EnvelopeContentsIndicator
{
    NoEnvelope(0, 0),
    Xy        (1, 4),
    Xyz       (2, 6),
    Xym       (3, 6),
    Xyzm      (4, 8);

    EnvelopeContentsIndicator(final int code,
                              final int arraySize)
    {
        this.code      = code;
        this.arraySize = arraySize;
    }

    public int getCode()
    {
        return this.code;
    }

    public int getArraySize()
    {
        return this.arraySize;
    }

    public static EnvelopeContentsIndicator fromCode(final int code)
    {
        //noinspection SwitchStatement
        switch(code)
        {
            case 0: return NoEnvelope;
            case 1: return Xy;
            case 3: return Xyz;
            case 4: return Xyzm;

            default: throw new IllegalArgumentException("Invalid envelope contents indicator code");
        }
    }

    private final int                              code;
    private final int                              arraySize;
}
