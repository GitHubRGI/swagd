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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
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
 * @author Luke Lambert
 * @author Jenifer Cochran
 *
 */
public class VerifierMainWindow extends Application
{
    private final ScrollPane scrollPane     = new ScrollPane();
    private final VBox       filesContainer = new VBox(10);
    public  final static double versionNumber = 1.0;

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
        final BorderPane layout     = new BorderPane();
        final Scene      scene      = new Scene(layout, 580, 400);
        final GridPane   bottomGrid = new GridPane();

        this.scrollPane.setFitToHeight(true);
        this.scrollPane.setFitToWidth (true);
        this.scrollPane.setContent(this.filesContainer);

        //set margins
        this.filesContainer.setPadding(new Insets(10));

        //Set the background Colors
        this.filesContainer.setStyle(String.format("-fx-background-color: %s;", Style.brightBlue.getHex()));
        this.scrollPane.setStyle(String.format("-fx-background-color: %s;", Style.brightBlue.getHex()));
        bottomGrid.setStyle(String.format("-fx-background-color: %s;", Style.greyBlue.getHex()));
        layout.setStyle(String.format("-fx-background-color: %s;", Style.brightBlue.getHex()));

        primaryStage.setTitle("RGi GeoPackage Verifier Tool");

        setIcon(primaryStage);

        bottomGrid.setHgap(100);

        final Text dragHereMessage = createFancyText("Drag GeoPackage Files Here");

        layout.setCenter(dragHereMessage);
        layout.setBottom(bottomGrid);

        this.createSWAGDHyperLink(bottomGrid);
        this.createGeoPackageSpecificationHyperLink(bottomGrid);
        createApplicationInfoHyperLink(bottomGrid);

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
                                              layout.setCenter(this.scrollPane);
                                              this.addFiles(db.getFiles());
                                              primaryStage.setResizable(true);
                                              primaryStage.setMinHeight(400);
                                              primaryStage.setMinWidth(580);
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

    private static void createApplicationInfoHyperLink(final GridPane bottomGrid)
    {
        final Hyperlink verifierToolInfo = new Hyperlink("About application");
        setHyperLinkFancyFont(verifierToolInfo);
        bottomGrid.add(verifierToolInfo, 2, 0);
        verifierToolInfo.setOnAction(e -> showApplicationInformation());
    }

    private void createSWAGDHyperLink(final GridPane bottomGrid)
    {
        final Hyperlink swagdInfo = new Hyperlink("SWAGD Project");
        setHyperLinkFancyFont(swagdInfo);
        bottomGrid.add(swagdInfo, 0, 0);
        swagdInfo.setOnAction(e -> this.getHostServices().showDocument("https://github.com/GitHubRGI/swagd"));
    }

    private void createGeoPackageSpecificationHyperLink(final GridPane bottomGrid)
    {
        final Hyperlink geoPackageLink = new Hyperlink("GeoPackage Specification");
        setHyperLinkFancyFont(geoPackageLink);
        bottomGrid.add(geoPackageLink, 1, 0);
        geoPackageLink.setOnAction(e -> this.getHostServices().showDocument("http://www.geopackage.org/spec/"));
    }

    private static void setIcon(final Stage stage)
    {
        final Image rgiIcon = new Image(VerifierMainWindow.class.getResourceAsStream("RGI_Logo.png"));
        stage.getIcons().add(rgiIcon);
    }

    private static void showApplicationInformation()
    {
        //create title and set font
        final Text title  = new Text("RGi GeoPackage Verifier Tool\n");
        title.setFill(Color.rgb(0, 120, 212));
        title.setFont(Font.font(Style.getFont(), FontWeight.EXTRA_BOLD, 20));
        title.setTextAlignment(TextAlignment.CENTER);

        //set company name and font
        final Text company = new Text("Reinventing Geospatial Inc.\n\n");
        company.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 18));
        company.setFill(Color.rgb(41, 110, 163));

        //set application information and font
        final Text about = new Text(String.format("This application will verify GeoPackages with Raster Tile data for the GeoPackage Encoding Standard Version 1.0.  "
                                  + "This Verification Tool will test the GeoPackage Core, GeoPackage Tiles, GeoPackage Extensions, GeoPackage Schema, "
                                  + "and GeoPackage Metadata requirements.  This will test a GeoPackage file against the Requirements referenced in the Encoding Standard.   "
                                  + "This will not verify GeoPackages with Feature data. RGi GeoPackage Verifier Tool Version %.1f", versionNumber));
        about.setFont(Font.font(Style.getFont(), FontWeight.THIN, 14));

        final Stage infoStage = new Stage();
        final TextFlow text   = new TextFlow(title, company, about);
        final Scene infoScene = new Scene(text, 400, 220);

        text.setStyle(String.format("-fx-background-color: %s;", Style.white.getHex()));

        setIcon(infoStage);
        infoStage.setResizable(false);
        infoStage.setScene(infoScene);
        infoStage.show();
    }

    private void addFiles(final Collection<File> files)
    {
        for(final File file : files)
        {
            if(file.isFile())
            {
                final FileVerificationPane verificationPane = new FileVerificationPane(file);
                verificationPane.setParent(VerifierMainWindow.this.filesContainer);

                VerifierMainWindow.this.filesContainer.getChildren().add(verificationPane);
            }
            else
            {
                //show error message
                final Stage errorStage = new Stage();
                final Label errorLabel = new Label("Error Invalid Input:\nMust select a File."); //TODO
                errorLabel.setFont(Font.font(Style.getFont(), FontWeight.EXTRA_BOLD, 16));
                errorLabel.setAlignment(Pos.CENTER);
                errorStage.setScene(new Scene(errorLabel, 300, 100));
                errorStage.show();
            }

        }
    }

    private static void setHyperLinkFancyFont(final Hyperlink link)
    {
        link.setTextFill(Style.darkAquaBlue.toColor());
        link.setFont(Font.font(Style.getFont(),FontWeight.BOLD, 11));
    }

    private static Text createFancyText(final String text)
    {
        final Text fancyText = new Text(text);
        fancyText.setFill(Style.white.toColor());
        fancyText.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 30));

        return fancyText;
    }
}
