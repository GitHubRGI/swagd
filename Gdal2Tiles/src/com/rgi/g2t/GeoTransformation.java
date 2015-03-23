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
package com.rgi.g2t;

import org.gdal.gdal.Dataset;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;

/**
 *
 * http://gdal.org/java/org/gdal/gdal/Dataset.html#GetGeoTransform()
 *
 * @author Luke Lambert
 *
 */
public class GeoTransformation
{
    // affineTransform[0] // top left x
    // affineTransform[1] // w-e pixel resolution
    // affineTransform[2] // rotation, 0 if image is "north up"
    // affineTransform[3] // top left y
    // affineTransform[4] // rotation, 0 if image is "north up"
    // affineTransform[5] // n-s pixel resolution..... negative?

    /**
     * @param affineTransform
     *             Affine transformation coefficients
     */
    public GeoTransformation(final double[] affineTransform)
    {
        if(affineTransform == null || affineTransform.length != 6)
        {
            throw new IllegalArgumentException("The affine transform array must not be null, and must have a length of 6");
        }

        this.affineTransform = affineTransform;

        this.topLeft = new Coordinate<>(this.affineTransform[0],
                                        this.affineTransform[3]);

        this.pixelDimensions = new Dimensions<>(this.affineTransform[1],  // Height is stored as a negative number...
                                                 -this.affineTransform[5]);
    }

    /**
     * @return Returns true if the image is "north up"
     */
    public boolean isNorthUp()
    {
        return this.affineTransform[2] == 0.0 &&
               this.affineTransform[4] == 0.0;
    }

    /**
     * @return the affineTransform
     */
    public double[] getAffineTransform()
    {
        return this.affineTransform;
    }

    /**
     * @return the topLeft
     */
    public Coordinate<Double> getTopLeft()
    {
        return this.topLeft;
    }

    /**
     * @return the pixelDimensions
     */
    public Dimensions<Double> getPixelDimensions()
    {
        return this.pixelDimensions;
    }

    /**
     * @param dataset
     *             GDAL {@link Dataset}
     * @return Returns the geographic bounds of a dataset based on the geotransformation
     */
    public BoundingBox getBounds(final Dataset dataset)
    {
        return new BoundingBox(this.affineTransform[0],
                               this.affineTransform[0] + this.affineTransform[1] * dataset.getRasterXSize(),
                               this.affineTransform[3] + this.affineTransform[5] * dataset.getRasterYSize(),
                               this.affineTransform[3]);
    }

    /**
     * The 'identity' transformation
     *
     * I * V = V
     */
    public static final GeoTransformation Identity = new GeoTransformation(new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0});

    private final double[]           affineTransform;
    private final Coordinate<Double> topLeft;
    private final Dimensions<Double> pixelDimensions;
}
