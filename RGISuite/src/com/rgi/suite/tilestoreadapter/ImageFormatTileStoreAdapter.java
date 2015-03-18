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

package com.rgi.suite.tilestoreadapter;

import java.util.Collection;
import java.util.NoSuchElementException;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.JComboBox;

import com.rgi.suite.Settings;

public abstract class ImageFormatTileStoreAdapter extends TileStoreWriterAdapter
{
    protected final JComboBox<Float>    compressionQuality   = new JComboBox<>();
    protected final JComboBox<String>   imageCompressionType = new JComboBox<>();
    protected final JComboBox<MimeType> imageFormat;

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

        this.imageFormat.setSelectedItem(this.getInitialImageFormat());
    }

    protected abstract Collection<MimeType> getSupportedImageFormats();

    protected abstract MimeType getInitialImageFormat();

    protected ImageWriteParam getImageWriteParameter()
    {
        final ImageWriteParam imageWriteParameter = this.getImageWriter().getDefaultWriteParam();

        final String compressionType         = (String)this.imageCompressionType.getSelectedItem();
        final Float  compressionQualityValue = (Float)this.compressionQuality.getSelectedItem();

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
}
