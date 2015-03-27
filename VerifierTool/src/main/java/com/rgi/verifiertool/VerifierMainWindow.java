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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author Jenifer Cochran
 *
 */
public class VerifierMainWindow extends Application
{
    /**
     * Launch the application.
     * @param args incoming arguments
     */

    public static void main(final String[] args)
    {
        Application.launch(args);
    }

    @SuppressWarnings("unused")
    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        //Set the window up
        Group root = new Group();
        Scene scene = new Scene(root, 551, 400);
        primaryStage.setTitle("GeoPackage Verifier Tool");
        final Text dragHereMessage = new Text("Drag GeoPackage Files Here.");
        dragHereMessage.setFont(new Font(20));
        dragHereMessage.setX(150.0);
        dragHereMessage.setY(190.0);

        root.getChildren().add(dragHereMessage);

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
            boolean success = false;
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
    }
}
