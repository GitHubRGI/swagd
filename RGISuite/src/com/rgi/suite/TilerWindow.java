//package com.rgi.suite;
//
//import java.awt.Color;
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//
//import javax.swing.AbstractSpinnerModel;
//import javax.swing.JColorChooser;
//import javax.swing.JLabel;
//import javax.swing.JSpinner;
//
//import com.rgi.common.Dimensions;
//import com.rgi.common.coordinate.CoordinateReferenceSystem;
//import com.rgi.common.tile.store.TileStoreException;
//import com.rgi.common.tile.store.TileStoreReader;
//import com.rgi.common.tile.store.TileStoreWriter;
//import com.rgi.common.util.FileUtility;
//import com.rgi.g2t.Tiler;
//
///**
// * Gather additional information for tiling, and tile
// *
// * @author Luke D. Lambert
// *
// */
//public class TilerWindow extends TileStoreCreationWindow
//{
//    private static final long serialVersionUID = -3488202344008846021L;
//
//    private static final String LastInputLocationSettingName = "tiling.lastInputLocation";
//
//    private final JSpinner     tileWidthSpinner;
//    private final JSpinner     tileHeightSpinner;
//    private final SwatchButton clearColorButton;
//
//    /**
//     * Constructor
//     *
//     * @param settings
//     *             Settings used to hint user preferences
//     */
//    public TilerWindow(final Settings settings)
//    {
//        super("Tiling", settings, LastInputLocationSettingName);
//
//        this.tileWidthSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(SettingsWindow.TileWidthSettingName,
//                                                                                     Integer::parseInt,
//                                                                                     SettingsWindow.DefaultTileWidth),
//                                                            128,
//                                                            2048));
//
//        this.tileHeightSpinner = new JSpinner(new PowerOfTwoSpinner(this.settings.get(SettingsWindow.TileHeightSettingName,
//                                                                                      Integer::parseInt,
//                                                                                      SettingsWindow.DefaultTileHeight),
//                                                             128,
//                                                             2048));
//
//
//        this.clearColorButton = new SwatchButton("");
//        this.clearColorButton.setColor(this.settings.get(SettingsWindow.NoDataColorSettingName,
//                                                         SettingsWindow::colorFromString,
//                                                         SettingsWindow.DefaultNoDataColor));
//
//        this.clearColorButton.addActionListener(e -> { final Color color = JColorChooser.showDialog(this,
//                                                                                                    "Choose No Data color...",
//                                                                                                    this.clearColorButton.getColor());
//
//                                                       if(color != null)
//                                                       {
//                                                           this.clearColorButton.setColor(color);
//                                                           this.settings.set(SettingsWindow.NoDataColorSettingName, SettingsWindow.colorToString(color));
//                                                           this.settings.save();
//                                                       }
//                                                     });
//
//        this.buildUi();
//        this.pack();
//    }
//
//    @Override
//    protected void execute(final TileStoreReader tileStoreReader, final TileStoreWriter tileStoreWriter) throws Exception
//    {
//        final int tileWidth  = (int)this.tileWidthSpinner .getValue();
//        final int tileHeight = (int)this.tileHeightSpinner.getValue();
//
//        final Color color = this.clearColorButton.getColor();
//
//        // Save UI values
//        this.settings.set(SettingsWindow.TileWidthSettingName,  Integer.toString(tileWidth));
//        this.settings.set(SettingsWindow.TileHeightSettingName, Integer.toString(tileHeight));
//
//        this.settings.set(SettingsWindow.NoDataColorSettingName, SettingsWindow.colorToString(color));
//
//        this.settings.save();
//
//        final Tiler tiler = new Tiler(new File(this.inputFileName.getText()),
//                                      tileStoreWriter,
//                                      new Dimensions<>(tileWidth,
//                                                       tileHeight),
//                                      color);
//        tiler.execute();
//    }
//
//    @Override
//    protected void inputFileChanged(final File file) throws TileStoreException
//    {
//        this.inputFileName.setText(file.getPath());
//
//        final CoordinateReferenceSystem crs = TilerWindow.getCrs(file);
//
//        this.inputCrs.setEditable(crs == null);
//
//        if(crs != null)
//        {
//            this.inputCrs.setSelectedItem(crs); // TODO if the store contains an unrecognized CRS the combo box won't change
//        }
//
//        this.outputFileName.setText(FileUtility.appendForUnique(String.format("%s%c%s.gpkg",
//                                                                              this.settings.get(SettingsWindow.OutputLocationSettingName, SettingsWindow.DefaultOutputLocation),
//                                                                              File.separatorChar,
//                                                                              FileUtility.nameWithoutExtension(file))));
//
//        final String name = FileUtility.nameWithoutExtension(file);
//
//        this.tileSetName.setText(name);
//        this.tileSetDescription.setText(String.format("Tile store %s (%s) packaged by %s at %s",
//                                                      name,
//                                                      file.getName(),
//                                                      System.getProperty("user.name"),
//                                                      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())));
//    }
//
//    @Override
//    protected Collection<OutputGridRow> getOutputParameters()
//    {
//        final List<OutputGridRow> outputParameters = new ArrayList<>();
//
//        outputParameters.add(new OutputGridRow(new JLabel("Tile Width:"),  this.tileWidthSpinner,  null));
//        outputParameters.add(new OutputGridRow(new JLabel("Tile Height:"), this.tileHeightSpinner, null));
//        outputParameters.add(new OutputGridRow(new JLabel("Clear color:"), this.clearColorButton,  null));
//
//        outputParameters.addAll(super.getOutputParameters());
//
//        return outputParameters;
//    }
//
//    @SuppressWarnings("serial")
//    private class PowerOfTwoSpinner extends AbstractSpinnerModel
//    {
//        private final int binaryLogOfMinimum;
//        private final int binaryLogOfMaximum;
//
//        private int binaryLogOfValue;
//
//        public PowerOfTwoSpinner(final int initial, final int minimum, final int maximum)
//        {
//            this.binaryLogOfMinimum = this.binaryLog(minimum);
//            this.binaryLogOfMaximum = this.binaryLog(maximum);
//
//            this.setValue(initial);
//        }
//
//        @Override
//        public Object getValue()
//        {
//            return (int)Math.pow(2, this.binaryLogOfValue);
//        }
//
//        @Override
//        public void setValue(final Object value)
//        {
//            this.binaryLogOfValue = this.binaryLog((int)value);
//            this.fireStateChanged();
//        }
//
//        @Override
//        public Object getNextValue()
//        {
//            final Object foo = this.binaryLogOfValue >= this.binaryLogOfMaximum ? null
//                                                                    : (int)Math.pow(2, this.binaryLogOfValue+1);
//
//            return foo;
//        }
//
//        @Override
//        public Object getPreviousValue()
//        {
//            return this.binaryLogOfValue <= this.binaryLogOfMinimum ? null
//                                                                    : (int)Math.pow(2, this.binaryLogOfValue-1);
//        }
//
//        private int binaryLog(final int val)
//        {
//            return (int)(Math.log(val) / Math.log(2));
//        }
//    }
//
//    private static CoordinateReferenceSystem getCrs(@SuppressWarnings("unused") final File file) throws RuntimeException
//    {
//        return null;
//
//        // TODO requires GDAL to work for this project
//        //osr.UseExceptions(); // TODO only do this once
//        //gdal.AllRegister();  // TODO only do this once
//        //
//        //final Dataset dataset = gdal.Open(file.getAbsolutePath(),
//        //                                  gdalconstConstants.GA_ReadOnly);
//        //
//        //if(dataset == null)
//        //{
//        //    return null;
//        //}
//        //
//        //final SpatialReference srs = new SpatialReference(dataset.GetProjection());
//        //
//        //gdal.GDALDestroyDriverManager(); // TODO only do this once
//        //
//        //final String attributePath = "PROJCS|GEOGCS|AUTHORITY";   // https://gis.stackexchange.com/questions/20298/
//        //
//        //final String authority  = srs.GetAttrValue(attributePath, 0);
//        //final String identifier = srs.GetAttrValue(attributePath, 1);
//        //
//        //if(authority == null || identifier == null)
//        //{
//        //    return null;    // Failed to get the attribute value for some reason, see: http://gdal.org/java/org/gdal/osr/SpatialReference.html#GetAttrValue(java.lang.String,%20int)
//        //}
//        //
//        //try
//        //{
//        //    return new CoordinateReferenceSystem(authority, Integer.parseInt(identifier));
//        //}
//        //catch(final NumberFormatException ex)
//        //{
//        //    return null;    // The authority identifier in the WKT wasn't an integer
//        //}
//    }
//}
