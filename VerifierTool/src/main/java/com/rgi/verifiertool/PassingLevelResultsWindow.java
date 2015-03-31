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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

   private final FailedRequirementsButton buttonCore       = new FailedRequirementsButton("GeoPackage Core");
   private final FailedRequirementsButton buttonTiles      = new FailedRequirementsButton("GeoPackage Tiles");
   private final FailedRequirementsButton buttonExtensions = new FailedRequirementsButton("GeoPackage Extensions");
   private final FailedRequirementsButton buttonSchema     = new FailedRequirementsButton("GeoPackage Schema");
   private final FailedRequirementsButton buttonMetadata   = new FailedRequirementsButton("GeoPackage Metadata");

   private final Result coreResult       = new Result(new Label("GeoPackage Core..."),      PassingLevel.Fail, this.buttonCore);
   private final Result tilesResult      = new Result(new Label("GeoPackage Tiles..."),     PassingLevel.Fail, this.buttonTiles);
   private final Result extensionsResult = new Result(new Label("GeoPackage Extensions..."),PassingLevel.Fail, this.buttonExtensions);
   private final Result schemaResult     = new Result(new Label("GeoPackage Schema..."),    PassingLevel.Fail, this.buttonSchema);
   private final Result metadataResult   = new Result(new Label("GeoPackage Metadata..."),  PassingLevel.Fail, this.buttonMetadata);

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
                    //get the failed requirements for each of the systems
                    PassingLevelResultsWindow.this.createTasks(geoPackage);
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
            result.getGeoPackageLabel().setFont(new Font(this.fontSize));
            createButtonListener(result.getButton());
            result.getButton().setVisible(false); //don't show if it is passed (will turn on if failed)

            this.gridPanel.add(result.getGeoPackageLabel(), 0, row);
            this.gridPanel.add(result.getPassingLabel(),    1, row);
            this.gridPanel.add(result.getButton(),          2, row);

            row++;
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 410, 140, Color.WHITE);
        root.setCenter(this.gridPanel);
        this.setScene(scene);
        this.show();
    }

    private static Task<Result> createTask(final Collection<VerificationIssue> messages, final Result result)
    {
        return new Task<Result>(){

            @Override
            protected Result call() throws Exception
            {
               result.setFailedMessages(messages);
               result.setPassingLevel(PassingLevelResultsWindow.getPassingLevel(messages));
               this.updateValue(result);
               return result;
            }
        };
    }

    private void createTasks(final GeoPackage geoPackage) throws SQLException
    {
        Task<Result> taskCore       = createTask(geoPackage.core()      .getVerificationIssues(geoPackage.getFile(), VerificationLevel.Full), this.coreResult);
        Task<Result> taskTiles      = createTask(geoPackage.tiles()     .getVerificationIssues(VerificationLevel.Full),                       this.tilesResult);
        Task<Result> taskExtensions = createTask(geoPackage.extensions().getVerificationIssues(VerificationLevel.Full),                       this.extensionsResult);
        Task<Result> taskSchema     = createTask( geoPackage.schema()   .getVerificationIssues(VerificationLevel.Full),                       this.schemaResult);
        Task<Result> taskMetadata   = createTask( geoPackage.metadata() .getVerificationIssues(VerificationLevel.Full),                       this.metadataResult);

        this.createTaskListeners(new ArrayList<>(Arrays.asList(taskCore, taskTiles, taskExtensions, taskSchema, taskMetadata)));

        Thread core       = new Thread(taskCore);
        Thread tiles      = new Thread(taskTiles);
        Thread extensions = new Thread(taskExtensions);
        Thread schema     = new Thread(taskSchema);
        Thread metadata   = new Thread(taskMetadata);

        core.start();
        tiles.start();
        extensions.start();
        metadata.start();
        schema.start();
    }

    private void createTaskListeners(final List<Task<Result>> taskList)
    {
        //this will post the result when finished verifying
        for(Task<Result> task: taskList)
        {
            task.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = newMessage;
                     result.getPassingLabel().setGraphic(null);
                     result.getPassingLabel().setText(result.getPassingLevel().getText());
                     result.getPassingLabel().setFont(new Font(this.fontSize));
                     result.getPassingLabel().setStyle("-fx-font-weight: bold");
                     result.getPassingLabel().setTextFill(result.getPassingLevel().getColor());
                     if(!PassingLevel.Pass.equals(result.getPassingLevel()))
                     {
                         result.getButton().setRequirements(result.getFailedMessages());
                         result.getButton().setVisible(true);
                     }
                     this.show();
                  }
            });
        }
    }

    private static void createButtonListener(final FailedRequirementsButton button)
    {
        button.setOnAction(e ->  new FailedRequirementsWindow(button.getFailedRequirements(), button.getComponent()));
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
