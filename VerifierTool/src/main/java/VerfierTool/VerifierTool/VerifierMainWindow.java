package VerfierTool.VerifierTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
public class VerifierMainWindow
{
    /**
     * @author Jenifer Cochran
     *
     */
    public enum PassingLevel
    {
        /**
         * Set the font to green if GeoPackage passes verifier
         */
        Pass(Color.GREEN),
        /**
         * Set the font to yellow if GeoPackage has warnings but no errors in verifier
         */
        Warning(Color.YELLOW),
        /**
         * Set the font to red if GeoPackage has failing requirements
         */
        Fail(Color.RED);
        private Color color;

        PassingLevel(final Color color)
        {
            this.color = color;
        }
    }

    private JFrame mainFileFrame;
    private JFrame passOrFailFrame;
    private static VerifierMainWindow window;

    /**
     * Launch the application.
     */
    public static void main(final String[] args)
    {
        EventQueue.invokeLater(() ->
        {
            try
            {
                window = new VerifierMainWindow();
                window.mainFileFrame.setVisible(true);
                window.passOrFailFrame.setVisible(false);
            } catch (final Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     * @wbp.parser.entryPoint
     */
    public VerifierMainWindow()
    {
        this.initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        this.mainFileFrame = new JFrame();
        this.mainFileFrame.setBounds(100, 100, 450, 300);
        this.mainFileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.addActionListener(e ->
        {
            final int option = fileChooser.showOpenDialog(VerifierMainWindow.this.mainFileFrame);
            if(option == JFileChooser.APPROVE_OPTION)
            {
                final File file = fileChooser.getSelectedFile();
            }
        });
        //this.buildPassFailPanel(file);
        this.mainFileFrame.getContentPane().add(fileChooser, BorderLayout.CENTER);
    }

    private void buildPassFailPanel(final File file)
    {
        VerifierMainWindow.this.passOrFailFrame = new JFrame();
        VerifierMainWindow.this.passOrFailFrame.setVisible(true);
        final JLabel coreLabel         = new JLabel("GeoPackage Core...");
        final JLabel tilesLabel        = new JLabel("GeoPackage Tiles...");
        final JLabel extensionsLabel   = new JLabel("GeoPackage Extensions...");
        final JLabel schemaLabel       = new JLabel("GeoPackage Schema...");
        final JLabel metadataLabel     = new JLabel("GeoPackage Metadata...");


        try(GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Open))
        {
            final Collection<VerificationIssue> coreFailedRequirements       = gpkg.core().getVerificationIssues(file, VerificationLevel.Full);
            final PassingLevel coreLevel = VerifierMainWindow.getPassingLevel(coreFailedRequirements);

            final Collection<VerificationIssue> tilesFailedRequirements      = gpkg.tiles().getVerificationIssues(VerificationLevel.Full);
            final PassingLevel tileLevel = VerifierMainWindow.getPassingLevel(tilesFailedRequirements);

            final Collection<VerificationIssue> extensionsFailedRequirements = gpkg.extensions().getVerificationIssues(VerificationLevel.Full);
            final PassingLevel extensionsLevel = VerifierMainWindow.getPassingLevel(extensionsFailedRequirements);

            final Collection<VerificationIssue> schemaFailedRequirements     = gpkg.schema().getVerificationIssues(VerificationLevel.Full);
            final PassingLevel schemaLevel = VerifierMainWindow.getPassingLevel(schemaFailedRequirements);

            final Collection<VerificationIssue> metadataFailedRequirements   = gpkg.metadata().getVerificationIssues(VerificationLevel.Full);
            final PassingLevel metadataLevel = VerifierMainWindow.getPassingLevel(metadataFailedRequirements);

        } catch (ClassNotFoundException | SQLException| ConformanceException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
