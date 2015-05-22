package com.rgi.view.regions;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * @author Steve Lander
 * @author Jenifer Cochran
 *
 */
public class BrowserRegion extends Region
{
    final WebView   webView   = new WebView();
    final WebEngine webEngine = this.webView.getEngine();

    /**
     *
     */
    public BrowserRegion()
    {
        //apply the styles
        this.getStyleClass().add("browserpane");
        final String html = "res/index.html";
        final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
        // load the web page
        this.webEngine.load(uri.toString());
        //add the web view to the scene
        this.getChildren().add(this.webView);
    }

    /*
     * The following methods resize the window properly
     * (non-Javadoc)
     * @see javafx.scene.Parent#layoutChildren()
     */
    @Override protected void layoutChildren()
    {
        double width  = this.getWidth();
        double height = this.getHeight();
        this.layoutInArea(this.webView, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(final double height)
    {
        return 750;
    }

    @Override protected double computePrefHeight(final double width)
    {
        return 500;
    }
}
