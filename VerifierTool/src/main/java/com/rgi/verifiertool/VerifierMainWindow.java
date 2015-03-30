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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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

    @SuppressWarnings("unused")//this is bc I do not make an object on line 105
    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        //Set the window up
        BorderPane layout = new BorderPane();
        Scene       scene = new Scene(layout, 551, 400);

        primaryStage.setTitle("GeoPackage Verifier Tool");

        final Text dragHereMessage = new Text("Drag GeoPackage Files Here.");
        dragHereMessage.setFill(Color.DARKBLUE);
        dragHereMessage.setFont(Font.font(null, FontWeight.BOLD, 30));

        Lighting lighting = new Lighting();
        Distant  light    = new Distant();
        light.setAzimuth(-135.0f);
        lighting.setLight(light);
        lighting.setSurfaceScale(4.0f);

        dragHereMessage.setEffect(lighting);

        primaryStage.setResizable(false);
        layout.setCenter(dragHereMessage);

        //create the even that drags the file over
        scene.setOnDragOver(event ->
        {
            Dragboard db = event.getDragboard();
            if (db.hasFiles())
            {
                event.acceptTransferModes(TransferMode.COPY);
            } else
            {
                event.consume();
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(event ->
        {

            Dragboard db = event.getDragboard();
            if (db.hasFiles())
            {
                for (File file : db.getFiles())
                {
                    new PassingLevelResultsWindow(file);
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
}
