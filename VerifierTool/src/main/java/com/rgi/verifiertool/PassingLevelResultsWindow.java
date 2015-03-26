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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.ConformanceException;
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

    /**
     * @param file GeoPackage File to verify
     */
    public PassingLevelResultsWindow(final File file)
    {
        this.buildPassFailPanel(file);
        this.setTitle(String.format("Verification for file %s", file.getName()));
    }

    private void buildPassFailPanel(final File file)
    {
        final Label coreLabel         = new Label("GeoPackage Core...");
        final Label tilesLabel        = new Label("GeoPackage Tiles...");
        final Label extensionsLabel   = new Label("GeoPackage Extensions...");
        final Label schemaLabel       = new Label("GeoPackage Schema...");
        final Label metadataLabel     = new Label("GeoPackage Metadata...");
        this.gridPanel.setHgap(2);
        this.gridPanel.setVgap(5);

        ColumnConstraints columnLeft = new ColumnConstraints();
        ColumnConstraints columnCenter = new ColumnConstraints();
        ColumnConstraints columnRight = new ColumnConstraints();

        columnLeft.setHgrow(Priority.ALWAYS);
        columnCenter.setHgrow(Priority.ALWAYS);
        columnRight.setHgrow(Priority.ALWAYS);

        this.gridPanel.getColumnConstraints().addAll(columnLeft, columnCenter, columnRight);


        try(GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Open))
        {
            final Collection<VerificationIssue> coreFailedRequirements       = gpkg.core().getVerificationIssues(file, VerificationLevel.Full);
            int row = 0;
            this.buildMessage(coreFailedRequirements, coreLabel, row, "GeoPackage Core");

            final Collection<VerificationIssue> tilesFailedRequirements      = gpkg.tiles().getVerificationIssues(VerificationLevel.Full);
            int row1 = 1;
            this.buildMessage(tilesFailedRequirements, tilesLabel, row1, "GeoPackage Tiles");

            final Collection<VerificationIssue> extensionsFailedRequirements = gpkg.extensions().getVerificationIssues(VerificationLevel.Full);
            int row2 = 2;
            this.buildMessage(extensionsFailedRequirements, extensionsLabel, row2, "GeoPackage Extensions");

            final Collection<VerificationIssue> schemaFailedRequirements     = gpkg.schema().getVerificationIssues(VerificationLevel.Full);
            int row3 = 3;
            this.buildMessage(schemaFailedRequirements, schemaLabel, row3, "GeoPackage Schema");

            final Collection<VerificationIssue> metadataFailedRequirements   = gpkg.metadata().getVerificationIssues(VerificationLevel.Full);
            int row4 = 4;
            this.buildMessage(metadataFailedRequirements, metadataLabel, row4, "GeoPackage Metadata");


        } catch (ClassNotFoundException | SQLException| ConformanceException | IOException e)
        {
            e.printStackTrace();
        }

       // this.setVisible(true);
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 400, 140, Color.WHITE);
        root.setCenter(this.gridPanel);
        this.setScene(scene);
        this.show();
    }

    @SuppressWarnings("unused")
    private void buildMessage(final Collection<VerificationIssue> failedRequirements, final Label coreLabel, final int row, final String component)
    {
        //Get Passing Level
        PassingLevel passingLevel = getPassingLevel(failedRequirements);
        //Create Label of Which Part of GeoPackage Tested
        this.gridPanel.add(coreLabel, 0, row);
        //Get The results and put it on the label
        Label result = new Label(passingLevel.getText());
        //Set the result in appropriate colored text
        result.setTextFill(passingLevel.getColor());
        GridPane.setHalignment(result, HPos.LEFT);
        this.gridPanel.add(result, 1, row);

        //IF errors create and show the error messages
        FailedRequirementsButton showMore = new FailedRequirementsButton("show more", failedRequirements);
        showMore.setOnAction(e ->
        {
            try
            {
                if (e.getSource().getClass() == FailedRequirementsButton.class)
                {
                    new FailedRequirementsWindow(showMore.getFailedRequirements(), component);
                }

            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
        //do not show error message button if the test is passed
        if(passingLevel.equals(PassingLevel.Pass))
        {
            showMore.setVisible(false);
        }
        this.gridPanel.add(showMore, 3, row);
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
