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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    /**
     * Version number of The Verfier Tool
     */
    public final static String rgiToolVersionNumber = String.format("%.1f.%s", 1.0, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
    /**
     * The Version of the GeoPackage Encoding Standard that this Verifier reflects
     */
    public final static String geoPackageSpecificationVersionNumber = "1.0";
    final static BorderPane layout     = new BorderPane();
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

        primaryStage.setTitle("SWAGD GeoPackage Verifier Tool");

        setIcon(primaryStage);

        //set the bottom grid column constraints
        ColumnConstraints columnLeft   = new ColumnConstraints(90,  120, 2000, Priority.ALWAYS, HPos.LEFT,   true);
        ColumnConstraints columnCenter = new ColumnConstraints(150, 150, 2000, Priority.ALWAYS, HPos.CENTER, true);
        ColumnConstraints columnRight  = new ColumnConstraints(100, 120, 2000, Priority.ALWAYS, HPos.RIGHT,  true);
        bottomGrid.getColumnConstraints().addAll(columnLeft, columnCenter, columnRight);
        bottomGrid.setMinSize(400, 25);
        bottomGrid.setAlignment(Pos.CENTER);


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
        final Hyperlink verifierToolInfo = new Hyperlink("About Application");
        setHyperLinkFancyFont(verifierToolInfo);
        bottomGrid.add(verifierToolInfo, 2, 0);
        GridPane.setHalignment(verifierToolInfo, HPos.RIGHT);
        verifierToolInfo.setOnAction(e -> showApplicationInformation());
    }

    private void createSWAGDHyperLink(final GridPane bottomGrid)
    {
        final Hyperlink swagdInfo = new Hyperlink("SWAGD Project");
        setHyperLinkFancyFont(swagdInfo);
        bottomGrid.add(swagdInfo, 0, 0);
        GridPane.setHalignment(swagdInfo, HPos.LEFT);
        swagdInfo.setOnAction(e -> this.getHostServices().showDocument("https://github.com/GitHubRGI/swagd"));
    }

    private void createGeoPackageSpecificationHyperLink(final GridPane bottomGrid)
    {
        final Hyperlink geoPackageLink = new Hyperlink("GeoPackage Specification");
        setHyperLinkFancyFont(geoPackageLink);
        bottomGrid.add(geoPackageLink, 1, 0);
        GridPane.setHalignment(geoPackageLink, HPos.CENTER);
        geoPackageLink.setOnAction(e -> this.getHostServices().showDocument("http://www.geopackage.org/spec/"));
    }

    private static void setIcon(final Stage stage)
    {
        final Image geopkgIcon = new Image(VerifierMainWindow.class.getResourceAsStream("geopkg.png"));
        stage.getIcons().add(geopkgIcon);
    }

    private static void showApplicationInformation()
    {
        //create title and set font
        final Text title  = new Text("SWAGD GeoPackage Verifier Tool\n");
        title.setFill(Style.brightBlue.toColor());
        title.setFont(Font.font(Style.getMainFont(), FontWeight.EXTRA_BOLD, 20));
        title.setTextAlignment(TextAlignment.CENTER);

        //set company name and font
        final Text company = new Text("SWAGD\n\n");
        company.setFont(Font.font(Style.getMainFont(), FontWeight.BOLD, 18));
        company.setFill(Style.darkAquaBlue.toColor());

        //set application information and font
        final Text about = new Text(String.format("This application will verify GeoPackages with Raster Tile data for the GeoPackage Encoding Standard Version 1.0.  "
                                  + "This Verification Tool will test the GeoPackage Core, GeoPackage Tiles, GeoPackage Extensions, GeoPackage Schema, "
                                  + "and GeoPackage Metadata requirements.  This will test a GeoPackage file against the Requirements referenced in the GeoPackage Encoding Standard.   "
                                  + "This will not verify GeoPackages with Feature data. SWAGD GeoPackage Verifier Tool Version %s", rgiToolVersionNumber));
        about.setFont(Font.font(Style.getMainFont(), FontWeight.THIN, 14));

        final Stage infoStage = new Stage();
        infoStage.setTitle(title.getText());
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
                final Stage     errorStage = new Stage();
                      GridPane  gridPane = new GridPane();
                final TextFlow  errorTextFlow = new TextFlow();

                Text errorText = new Text(String.format("Error Invalid Input"));
                Text invalidFileText = new Text(String.format("\t%s", file));
                invalidFileText.setFont(Font.font(Style.getMainFont(), FontWeight.MEDIUM, 14));
                errorText.setFont(Font.font(Style.getMainFont(), FontWeight.EXTRA_BOLD, 16));

                errorTextFlow.getChildren().addAll(errorText, invalidFileText);
                errorTextFlow.setMaxWidth(500);

                Image      errorImage  = new Image(VerifierMainWindow.class.getResourceAsStream("errorIcon.png"));
                ImageView  errorViewer = new ImageView(errorImage);

                errorViewer.setFitHeight(60);
                errorViewer.setFitWidth(60);

                gridPane.add(errorViewer, 0, 0);
                gridPane.add(errorTextFlow, 1, 0);
                gridPane.setHgap(10);
                gridPane.resize(errorTextFlow.getPrefWidth() + errorViewer.getFitWidth(), errorTextFlow.getPrefHeight() + errorViewer.getFitHeight());

                errorStage.setTitle("Error Invalid Input");
                errorStage.getIcons().add(errorImage);
                errorStage.setScene(new Scene(gridPane, gridPane.getPrefWidth(), gridPane.getPrefHeight()));
                errorStage.setResizable(false);
                errorStage.show();
            }

        }
    }

    private static void setHyperLinkFancyFont(final Hyperlink link)
    {
        link.setTextFill(Style.darkAquaBlue.toColor());
        link.setFont(Font.font(Style.getMainFont(),FontWeight.BOLD, 11));
    }

    private static Text createFancyText(final String text)
    {
        final Text fancyText = new Text(text);
        fancyText.setFill(Style.white.toColor());
        fancyText.setFont(Font.font(Style.getMainFont(), FontWeight.BOLD, 30));

        return fancyText;
    }
    /**
     * @return The width of this resizable root(in this case layout). This is used to resize the button's layout in FileVerification Pane.
     */
    public static double getRootWidth()
    {
        return layout.getWidth();
    }

    /**
     * @return The width of this resizable root(in this case layout) property.
     * This is used to resize the button's layout in FileVerification Pane with binding the properties.

     */
    public static ReadOnlyDoubleProperty getRootWidthProperty()
    {
        return layout.widthProperty();
    }
}
