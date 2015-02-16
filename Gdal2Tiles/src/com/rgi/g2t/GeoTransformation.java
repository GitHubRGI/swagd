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

        this.topLeft = new Coordinate<>(this.affineTransform[3],
                                        this.affineTransform[0]);

        this.pixelDimensions = new Dimensions(-this.affineTransform[5],  // Height is stored as a negative number...
                                               this.affineTransform[1]);
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
    public Dimensions getPixelDimensions()
    {
        return this.pixelDimensions;
    }

    public Coordinate<Double> getBottomRight(final double rasterHeight, final double rasterWidth)
    {
        return new Coordinate<>(this.affineTransform[3] + this.affineTransform[5] * rasterHeight,
                                this.affineTransform[0] + this.affineTransform[1] * rasterWidth);
    }

    public BoundingBox getBounds(final Dataset dataset)
    {
        return new BoundingBox(this.affineTransform[3] + this.affineTransform[5] * dataset.getRasterYSize(),
                               this.affineTransform[0],
                               this.affineTransform[3],
                               this.affineTransform[0] + this.affineTransform[1] * dataset.getRasterXSize());
    }

    public static final GeoTransformation Identity = new GeoTransformation(new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0});

    private final double[]           affineTransform;
    private final Coordinate<Double> topLeft;
    private final Dimensions         pixelDimensions;
}
