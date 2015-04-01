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

package com.rgi.verifiertool;

import java.io.File;
import java.util.Collection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.Light.Distant;
import javafx.scene.effect.Lighting;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * @author Jenifer Cochran
 *
 */
public class VerifierMainWindow extends Application
{
    private final BorderPane layout = new BorderPane();

    private final VBox filesContainer = new VBox(); // TODO wrap in a scroll panel

    /**
     * Launch the Verifier application.
     * @param args incoming arguments
     */
    public static void main(final String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        //Set the window up
        final Scene    scene      = new Scene(this.layout, 550, 400);
        final GridPane bottomGrid = new GridPane();

        bottomGrid.setHgap(100);

        final Text dragHereMessage = createFancyText("Drag GeoPackage Files Here");

        this.layout.setCenter(dragHereMessage);
        this.layout.setBottom(bottomGrid);

        //Create Link to SWAGD github
        final Hyperlink swagdInfo = new Hyperlink("SWAGD Project");
        bottomGrid.add(swagdInfo, 0, 0);
        swagdInfo.setOnAction(e -> this.getHostServices().showDocument("https://github.com/GitHubRGI/swagd"));

        //create link to GeoPackage specification Document
        final Hyperlink geoPackageLink = new Hyperlink("GeoPackage Specification");
        bottomGrid.add(geoPackageLink, 1, 0);
        geoPackageLink.setOnAction(e -> this.getHostServices().showDocument("http://www.geopackage.org/spec/"));

        //add an about this application link
        final Hyperlink verifierToolInfo = new Hyperlink("About application");
        bottomGrid.add(verifierToolInfo, 2, 0);
        verifierToolInfo.setOnAction(e -> { final Text title  = new Text("GeoPackage Verifier Tool\n");
                                            title.setFill(Color.DARKBLUE);
                                            title.setFont(Font.font(null, FontWeight.EXTRA_BOLD, 20));
                                            title.setTextAlignment(TextAlignment.CENTER);

                                            final Text company = new Text("Reinventing Geospatial Inc.\n\n");
                                            company.setFont(Font.font(null, FontWeight.BOLD, 18));

                                            final Text about = new Text("This application will verify GeoPackages with Raster Tile data for the GeoPackage Encoding Standard Version 1.0.  "
                                                                      + "This Verification Tool will test the GeoPackage Core, GeoPackage Tiles, GeoPackage Extensions, GeoPackage Schema, "
                                                                      + "and GeoPackage Metadata requirements.  This will test a GeoPackage file against the Requirements referenced in the Encoding Standard.   "
                                                                      + "This will not verify GeoPackages with Feature data.");
                                            about.setFont(Font.font(null, FontWeight.THIN, 14));

                                            final Stage infoStage = new Stage();
                                            final TextFlow text   = new TextFlow(title, company, about);
                                            final Scene infoScene = new Scene(text, 400, 230);

                                            infoStage.setScene(infoScene);
                                            infoStage.show();
                                          });

        //create the even that drags the file over
        scene.setOnDragOver(event -> { final Dragboard db = event.getDragboard();
                                       if(db.hasFiles())
                                       {
                                           event.acceptTransferModes(TransferMode.COPY);
                                       }
                                       else
                                       {
                                           event.consume();
                                       }
                                     });

        // Dropping over surface
        scene.setOnDragDropped(event -> { final Dragboard db = event.getDragboard();
                                          if(db.hasFiles())
                                          {
                                              this.layout.setCenter(this.filesContainer);

                                              this.addFiles(db.getFiles());
                                          }
                                          event.setDropCompleted(true);
                                          event.consume();
                                        });

        //show the window

        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addFiles(final Collection<File> files)
    {
        for(final File file : files)
        {
            this.filesContainer.getChildren().add(new FileVerificationPane(file));

            //final Stage resultsStage = new PassingLevelResultsWindow(file);
            //resultsStage.show();
        }
    }

    private static Text createFancyText(final String text)
    {
        final Text fancyText = new Text(text);
        fancyText.setFill(Color.DARKBLUE);
        fancyText.setFont(Font.font(null, FontWeight.BOLD, 30));

        final Lighting lighting = new Lighting();
        final Distant  light    = new Distant();
        light.setAzimuth(-135.0f);
        lighting.setLight(light);
        lighting.setSurfaceScale(4.0f);

        fancyText.setEffect(lighting);

        return fancyText;
    }
}
