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
package com.rgi.suite.tilestoreadapter.tms;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.tile.store.tms.TmsWriter;
import com.rgi.common.util.FileUtility;
import com.rgi.suite.Settings;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;

public class TmsTileStoreWriterAdapter extends TileStoreWriterAdapter
{
    private static final String TmsOutputLocationSettingName = "ui.tms.outputLocation";

    private static final String DefaultTmsOutputLocation = System.getProperty("user.home");

    private String name = "";

    private final JComboBox<Float>    compressionQuality   = new JComboBox<>(new DefaultComboBoxModel<>());
    private final JTextField          directory            = new JTextField();
    private final JButton             outputFilename       = new JButton("\u2026");
    private final JComboBox<String>   imageCompressionType = new JComboBox<>(new DefaultComboBoxModel<>());
    private final JComboBox<MimeType> imageFormat          = new JComboBox<>(new DefaultComboBoxModel<>(TmsWriter.SupportedImageFormats
                                                                                                                 .stream()
                                                                                                                 .sorted((a, b) -> a.toString().compareTo(b.toString()))
                                                                                                                 .toArray(MimeType[]::new)));

    private final Collection<Collection<JComponent>> writerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Image format:"),        this.imageFormat),
                                                                                             Arrays.asList(new JLabel("Compression type:"),    this.imageCompressionType),
                                                                                             Arrays.asList(new JLabel("Compression quality:"), this.compressionQuality),
                                                                                             Arrays.asList(new JLabel("Directory:"),           this.directory, this.outputFilename));

    public TmsTileStoreWriterAdapter(final Settings settings)
    {
        super(settings);

        // TODO save values of controls to settings

        this.outputFilename.addActionListener(e -> { final String startDirectory = this.settings.get(TmsOutputLocationSettingName, DefaultTmsOutputLocation);

                                                     final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                     fileChooser.setMultiSelectionEnabled(false);
                                                     fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                                                     final int option = fileChooser.showOpenDialog(null);

                                                     if(option == JFileChooser.APPROVE_OPTION)
                                                     {
                                                         final File file = fileChooser.getSelectedFile();

                                                         this.settings.set(TmsOutputLocationSettingName, file.getParent());
                                                         this.settings.save();

                                                         final String directory = String.format("%s%c%s%c",
                                                                                                fileChooser.getSelectedFile().getPath(),
                                                                                                File.separatorChar,
                                                                                                this.name,
                                                                                                File.separatorChar);

                                                         this.directory.setText(directory);
                                                     }
                                                   });

        this.imageFormat.addActionListener(e -> { this.imageFormatChanged(); });

        this.imageCompressionType.addActionListener(e -> { this.imageCompressionTypeChanged(); });

        this.imageFormat.setSelectedItem(TmsWriter.SupportedImageFormats.stream()
                                                  .filter(mimeType -> mimeType.toString()
                                                                              .toLowerCase()
                                                                              .equals("image/png"))
                                                  .findFirst()
                                                  .get());
    }

    @Override
    public String toString()
    {
        return "TMS";
    }

    @Override
    public void hint(final File inputFile) throws TileStoreException
    {
        if(!inputFile.getName().isEmpty())
        {
            this.name = FileUtility.nameWithoutExtension(inputFile);

            final String directoryName = String.format("%s%c%s",
                                                       this.settings.get(TmsOutputLocationSettingName, DefaultTmsOutputLocation),
                                                       File.separatorChar,
                                                       this.name);

            this.directory.setText(FileUtility.appendForUnique(directoryName) + File.separatorChar);
        }
    }

    @Override
    public Collection<Collection<JComponent>> getWriterParameterControls()
    {
        return this.writerParameterControls;
    }

    @Override
    public TileStoreWriter getTileStoreWriter(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        final MimeType mimeType = (MimeType)this.imageFormat.getSelectedItem();

        return new TmsWriter(tileStoreReader.getCoordinateReferenceSystem(),
                             new File(this.directory.getText()).toPath(),
                             mimeType,
                             this.getImageWriteParameter());
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

    private ImageWriteParam getImageWriteParameter()
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

    @SafeVarargs
    private static <T> void addAllItems(final JComboBox<T> comboBox, final T... items)
    {
        for(final T item : items)
        {
            comboBox.addItem(item);
        }
    }
}
