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
package com.rgi.suite.tilestoreadapter.tms;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.activation.MimeType;
import javax.swing.JButton;
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
import com.rgi.suite.tilestoreadapter.ImageFormatTileStoreAdapter;

/**
 *
 *
 * @author Luke Lambert
 *
 */
public class TmsTileStoreWriterAdapter extends ImageFormatTileStoreAdapter
{
    private static final String TmsOutputLocationSettingName = "ui.tms.outputLocation";
    private static final String DefaultTmsOutputLocation     = System.getProperty("user.home");

    private final JTextField directory         = new JTextField();
    private final JButton    directorySelector = new JButton("\u2026");

    private final Collection<Collection<JComponent>> writerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Image format:"),        this.imageFormat),
                                                                                             Arrays.asList(new JLabel("Compression type:"),    this.imageCompressionType),
                                                                                             Arrays.asList(new JLabel("Compression quality:"), this.compressionQuality),
                                                                                             Arrays.asList(new JLabel("Directory:"),           this.directory, this.directorySelector));

    /**
     * Constructor
     *
     * @param settings
     *             Handle to the application's settings object
     *
     */
    public TmsTileStoreWriterAdapter(final Settings settings)
    {
        super(settings);

        this.directorySelector.addActionListener(e -> { final String startDirectory = this.settings.get(TmsOutputLocationSettingName, DefaultTmsOutputLocation);

                                                        final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                        fileChooser.setMultiSelectionEnabled(false);
                                                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                                                        final int option = fileChooser.showOpenDialog(null);

                                                        if(option == JFileChooser.APPROVE_OPTION)
                                                        {
                                                            final File file = fileChooser.getSelectedFile();

                                                            this.settings.set(TmsOutputLocationSettingName, file.getParent());
                                                            this.settings.save();

                                                            this.directory.setText(String.format("%s%c%s%c",
                                                                                                 fileChooser.getSelectedFile().getPath(),
                                                                                                 File.separatorChar,
                                                                                                 new File(this.directory.getText()).getName(),
                                                                                                 File.separatorChar));
                                                        }
                                                   });
    }

    @Override
    public String toString()
    {
        return "TMS";
    }

    @Override
    protected MimeType getInitialImageFormat()
    {
        return TmsWriter.SupportedImageFormats
                        .stream()
                        .filter(mimeType -> mimeType.toString()
                                                    .toLowerCase()
                                                    .equals("image/png"))
                        .findFirst()
                        .get();
    }

    @Override
    protected Collection<MimeType> getSupportedImageFormats()
    {
        return TmsWriter.SupportedImageFormats;
    }

    @Override
    public void hint(final File inputFile) throws TileStoreException
    {
        if(!inputFile.getName().isEmpty())
        {
            final String directoryName = String.format("%s%c%s",
                                                       this.settings.get(TmsOutputLocationSettingName, DefaultTmsOutputLocation),
                                                       File.separatorChar,
                                                       FileUtility.nameWithoutExtension(inputFile));

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
        final File file = new File(this.directory.getText());

        final String parent = file.getParent();

        if(parent != null)
        {
            this.settings.set(TmsOutputLocationSettingName, parent);
            this.settings.save();
        }

        final MimeType mimeType = (MimeType)this.imageFormat.getSelectedItem();

        return new TmsWriter(tileStoreReader.getCoordinateReferenceSystem(),
                             file.toPath(),
                             mimeType,
                             this.getImageWriteParameter());
    }

    @Override
    public void removeStore() throws TileStoreException
    {
        final File newDirectory = new File(this.directory.getText());

        if(newDirectory.delete() == false)
        {
            throw new TileStoreException(String.format("Unable to remove directory '%s'",
                                                       newDirectory.getAbsolutePath()));
        }
    }
}
