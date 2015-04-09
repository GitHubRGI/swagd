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

package com.rgi.suite.tilestoreadapter.geopackage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import store.GeoPackageWriter;

import com.rgi.common.Range;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.FileUtility;
import com.rgi.suite.Settings;
import com.rgi.suite.tilestoreadapter.ImageFormatTileStoreAdapter;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;

/**
 * {@link TileStoreWriterAdapter} for the GeoPackage tile store format
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageTileStoreWriterAdapter extends ImageFormatTileStoreAdapter
{
    private static final String GeoPackageOutputLocationSettingName = "ui.gpkg.outputLocation";

    private static final String DefaultGeoPackageOutputLocation = System.getProperty("user.home");

    private final JTextField tileSetName            = new JTextField();
    private final JTextField tileSetDescription     = new JTextField();
    private final JTextField filename               = new JTextField();
    private final JButton    outputFileNameSelector = new JButton("\u2026");

    private final Collection<Collection<JComponent>> writerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Image format:"),         this.imageFormat),
                                                                                             Arrays.asList(new JLabel("Compression type:"),     this.imageCompressionType),
                                                                                             Arrays.asList(new JLabel("Compression quality:"),  this.compressionQuality),
                                                                                             Arrays.asList(new JLabel("Tile set name:"),        this.tileSetName),
                                                                                             Arrays.asList(new JLabel("Tile set description:"), this.tileSetDescription),
                                                                                             Arrays.asList(new JLabel("File name:"),            this.filename, this.outputFileNameSelector));

    /**
     * Constructor
     *
     * @param settings
     *             Handle to the application's settings object
     */
    public GeoPackageTileStoreWriterAdapter(final Settings settings)
    {
        super(settings);

        this.outputFileNameSelector.addActionListener(e -> { final String startDirectory = this.settings.get(GeoPackageOutputLocationSettingName, DefaultGeoPackageOutputLocation);

                                                             final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                             fileChooser.setMultiSelectionEnabled(false);
                                                             fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                             final int option = fileChooser.showOpenDialog(null);

                                                             if(option == JFileChooser.APPROVE_OPTION)
                                                             {
                                                                 final File file = fileChooser.getSelectedFile();

                                                                 if(file.isDirectory())
                                                                 {
                                                                     final File oldFileSelection = new File(this.filename.getText());

                                                                     this.filename.setText(String.format("%s%c%s",
                                                                                                         file.getPath(),
                                                                                                         File.separatorChar,
                                                                                                         (oldFileSelection.isDirectory() ? "geopackage.gpkg"
                                                                                                                                         : oldFileSelection.getName())));
                                                                 }
                                                                 else
                                                                 {
                                                                     this.filename.setText(file.getPath());
                                                                 }

                                                                 this.settings.set(GeoPackageOutputLocationSettingName, (new File(this.filename.getText()).getParent()));
                                                                 this.settings.save();
                                                             }
                                                           });
    }

    @Override
    public String toString()
    {
        return "GeoPackage";
    }

    @Override
    protected Collection<MimeType> getSupportedImageFormats()
    {
        return GeoPackageWriter.SupportedImageFormats;
    }

    @Override
    protected MimeType getInitialImageFormat()
    {
        try
        {
            return new MimeType("image/png");
        }
        catch(final MimeTypeParseException ex)
        {
            // This can't happen so long as GeoPackages support PNGs
            return null;
        }
    }

    @Override
    public void hint(final File inputFile) throws TileStoreException
    {
        String name = FileUtility.nameWithoutExtension(inputFile);

        name = name.replaceAll("[^_a-zA-Z0-9]", "_");

        name = name.replaceAll("^gpkg_", "");

        if(name.matches("^[0-9].*"))
        {
            name = "_" + name;
        }

        this.filename.setText(FileUtility.appendForUnique(String.format("%s%c%s.gpkg",
                                                                        this.settings.get(GeoPackageOutputLocationSettingName, DefaultGeoPackageOutputLocation),
                                                                        File.separatorChar,
                                                                        name)));
        this.tileSetName.setText(name);
        this.tileSetDescription.setText(String.format("Tile store %s packaged by %s at %s",
                                                      name,
                                                      System.getProperty("user.name"),
                                                      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())));
    }

    @Override
    public Collection<Collection<JComponent>> getWriterParameterControls()
    {
        return this.writerParameterControls;
    }

    @Override
    public TileStoreWriter getTileStoreWriter(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        final File file = new File(this.filename.getText());

        final String parent = file.getParent();

        if(parent != null)
        {
            this.settings.set(GeoPackageOutputLocationSettingName, parent);
                              this.settings.save();
        }

        final MimeType mimeType = (MimeType)this.imageFormat.getSelectedItem();

        return new GeoPackageWriter(file,
                                    tileStoreReader.getCoordinateReferenceSystem(),
                                    this.tileSetName.getText(),
                                    this.tileSetName.getText(),
                                    this.tileSetDescription.getText(),
                                    tileStoreReader.getBounds(),
                                    getRelativeZoomTimesTwoTileScheme(tileStoreReader),
                                    mimeType,
                                    this.getImageWriteParameter());
    }

    @Override
    public void removeStore() throws TileStoreException
    {
        final File file = new File(this.filename.getText());

        if(file.delete() == false)
        {
            throw new TileStoreException(String.format("Unable to remove file '%s'",
                                                       file.getAbsolutePath()));
        }
    }

    private static TileScheme getRelativeZoomTimesTwoTileScheme(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();

        if(zoomLevels.size() == 0)
        {
            throw new TileStoreException("Input tile store contains no zoom levels");
        }

        final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);

        final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());

        final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
        final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);

        final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;
        final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;

        return new ZoomTimesTwo(zoomLevelRange.getMinimum(),
                                zoomLevelRange.getMaximum(),
                                minZoomLevelMatrixWidth,
                                minZoomLevelMatrixHeight);
    }
}
