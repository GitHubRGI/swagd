package com.rgi.geopackage.features;

import java.util.function.BiConsumer;

/**
 * http://www.geopackage.org/spec/#gpb_spec
 *
 * @author Luke Lambert
 */
public enum EnvelopeContentsIndicator
{
    NoEnvelope(0, 0, (coordinate, array) -> { }),

    Xy        (1, 4, (coordinate, array) -> { nanMinimum(coordinate.getX(), array, 0); // min x
                                              nanMaximum(coordinate.getX(), array, 1); // max x
                                              nanMinimum(coordinate.getY(), array, 2); // min y
                                              nanMaximum(coordinate.getY(), array, 3); // max y
                                            }),

    Xyz       (2, 6, (coordinate, array) -> { Xy.comparer.accept(coordinate, array);
                                              nanMinimum(coordinate.getZ(), array, 4); // min z
                                              nanMaximum(coordinate.getZ(), array, 5); // max z
                                            }),

    Xym       (3, 6, (coordinate, array) -> { Xy.comparer.accept(coordinate, array);
                                              nanMinimum(coordinate.getM(), array, 4); // min m
                                              nanMaximum(coordinate.getM(), array, 5); // max m
                                            }),

    Xyzm      (4, 8, (coordinate, array) -> { Xy.comparer.accept(coordinate, array);

                                              nanMinimum(coordinate.getZ(), array, 4); // min z
                                              nanMaximum(coordinate.getZ(), array, 5); // max z
                                              nanMinimum(coordinate.getM(), array, 6); // min m
                                              nanMaximum(coordinate.getM(), array, 7); // max m
                                            });

    EnvelopeContentsIndicator(final int                              code,
                              final int                              arraySize,
                              final BiConsumer<Coordinate, double[]> comparer)
    {
        this.code      = code;
        this.arraySize = arraySize;
        this.comparer  = comparer;
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

    public BiConsumer<Coordinate, double[]> getComparer()
    {
        return this.comparer;
    }
    private static void nanMinimum(final double newValue, final double[] array, final int arrayIndex)
    {
        if(Double.isNaN(array[arrayIndex]) || newValue < array[arrayIndex])
        {
            array[arrayIndex] = newValue;
        }
    }

    private static void nanMaximum(final double newValue, final double[] array, final int arrayIndex)
    {
        if(Double.isNaN(array[arrayIndex]) || newValue > array[arrayIndex])
        {
            array[arrayIndex] = newValue;
        }
    }

    private final int                              code;
    private final int                              arraySize;

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final BiConsumer<Coordinate, double[]> comparer;
}
