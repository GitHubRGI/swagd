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

import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
public class PassingLevelResultsWindow extends JFrame
{
    /**
     *
     */
    private final JPanel mainPanel = new JPanel();
    private static final long serialVersionUID = 1L;

    /**
     * @param file GeoPackage File to verify
     */
    public PassingLevelResultsWindow(final File file)
    {
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(this, String.format("File does not exist: %s", file.getAbsolutePath()), "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            this.buildPassFailPanel(file);
        }

        this.setSize(500, 200);
        this.setTitle(String.format("Verification for file %s", file.getName()));
    }

    private void buildPassFailPanel(final File file)
    {
        final JLabel coreLabel         = new JLabel("GeoPackage Core...");
        final JLabel tilesLabel        = new JLabel("GeoPackage Tiles...");
        final JLabel extensionsLabel   = new JLabel("GeoPackage Extensions...");
        final JLabel schemaLabel       = new JLabel("GeoPackage Schema...");
        final JLabel metadataLabel     = new JLabel("GeoPackage Metadata...");
        this.add(this.mainPanel);
        this.mainPanel.setLayout(new GridLayout(5,3));
        this.mainPanel.setBackground(Color.WHITE);

        try(GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Open))
        {
            final Collection<VerificationIssue> coreFailedRequirements       = gpkg.core().getVerificationIssues(file, VerificationLevel.Full);
            this.buildMessage(coreFailedRequirements, coreLabel);

            final Collection<VerificationIssue> tilesFailedRequirements      = gpkg.tiles().getVerificationIssues(VerificationLevel.Full);
            this.buildMessage(tilesFailedRequirements, tilesLabel);

            final Collection<VerificationIssue> extensionsFailedRequirements = gpkg.extensions().getVerificationIssues(VerificationLevel.Full);
            this.buildMessage(extensionsFailedRequirements, extensionsLabel);

            final Collection<VerificationIssue> schemaFailedRequirements     = gpkg.schema().getVerificationIssues(VerificationLevel.Full);
            this.buildMessage(schemaFailedRequirements, schemaLabel);

            final Collection<VerificationIssue> metadataFailedRequirements   = gpkg.metadata().getVerificationIssues(VerificationLevel.Full);
            this.buildMessage(metadataFailedRequirements, metadataLabel);

        } catch (ClassNotFoundException | SQLException| ConformanceException | IOException e)
        {
            e.printStackTrace();
        }

        this.setVisible(true);
    }

    @SuppressWarnings("unused")
    private void buildMessage(final Collection<VerificationIssue> failedRequirements, final JLabel label)
    {
        //Get Passing Level
        PassingLevel passingLevel = getPassingLevel(failedRequirements);
        //Create Label of Which Part of GeoPackage Tested
        this.mainPanel.add(label);
        //Get The results and put it on the label
        JLabel result = new JLabel(passingLevel.getText());
        //Set the result in appropriate colored text
        result.setForeground(passingLevel.getColor());
        //add the result text
        this.mainPanel.add(result);

        //IF errors create and show the error messages
        FailedRequirementsButton showMore = new FailedRequirementsButton("show more", failedRequirements);

        showMore.addActionListener(e ->
        {
            try
            {
                if (e.getSource().getClass() == FailedRequirementsButton.class)
                {
                    new FailedRequirementsWindow(showMore.getFailedRequirements());
                }

            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });


        this.mainPanel.add(showMore);

        //do not show error message button if the test is passed
        if(passingLevel.equals(PassingLevel.Pass))
        {
            showMore.setVisible(false);
        }
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
