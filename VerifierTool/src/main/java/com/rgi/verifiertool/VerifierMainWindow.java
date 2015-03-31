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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.Light.Distant;
import javafx.scene.effect.Lighting;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author Jenifer Cochran
 *
 */
public class VerifierMainWindow extends Application
{
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
        final BorderPane layout = new BorderPane();
        final Scene      scene  = new Scene(layout, 550, 400);

        primaryStage.setTitle("GeoPackage Verifier Tool");

        final Text dragHereMessage = createFancyText("Drag GeoPackage Files Here.");

        final Text applicationInfo = new Text();//TODO
        final Hyperlink geoPackageLink = new Hyperlink("GeoPackage Specification");
        layout.setBottom(geoPackageLink);

        geoPackageLink.setOnAction((ThrowingEventHandler<ActionEvent>)(event -> Desktop.getDesktop().browse(new URI("http://www.geopackage.org/spec/"))));

        primaryStage.setResizable(false);
        layout.setCenter(dragHereMessage);

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
                                              for(final File file : db.getFiles())
                                              {
                                                  final Stage resultsStage = new PassingLevelResultsWindow(file);
                                                  resultsStage.show();
                                              }
                                          }
                                          event.setDropCompleted(true);
                                          event.consume();
                                        });

        //show the window
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> Platform.exit());
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
