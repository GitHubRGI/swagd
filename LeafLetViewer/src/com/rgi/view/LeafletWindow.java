package com.rgi.view;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import com.rgi.store.tiles.TileStoreReader;

/**
 *
 * @author Jenifer Cochran
 *
 */
public class LeafletWindow extends Application
{
    public  LeafletWindow(final Collection<TileStoreReader> tileStoreReaders)
    {

    }


    private Scene scene;
    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Web View");
        this.scene = new Scene(new Browser(),750,500, Color.web("#666970"));
        primaryStage.setScene(this.scene);
        primaryStage.show();
    }

    /**
     * @author Steve Lander
     * @author Jenifer Cochran
     *
     */
    public class Browser extends Region
    {
        final WebView browser = new WebView();
        final WebEngine webEngine = this.browser.getEngine();

        /**
         *
         */
        public Browser() {
            //apply the styles
            this.getStyleClass().add("browser");
            final String html = "res/index.html";
            final URI uri = Paths.get(html).toAbsolutePath().toUri();
            // load the web page
            this.webEngine.load(uri.toString());
            //add the web view to the scene
            this.getChildren().add(this.browser);
        }
    }
}
