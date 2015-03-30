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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * @author Jenifer Cochran
 *
 */
public class PassingLevelResultsWindow extends Stage
{
    GridPane gridPanel = new GridPane();
    private final int fontSize = 18;
    File file;

   private final FailedRequirementsButton buttonCore       = new FailedRequirementsButton(Collections.EMPTY_LIST, "GeoPackage Core");
   private final FailedRequirementsButton buttonTiles      = new FailedRequirementsButton(Collections.EMPTY_LIST, "GeoPackage Tiles");
   private final FailedRequirementsButton buttonExtensions = new FailedRequirementsButton(Collections.EMPTY_LIST, "GeoPackage Extensions");
   private final FailedRequirementsButton buttonSchema     = new FailedRequirementsButton(Collections.EMPTY_LIST, "GeoPackage Schema");
   private final FailedRequirementsButton buttonMetadata   = new FailedRequirementsButton(Collections.EMPTY_LIST, "GeoPackage Metadata");

   private final Result coreResult       = new Result(new Label("GeoPackage Core..."),      PassingLevel.Fail, this.buttonCore);
   private final Result tilesResult      = new Result(new Label("GeoPackage Tiles..."),     PassingLevel.Fail, this.buttonTiles);
   private final Result extensionsResult = new Result(new Label("GeoPackage Extensions..."),PassingLevel.Fail, this.buttonExtensions);
   private final Result schemaResult     = new Result(new Label("GeoPackage Schema..."),    PassingLevel.Fail, this.buttonSchema);
   private final Result metadataResult   = new Result(new Label("GeoPackage Metadata..."),  PassingLevel.Fail, this.buttonMetadata);

    GeoPackage gpkg;

    /**
     * @param file GeoPackage File to verify
     */
    public PassingLevelResultsWindow(final File file)
    {
        this.file = file;
        this.setTitle(String.format("Verification for file %s", file.getName()));
        this.buildPassFailPanel();
        //show window while validating the GeoPackages
        Task<Void> task = new Task<Void>(){

            @Override
            protected Void call() throws Exception
            {
                try(GeoPackage geoPackage = new GeoPackage(PassingLevelResultsWindow.this.file, VerificationLevel.None, OpenMode.Open))
                {
                    PassingLevelResultsWindow.this.gpkg = geoPackage;
                    //get the failed requirements for each of the systems
                    PassingLevelResultsWindow.this.createTasks();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                return null;
            }};
      Thread mainThread = new Thread(task);
      mainThread.start();

    }

    private void buildPassFailPanel()
    {
        this.gridPanel.setHgap(2);
        this.gridPanel.setVgap(1);


        ColumnConstraints columnLeft   = new ColumnConstraints(230);
        ColumnConstraints columnCenter = new ColumnConstraints(90);
        ColumnConstraints columnRight  = new ColumnConstraints(90);

        this.setResizable(false);
        this.gridPanel.getColumnConstraints().addAll(columnLeft, columnCenter, columnRight);

        //create the panel with the label of the system, passingLevel, and the button to show the failed requirements
        int row = 0;
        for(Result result: new ArrayList<>(Arrays.asList(this.coreResult, this.tilesResult, this.extensionsResult, this.metadataResult, this.schemaResult)))
        {
            result.geoPackageLabel.setFont(new Font(this.fontSize));
            createButtonListener(result.button);
            result.button.setVisible(false); //don't show if it is passed (will turn on if failed)

            this.gridPanel.add(result.geoPackageLabel, 0, row);
            this.gridPanel.add(result.passingLabel,    1, row);
            this.gridPanel.add(result.button,          2, row);

            row++;
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 410, 140, Color.WHITE);
        root.setCenter(this.gridPanel);
        this.setScene(scene);
        this.show();
    }

    private void createTasks()
    {

        Task<Object> taskCore = new Task<Object>(){

            @Override
            protected Object call() throws Exception
            {
               PassingLevelResultsWindow.this.coreResult.failedMessages = PassingLevelResultsWindow.this.gpkg.core().getVerificationIssues(PassingLevelResultsWindow.this.file, VerificationLevel.Full);
               PassingLevelResultsWindow.this.coreResult.passingLevel  = PassingLevelResultsWindow.getPassingLevel( PassingLevelResultsWindow.this.coreResult.failedMessages);
               this.updateValue(PassingLevelResultsWindow.this.coreResult);
               return PassingLevelResultsWindow.this.coreResult;
            }

        };

        Task<Object> taskTiles = new Task<Object>(){
            @Override
            protected Object call() throws Exception
            {
                PassingLevelResultsWindow.this.tilesResult.failedMessages  = PassingLevelResultsWindow.this.gpkg.tiles().getVerificationIssues(VerificationLevel.Full);
                PassingLevelResultsWindow.this.tilesResult.passingLevel = PassingLevelResultsWindow.getPassingLevel( PassingLevelResultsWindow.this.tilesResult.failedMessages);
                this.updateValue(PassingLevelResultsWindow.this.tilesResult);

                return PassingLevelResultsWindow.this.tilesResult;
            }
        };

        Task<Object> taskExtensions = new Task<Object>(){

            @Override
            protected Object call() throws Exception
            {
                PassingLevelResultsWindow.this.extensionsResult.failedMessages = PassingLevelResultsWindow.this.gpkg.extensions().getVerificationIssues(VerificationLevel.Full);
                PassingLevelResultsWindow.this.extensionsResult.passingLevel = PassingLevelResultsWindow.getPassingLevel(PassingLevelResultsWindow.this.extensionsResult.failedMessages);
                this.updateValue(PassingLevelResultsWindow.this.extensionsResult);

                return PassingLevelResultsWindow.this.extensionsResult;
            }
        };
        Task<Object> taskSchema = new Task<Object>(){

            @Override
            protected Object call() throws Exception
            {
                PassingLevelResultsWindow.this.schemaResult.failedMessages = PassingLevelResultsWindow.this.gpkg.schema().getVerificationIssues(VerificationLevel.Full);
                PassingLevelResultsWindow.this.schemaResult.passingLevel = PassingLevelResultsWindow.getPassingLevel(PassingLevelResultsWindow.this.schemaResult.failedMessages);
                this.updateValue(PassingLevelResultsWindow.this.schemaResult);

                return PassingLevelResultsWindow.this.schemaResult;
            }
        };

        Task<Object> taskMetadata = new Task<Object>(){

            @Override
            protected Object call() throws Exception
            {
                PassingLevelResultsWindow.this.metadataResult.failedMessages = PassingLevelResultsWindow.this.gpkg.metadata().getVerificationIssues(VerificationLevel.Full);
                PassingLevelResultsWindow.this.metadataResult.passingLevel   = PassingLevelResultsWindow.getPassingLevel(PassingLevelResultsWindow.this.metadataResult.failedMessages);
                this.updateValue(PassingLevelResultsWindow.this.metadataResult);

                return PassingLevelResultsWindow.this.metadataResult;
            }
        };

        this.createTaskListeners(new ArrayList<>(Arrays.asList(taskCore, taskTiles, taskExtensions, taskSchema, taskMetadata)));

        Thread core       = new Thread(taskCore);
        Thread tiles      = new Thread(taskTiles);
        Thread extensions = new Thread(taskExtensions);
        Thread schema     = new Thread(taskSchema);
        Thread metadata   = new Thread(taskMetadata);

        try
        {
            core.start();
            tiles.start();
            extensions.start();
            schema.start();
            metadata.start();

            core.join();
            tiles.join();
            extensions.join();
            schema.join();
            metadata.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void createTaskListeners(final List<Task<Object>> taskList)
    {
        //this will post the result when finished verifying
        for(Task<Object> task: taskList)
        {
            task.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.passingLabel.setGraphic(null);
                     result.passingLabel.setText(result.passingLevel.getText());
                     result.passingLabel.setFont(new Font(this.fontSize));
                     result.passingLabel.setStyle("-fx-font-weight: bold");
                     result.passingLabel.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
            });
        }
    }

    @SuppressWarnings("unused")//because of line 263, value not set to an object
    private static void createButtonListener(final FailedRequirementsButton button)
    {
        button.setOnAction(e ->
        {
            try
            {
                if (e.getSource().getClass() == FailedRequirementsButton.class)
                {
                    new FailedRequirementsWindow(button.getFailedRequirements(), button.getComponent());
                }

            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }

    /**
     * @param failedRequirements a collection of failed requirements
     * @return the passing level of the collection
     */
    public static PassingLevel getPassingLevel(final Collection<VerificationIssue> failedRequirements)
    {
        if(failedRequirements.isEmpty())
        {
            //Message Passed
            return PassingLevel.Pass;
        }
        else if(failedRequirements.stream().anyMatch(issue -> issue.getRequirement().severity().equals(Severity.Error)))
        {
            //failed
            return PassingLevel.Fail;
        }
        else
        {
            //Warning
            return PassingLevel.Warning;
        }
    }
}
