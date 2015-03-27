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
import java.util.Collections;

import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
    ProgressIndicator indicatorCore  = new ProgressIndicator();
    ProgressIndicator indicatorTiles = new ProgressIndicator();
    ProgressIndicator indicatorExtensions = new ProgressIndicator();
    ProgressIndicator indicatorSchema = new ProgressIndicator();
    ProgressIndicator indicatorMetadata = new ProgressIndicator();

    Label resultCoreLabel = new Label();
    Label resultTiles = new Label();
    Label resultExtensions = new Label();
    Label resultSchema = new Label();
    Label resultMetadata = new Label();

    FailedRequirementsButton buttonCore = new FailedRequirementsButton(Collections.EMPTY_LIST);
    FailedRequirementsButton buttonTiles = new FailedRequirementsButton(Collections.EMPTY_LIST);
    FailedRequirementsButton buttonExtensions = new FailedRequirementsButton(Collections.EMPTY_LIST);
    FailedRequirementsButton buttonSchema = new FailedRequirementsButton(Collections.EMPTY_LIST);
    FailedRequirementsButton buttonMetadata = new FailedRequirementsButton(Collections.EMPTY_LIST);

    Result coreResult       = new Result(this.resultCoreLabel, PassingLevel.Fail, Collections.EMPTY_LIST, this.buttonCore);
    Result tilesResult      = new Result(this.resultTiles,     PassingLevel.Fail, Collections.EMPTY_LIST, this.buttonTiles);
    Result extensionsResult = new Result(this.resultExtensions,PassingLevel.Fail, Collections.EMPTY_LIST, this.buttonExtensions);
    Result schemaResult     = new Result(this.resultSchema,    PassingLevel.Fail, Collections.EMPTY_LIST, this.buttonSchema);
    Result metadataResult   = new Result(this.resultMetadata,  PassingLevel.Fail, Collections.EMPTY_LIST, this.buttonMetadata);

    GeoPackage gpkg;

    /**
     * @param file GeoPackage File to verify
     */
    public PassingLevelResultsWindow(final File file)
    {
        this.file = file;
        this.setTitle(String.format("Verification for file %s", file.getName()));
        try(GeoPackage geoPackage = new GeoPackage(PassingLevelResultsWindow.this.file, VerificationLevel.None, OpenMode.Open))
        {
            this.buildPassFailPanel(geoPackage);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void buildPassFailPanel(final GeoPackage geoPackage)
    {
        final Label coreLabel         = new Label("GeoPackage Core...");
        final Label tilesLabel        = new Label("GeoPackage Tiles...");
        final Label extensionsLabel   = new Label("GeoPackage Extensions...");
        final Label schemaLabel       = new Label("GeoPackage Schema...");
        final Label metadataLabel     = new Label("GeoPackage Metadata...");

        this.gridPanel.setHgap(2);
        this.gridPanel.setVgap(1);

        this.gpkg = geoPackage;

        ColumnConstraints columnLeft = new ColumnConstraints(230);
        ColumnConstraints columnCenter = new ColumnConstraints(90);
        ColumnConstraints columnRight = new ColumnConstraints(40);

        this.setResizable(false);
        this.gridPanel.getColumnConstraints().addAll(columnLeft, columnCenter, columnRight);

        //Verify GeoPackage and display results

            int row = 0;
            this.buildLabelTest(coreLabel, row);
            this.resultCoreLabel.setGraphic(this.indicatorCore);
            this.gridPanel.add(this.resultCoreLabel, 1, row);
            this.createButtonListener(this.buttonCore, "GeoPackage Core");
            this.buttonCore.setVisible(false);
            this.gridPanel.add(this.buttonCore, 3, row);

            int row1 = 1;
            this.buildLabelTest(tilesLabel, row1);
            this.resultTiles.setGraphic(this.indicatorTiles);
            this.gridPanel.add(this.resultTiles, 1, row1);
            this.createButtonListener(this.buttonTiles, "GeoPackage Tiles");
            this.buttonTiles.setVisible(false);
            this.gridPanel.add(this.buttonTiles, 3, row1);

            int row2 = 2;
            this.buildLabelTest(extensionsLabel, row2);
            this.resultExtensions.setGraphic(this.indicatorExtensions);
            this.gridPanel.add(this.resultExtensions, 1, row2);
            this.createButtonListener(this.buttonExtensions, "GeoPackage Extensions");
            this.buttonExtensions.setVisible(false);
            this.gridPanel.add(this.buttonExtensions, 3, row2);

            int row3 = 3;
            this.buildLabelTest(schemaLabel, row3);
            this.resultSchema.setGraphic(this.indicatorSchema);
            this.gridPanel.add(this.resultSchema, 1, row3);
            this.createButtonListener(this.buttonSchema, "GeoPackage Schema");
            this.buttonSchema.setVisible(false);
            this.gridPanel.add(this.buttonSchema, 3, row3);

            int row4 = 4;
            this.buildLabelTest(metadataLabel, row4);
            this.resultMetadata.setGraphic(this.indicatorMetadata);
            this.gridPanel.add(this.resultMetadata, 1, row4);
            this.createButtonListener(this.buttonMetadata, "GeoPackage Metadata");
            this.buttonMetadata.setVisible(false);
            this.gridPanel.add(this.buttonMetadata, 3, row4);
            //create task

            Task<Object> taskCore = new Task<Object>(){

                @Override
                protected Object call() throws Exception
                {
                       Collection<VerificationIssue> coreFailedMessages = PassingLevelResultsWindow.this.gpkg.core().getVerificationIssues(PassingLevelResultsWindow.this.file, VerificationLevel.Full);
                       PassingLevel corePassingLevel = PassingLevelResultsWindow.getPassingLevel(coreFailedMessages);
                       PassingLevelResultsWindow.this.coreResult.failedMessages = coreFailedMessages;
                       PassingLevelResultsWindow.this.coreResult.passingLevel = corePassingLevel;
                       this.updateValue(PassingLevelResultsWindow.this.coreResult);

                    return PassingLevelResultsWindow.this.tilesResult;
                }

            };

            Task<Object> taskTiles = new Task<Object>(){
                @Override
                protected Object call() throws Exception
                {
                    Collection<VerificationIssue> tilesFailedMessages = PassingLevelResultsWindow.this.gpkg.tiles().getVerificationIssues(VerificationLevel.Full);
                    PassingLevel tilesPassingLevel = PassingLevelResultsWindow.getPassingLevel(tilesFailedMessages);
                    PassingLevelResultsWindow.this.tilesResult.failedMessages = tilesFailedMessages;
                    PassingLevelResultsWindow.this.tilesResult.passingLevel = tilesPassingLevel;
                    this.updateValue(PassingLevelResultsWindow.this.tilesResult);
                    return PassingLevelResultsWindow.this.tilesResult;
                }
            };

            Task<Object> taskExtensions = new Task<Object>(){

                @Override
                protected Object call() throws Exception
                {
                    Collection<VerificationIssue> extensionFailedMessages = PassingLevelResultsWindow.this.gpkg.extensions().getVerificationIssues(VerificationLevel.Full);
                    PassingLevel extensionsPassingLevel = PassingLevelResultsWindow.getPassingLevel(extensionFailedMessages);
                    PassingLevelResultsWindow.this.extensionsResult.failedMessages = extensionFailedMessages;
                    PassingLevelResultsWindow.this.extensionsResult.passingLevel = extensionsPassingLevel;
                    this.updateValue(PassingLevelResultsWindow.this.extensionsResult);
                    return PassingLevelResultsWindow.this.extensionsResult;
                }
            };
            Task<Object> taskSchema = new Task<Object>(){

                @Override
                protected Object call() throws Exception
                {
                    Collection<VerificationIssue> schemaFailedMessages = PassingLevelResultsWindow.this.gpkg.schema().getVerificationIssues(VerificationLevel.Full);
                    PassingLevel schemaPassingLevel = PassingLevelResultsWindow.getPassingLevel(schemaFailedMessages);
                    PassingLevelResultsWindow.this.schemaResult.failedMessages = schemaFailedMessages;
                    PassingLevelResultsWindow.this.schemaResult.passingLevel = schemaPassingLevel;
                    this.updateValue(PassingLevelResultsWindow.this.schemaResult);
                    return PassingLevelResultsWindow.this.schemaResult;
                }
            };

            Task<Object> taskMetadata = new Task<Object>(){

                @Override
                protected Object call() throws Exception
                {
                    Collection<VerificationIssue> metadataFailedMessages = PassingLevelResultsWindow.this.gpkg.metadata().getVerificationIssues(VerificationLevel.Full);
                    PassingLevel metadataPassingLevel = PassingLevelResultsWindow.getPassingLevel(metadataFailedMessages);
                    PassingLevelResultsWindow.this.metadataResult.failedMessages = metadataFailedMessages;
                    PassingLevelResultsWindow.this.metadataResult.passingLevel = metadataPassingLevel;
                    this.updateValue(PassingLevelResultsWindow.this.metadataResult);
                    return PassingLevelResultsWindow.this.metadataResult;
                }
            };

            taskCore.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.label.setGraphic(null);
                     result.label.setText(result.passingLevel.getText());
                     result.label.setFont(new Font(this.fontSize));
                     result.label.setStyle("-fx-font-weight: bold");
                     result.label.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
               });

            taskTiles.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.label.setGraphic(null);
                     result.label.setText(result.passingLevel.getText());
                     result.label.setFont(new Font(this.fontSize));
                     result.label.setStyle("-fx-font-weight: bold");
                     result.label.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
               });

            taskExtensions.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.label.setGraphic(null);
                     result.label.setText(result.passingLevel.getText());
                     result.label.setFont(new Font(this.fontSize));
                     result.label.setStyle("-fx-font-weight: bold");
                     result.label.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
               });

            taskSchema.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.label.setGraphic(null);
                     result.label.setText(result.passingLevel.getText());
                     result.label.setFont(new Font(this.fontSize));
                     result.label.setStyle("-fx-font-weight: bold");
                     result.label.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
            });

            taskMetadata.valueProperty().addListener((obs, oldMessage, newMessage) -> {
                if(newMessage.getClass() == Result.class)
                  {
                     Result result = (Result) newMessage;
                     result.label.setGraphic(null);
                     result.label.setText(result.passingLevel.getText());
                     result.label.setFont(new Font(this.fontSize));
                     result.label.setStyle("-fx-font-weight: bold");
                     result.label.setTextFill(result.passingLevel.getColor());
                     if(!PassingLevel.Pass.equals(result.passingLevel))
                     {
                         result.button.setRequirements(result.failedMessages);
                         result.button.setVisible(true);
                     }
                  }
            });

            Thread core = new Thread(taskCore);
            Thread tiles = new Thread(taskTiles);
            Thread extensions = new Thread(taskExtensions);
            Thread schema = new Thread(taskSchema);
            Thread metadata = new Thread(taskMetadata);

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


                } catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }




       // this.setVisible(true);
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 450, 140, Color.WHITE);
        root.setCenter(this.gridPanel);
        this.setScene(scene);
        this.show();
    }

    private void buildLabelTest(final Label coreLabel, final int row)
    {
        //Create Label of Which Part of GeoPackage Tested
        this.gridPanel.add(coreLabel, 0, row);
        coreLabel.setFont(new Font(this.fontSize));
    }

    @SuppressWarnings("unused")
    private void buildPassingLevelAndShowMore(final Collection<VerificationIssue> failedRequirements, final int row, final String component)
    {
        //Get Passing Level
        PassingLevel passingLevel = getPassingLevel(failedRequirements);
        //Get The results and put it on the label
        Label result = new Label(passingLevel.getText());
        //Set the result in appropriate colored text
        result.setTextFill(passingLevel.getColor());
        result.setStyle("-fx-font-weight: bold");
        result.setFont(new Font(this.fontSize ));
        GridPane.setHalignment(result, HPos.LEFT);
        this.gridPanel.add(result, 1, row);

        //IF errors create and show the error messages
        FailedRequirementsButton showMore = new FailedRequirementsButton(failedRequirements);

        //do not show error message button if the test is passed
        if(passingLevel.equals(PassingLevel.Pass))
        {
            showMore.setVisible(false);
        }
        this.gridPanel.add(showMore, 3, row);
    }

    private void createButtonListener(final FailedRequirementsButton button, final String component)
    {
        button.setOnAction(e ->
        {
            try
            {
                if (e.getSource().getClass() == FailedRequirementsButton.class)
                {
                    new FailedRequirementsWindow(button.getFailedRequirements(), component);
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
