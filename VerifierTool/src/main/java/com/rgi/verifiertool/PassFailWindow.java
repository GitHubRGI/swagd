package com.rgi.verifiertool;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.JButton;
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
public class PassFailWindow extends JFrame
{
    /**
     *
     */
    private final JPanel mainPanel = new JPanel();
    private static final long serialVersionUID = 1L;

    /**
     * @param file GeoPackage File to verify
     */
    public PassFailWindow(final File file)
    {
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(this, String.format("File does not exist: %s", file.getAbsolutePath()), "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            this.buildPassFailPanel(file);
        }

        this.setSize(500, 500);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.setVisible(true);


    }

    private void buildMessage(final Collection<VerificationIssue> failedRequirements, final JLabel label)
    {

        PassingLevel passingLevel = getPassingLevel(failedRequirements);
        this.mainPanel.add(label);
        JLabel result = new JLabel(passingLevel.getText());
        result.setForeground(passingLevel.getColor());
        this.mainPanel.add(result);
        JButton showMore = new JButton("show error/warning Messages");
        //TODO create Action Listener
        this.mainPanel.add(showMore);
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
