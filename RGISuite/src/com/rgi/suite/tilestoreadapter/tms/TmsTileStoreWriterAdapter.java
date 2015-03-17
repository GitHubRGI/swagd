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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.tile.store.tms.TmsWriter;
import com.rgi.common.util.FileUtility;
import com.rgi.suite.Settings;
import com.rgi.suite.SettingsWindow;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;

public class TmsTileStoreWriterAdapter extends TileStoreWriterAdapter
{
    private String name = "";

    private final JSpinner            compressionQualitySpinner    = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
    private final JTextField          directoryText                = new JTextField();
    private final JButton             outputFileNameButton         = new JButton("\u2026");
    private final JComboBox<String>   imageCompressionTypeComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
    private final JComboBox<MimeType> imageFormatComboBox          = new JComboBox<>(new DefaultComboBoxModel<>(TmsWriter.SupportedImageFormats
                                                                                                                         .stream()
                                                                                                                         .sorted((a, b) -> a.toString().compareTo(b.toString()))
                                                                                                                         .toArray(MimeType[]::new)));

    private final Collection<Collection<JComponent>> writerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Image format:"),        this.imageFormatComboBox),
                                                                                             Arrays.asList(new JLabel("Compression type:"),    this.imageCompressionTypeComboBox),
                                                                                             Arrays.asList(new JLabel("Compression quality:"), this.compressionQualitySpinner),
                                                                                             Arrays.asList(new JLabel("Directory:"),           this.directoryText, this.outputFileNameButton));

    public TmsTileStoreWriterAdapter(final Settings settings)
    {
        super(settings);

        // TODO save values of controls to settings

        this.outputFileNameButton.addActionListener(e -> { final String startDirectory = this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation);

                                                           final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                           fileChooser.setMultiSelectionEnabled(false);
                                                           fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                                                           final int option = fileChooser.showOpenDialog(null);

                                                           if(option == JFileChooser.APPROVE_OPTION)
                                                           {
                                                               final File file = fileChooser.getSelectedFile();

                                                               this.settings.set(SettingsWindow.OutputLocationSettingName, file.getParent());
                                                               this.settings.save();

                                                               final String directory = String.format("%s%c%s%c",
                                                                                                      fileChooser.getSelectedFile().getPath(),
                                                                                                      File.separatorChar,
                                                                                                      this.name,
                                                                                                      File.separatorChar);

                                                               this.directoryText.setText(directory);
                                                           }
                                                         });

        this.imageFormatComboBox.addActionListener(e -> { final MimeType mimeType = (MimeType)this.imageFormatComboBox.getSelectedItem();

                                                          this.imageCompressionTypeComboBox.removeAllItems();

                                                          final String[] compressionTypes = getImageWriteParameterCompressionTypes(mimeType);

                                                          this.imageCompressionTypeComboBox.setEnabled(compressionTypes.length > 0);

                                                          addAllItems(this.imageCompressionTypeComboBox,
                                                                      compressionTypes);
                                                        });

        this.imageFormatComboBox.setSelectedItem(TmsWriter.SupportedImageFormats.stream()
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
                                                       this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation),
                                                       File.separatorChar,
                                                       this.name);

            this.directoryText.setText(FileUtility.appendForUnique(directoryName) + File.separatorChar);
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
        final MimeType mimeType = (MimeType)this.imageFormatComboBox.getSelectedItem();

        return new TmsWriter(tileStoreReader.getCoordinateReferenceSystem(),
                             new File(this.directoryText.getText()).toPath(),
                             mimeType,
                             this.getImageWriteParameter(mimeType));
    }

    private static String[] getImageWriteParameterCompressionTypes(final MimeType mimeType)
    {
        try
        {
            return ImageIO.getImageWritersByMIMEType(mimeType.toString())
                          .next()   // Use the first
                          .getDefaultWriteParam()
                          .getCompressionTypes();
        }
        catch(final NoSuchElementException | UnsupportedOperationException ex)
        {
            return new String[] {};
        }
    }

    // TODO this is duplicated in GeoPackageTileStoreWriterAdapter
    private ImageWriteParam getImageWriteParameter(final MimeType mimeType)
    {
        try
        {
            final ImageWriter imageWriter = ImageIO.getImageWritersByMIMEType(mimeType.toString()).next();

            final ImageWriteParam imageWriteParameter = imageWriter.getDefaultWriteParam();

            if(imageWriteParameter.canWriteCompressed())
            {
                imageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParameter.setCompressionType((String)this.imageCompressionTypeComboBox.getSelectedItem());
                imageWriteParameter.setCompressionQuality(((Double)this.compressionQualitySpinner.getValue()).floatValue());
            }

            return imageWriteParameter;
        }
        catch(final NoSuchElementException ex)
        {
            throw new IllegalArgumentException(String.format("Mime type '%s' is not a supported for image writing by your Java environment", mimeType.toString()));
        }
    }

    private static <T> void addAllItems(final JComboBox<T> comboBox, final T... items)
    {
        for(final T item : items)
        {
            comboBox.addItem(item);
        }
    }
}
