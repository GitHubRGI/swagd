package com.rgi.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.geopackage.GeoPackageReader;
import com.rgi.view.regions.BrowserRegion;
import com.rgi.view.regions.TreeRegion;
import com.rgi.view.regions.ViewerMenuBar;

public class ViewerMainWindow extends Application
{

    private Scene scene;
    private final static BorderPane layout = new BorderPane();

    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Map Viewer");
        this.scene = new Scene(layout,750,500, Color.web("#666970"));

        try(TileStoreReader baseReader = this.createTestTileStoreReader())
        {
            final TreeItem<TileLoaderBridge> rootItem = new TreeItem<>(new TileLoaderBridge(this.createTestTileStoreReader()));

            TreeRegion tree = new TreeRegion(Arrays.asList(baseReader), rootItem);

            layout.setLeft(tree);
            layout.setCenter(new BrowserRegion());
            layout.setTop(new ViewerMenuBar(primaryStage, tree));

            primaryStage.setScene(this.scene);
            primaryStage.show();
        }
    }

    public static void main(final String[] args)
    {
       launch(args);
    }

    /*
     *  Delete these methods (testing purposes only)
     */

    private TileStoreReader createTestTileStoreReader()
    {
        String tileSetName = "tileSet";
        File testFile = getRandomFile(5);
        testFile.deleteOnExit();
        try(GeoPackage gpkg = createAGeoPackage(testFile, tileSetName))
        {
            return new GeoPackageReader(testFile, tileSetName);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        throw new RuntimeException("cannot create test reader");

    }

    private static String getRanString(final int length)
    {
        Random randomGenerator = new Random();
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {

         text[i] = characters.charAt(randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private static File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(getRanString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }

    private static GeoPackage createAGeoPackage(final File testFile, final String tileSetName) throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.tiles().addTileSet(tileSetName, getRanString(6), getRanString(7), new BoundingBox(-180, -90, 180, 90), gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            return gpkg;
        }
    }
}
