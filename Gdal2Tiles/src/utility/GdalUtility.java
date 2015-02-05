/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package utility;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * @author Luke Lambert
 *
 */
public class GdalUtility
{
    /**
     * Converts a GDAL {@link Dataset} into a {@link BufferedImage} <br>
     * <br>
     * Code based on GDAL/SWIG example found <a href=
     * "https://svn.osgeo.org/gdal/trunk/gdal/swig/java/apps/GDALtest.java"
     * >here</a>.
     *
     * @param dataset
     *            A GDAL {@link Dataset}
     * @return Returns a {@link BufferedImage} who's contents matches that of
     *         the input GDAL {@link Dataset}
     */
    public static BufferedImage convert(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Dataset may not be null");
        }

        final int bandCount = dataset.getRasterCount();

        if(bandCount <= 0)
        {
            throw new RuntimeException("Raster contained no bands");
        }

        final ByteBuffer[] bands   = new ByteBuffer[bandCount];
        final int[]        banks   = new int       [bandCount];
        final int[]        offsets = new int       [bandCount];

        final int xsize  = dataset.getRasterXSize();
        final int ysize  = dataset.getRasterYSize();
        final int pixels = xsize * ysize;

        int bandDataType = 0;
        Band band = null;

        for(int bandIndex = 0; bandIndex < bandCount; bandIndex++)
        {
            band = dataset.GetRasterBand(bandIndex + 1);   // Bands are not 0-base indexed, so we must add 1

            bandDataType = band.getDataType();
            final int bufferSize = pixels * (gdal.GetDataTypeSize(bandDataType) / 8);

            final ByteBuffer data = ByteBuffer.allocateDirect(bufferSize);
            data.order(ByteOrder.nativeOrder());

            if(band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(), xsize, ysize, bandDataType, data) != gdalconstConstants.CE_None)
            {
                throw new RuntimeException("Could not read raster: " + GdalError.lastError());
            }

            bands[bandIndex] = data;

            banks[bandIndex] = bandIndex;
            offsets[bandIndex] = 0;
        }

        if(band == null)
        {
            throw new RuntimeException("Raster contained no bands");
        }

        DataBuffer  dataBuffer = null;
        SampleModel sampleModel = null;
        int bufferedImageDataType = 0;
        int dataBufferType = 0;

        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            final byte[][] bytes = new byte[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                bytes[i] = new byte[pixels];
                bands[i].get(bytes[i]);
            }
            dataBuffer = new DataBufferByte(bytes, pixels);
            dataBufferType = DataBuffer.TYPE_BYTE;
            sampleModel = new BandedSampleModel(dataBufferType, xsize, ysize, xsize, banks, offsets);
            bufferedImageDataType = (band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            final short[][] shorts = new short[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                shorts[i] = new short[pixels];
                bands[i].asShortBuffer().get(shorts[i]);
            }
            dataBuffer = new DataBufferShort(shorts, pixels);
            dataBufferType = DataBuffer.TYPE_USHORT;
            sampleModel = new BandedSampleModel(dataBufferType, xsize, ysize, xsize, banks, offsets);
            bufferedImageDataType = BufferedImage.TYPE_USHORT_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            final int[][] ints = new int[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                ints[i] = new int[pixels];
                bands[i].asIntBuffer().get(ints[i]);
            }
            dataBuffer = new DataBufferInt(ints, pixels);
            dataBufferType = DataBuffer.TYPE_INT;
            sampleModel = new BandedSampleModel(dataBufferType, xsize, ysize, xsize, banks, offsets);
            bufferedImageDataType = BufferedImage.TYPE_CUSTOM;
        }

        final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
        BufferedImage img = null;
        ColorModel cm = null;

        if(band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
        {
            bufferedImageDataType = BufferedImage.TYPE_BYTE_INDEXED;
            cm = band.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(bandDataType));
            img = new BufferedImage(cm, raster, false, null);
        }
        else
        {
            if(bandCount > 2)
            {
                cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                                    bandCount == 4,
                                                                    false,
                                                                    bandCount == 4 ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
                                                                    dataBufferType);
                img = new BufferedImage(cm, raster, true, null);
            }
            else
            {
                img = new BufferedImage(xsize, ysize, bufferedImageDataType);
                img.setData(raster);
            }
        }
        return img;
    }
}
