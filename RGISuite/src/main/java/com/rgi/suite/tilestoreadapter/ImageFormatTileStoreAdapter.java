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

package com.rgi.suite.tilestoreadapter;

import java.util.Collection;
import java.util.NoSuchElementException;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.JComboBox;

import com.rgi.suite.Settings;

/**
 * Abstract base {@link TileStoreWriterAdapter} that contains the UI elements
 * and logic to manipulate image compression parameters
 *
 * @author Luke Lambert
 *
 */
public abstract class ImageFormatTileStoreAdapter extends TileStoreWriterAdapter
{
    protected final JComboBox<Float>    compressionQuality   = new JComboBox<>();
    protected final JComboBox<String>   imageCompressionType = new JComboBox<>();
    protected final JComboBox<MimeType> imageFormat;

    /**
     * Constructor
     *
     * @param settings
     *             Handle to the application's settings object
     */
    public ImageFormatTileStoreAdapter(final Settings settings)
    {
        super(settings);

        this.imageFormat = new JComboBox<>();

        this.imageFormat.addActionListener(e -> { this.imageFormatChanged(); });

        this.imageCompressionType.addActionListener(e -> { this.imageCompressionTypeChanged(); });

        addAllItems(this.imageFormat,
                    this.getSupportedImageFormats()
                        .stream()
                        .sorted((a, b) -> a.toString().compareTo(b.toString()))
                        .toArray(MimeType[]::new));

        this.selectImageFormat(this.getInitialImageFormat());

//        this.compressionQuality  .setEnabled(false);
//        this.imageCompressionType.setEnabled(false);
//        this.imageFormat         .setEnabled(false);
    }

    protected abstract Collection<MimeType> getSupportedImageFormats();

    protected abstract MimeType getInitialImageFormat();

    protected ImageWriteParam getImageWriteParameter()
    {
        final ImageWriteParam imageWriteParameter = this.getImageWriter().getDefaultWriteParam();

        final String compressionType         = (String)this.imageCompressionType.getSelectedItem();
        final Float  compressionQualityValue = (Float) this.compressionQuality  .getSelectedItem();

        if(compressionType != null && imageWriteParameter.canWriteCompressed())
        {
            imageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParameter.setCompressionType(compressionType);

            if(compressionQualityValue != null)
            {
                imageWriteParameter.setCompressionQuality(compressionQualityValue);
            }

            return imageWriteParameter;
        }

        return null;
    }

    private void imageFormatChanged()
    {
        this.imageCompressionType.removeAllItems();

        try
        {
            final ImageWriter imageWriter = this.getImageWriter();

            addAllItems(this.imageCompressionType,
                        imageWriter.getDefaultWriteParam().getCompressionTypes());

            this.imageCompressionType.setEnabled(true);
        }
        catch(final NoSuchElementException | UnsupportedOperationException ex)
        {
            this.imageCompressionType.setEnabled(false);
            this.compressionQuality.setEnabled(false);
        }
    }

    private void imageCompressionTypeChanged()
    {
        final String compressionType = (String)this.imageCompressionType.getSelectedItem();

        this.compressionQuality.removeAllItems();

        if(compressionType != null)
        {
            final ImageWriteParam imageWriteParameter = this.getImageWriter().getDefaultWriteParam();

            if(imageWriteParameter.canWriteCompressed())
            {
                imageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParameter.setCompressionType(compressionType);

                final float[] qualityValues = imageWriteParameter.getCompressionQualityValues();

                this.compressionQuality.setEnabled(qualityValues != null && qualityValues.length > 0);

                if(qualityValues != null)
                {
                    for(final float value : qualityValues)
                    {
                        this.compressionQuality.addItem(value);
                    }

                    this.compressionQuality.setSelectedIndex(this.compressionQuality.getItemCount()-1);
                }
            }
        }
    }

    private ImageWriter getImageWriter()
    {
        final MimeType mimeType = (MimeType)this.imageFormat.getSelectedItem();

        return ImageIO.getImageWritersByMIMEType(mimeType.toString()).next();
    }

    @SafeVarargs
    private static <T> void addAllItems(final JComboBox<T> comboBox, final T... items)
    {
        for(final T item : items)
        {
            comboBox.addItem(item);
        }
    }

    protected void selectImageFormat(final MimeType mimeType)
    {
        for(int x = 0; x < this.imageFormat.getItemCount(); ++x)
        {
            final MimeType other = this.imageFormat.getItemAt(x);

            if(other.getBaseType().equals(mimeType.getBaseType()) &&
               other.getSubType() .equals(mimeType.getSubType()))
            {
                this.imageFormat.setSelectedIndex(x);
                return;
            }
        }
    }
}
