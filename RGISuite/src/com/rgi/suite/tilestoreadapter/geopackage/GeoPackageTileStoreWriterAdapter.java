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

    public GeoPackageTileStoreWriterAdapter(final Settings settings)
    {
        super(settings);

        // TODO save values of controls to settings

        this.outputFileNameSelector.addActionListener(e -> { final String startDirectory = this.settings.get(GeoPackageOutputLocationSettingName, DefaultGeoPackageOutputLocation);

                                                             final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                             fileChooser.setMultiSelectionEnabled(false);
                                                             fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                                                             final int option = fileChooser.showOpenDialog(null);

                                                             if(option == JFileChooser.APPROVE_OPTION)
                                                             {
                                                                 final File file = fileChooser.getSelectedFile();

                                                                 this.settings.set(GeoPackageOutputLocationSettingName, file.getParent());
                                                                 this.settings.save();

                                                                 this.filename.setText(fileChooser.getSelectedFile().getPath());
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

        if(name.startsWith("[0-9]"))
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
        final MimeType mimeType = (MimeType)this.imageFormat.getSelectedItem();

        return new GeoPackageWriter(new File(this.filename.getText()),
                                    tileStoreReader.getCoordinateReferenceSystem(),
                                    this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                    this.tileSetName.getText(),    // TODO !!IMPORTANT!! make sure this meets the naming standards
                                    this.tileSetDescription.getText(),
                                    tileStoreReader.getBounds(),
                                    getRelativeZoomTimesTwoTileScheme(tileStoreReader),
                                    mimeType,
                                    this.getImageWriteParameter());
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
