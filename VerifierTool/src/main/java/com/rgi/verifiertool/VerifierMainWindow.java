package com.rgi.verifiertool;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
     */
    public static void main(final String[] args)
    {
        Application.launch(args);
    }

    @SuppressWarnings("unused")
    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        Group root = new Group();
        Scene scene = new Scene(root, 551, 400);
        primaryStage.setTitle("GeoPackage Verifier Tool");
        final Text dragHereMessage = new Text("Drag GeoPackage File Here.");
        dragHereMessage.setX(200.0);
        dragHereMessage.setY(200.0);
        root.getChildren().add(dragHereMessage);

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
                success = true;
                String filePath = null;
                for (File file : db.getFiles())
                {
                    filePath = file.getAbsolutePath();
                    new PassFailWindow(new File(filePath));
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
