package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationIssue;

/**
 * Pane representing a single GeoPackage subsystem's verification output
 *
 * @author Luke Lambert
 *
 */
public class SubsystemVerificationPane extends VBox
{
    private boolean hasIssues;

    private final String subsystemName;

    private final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction;

    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    /**
     * Constructor
     *
     * @param subsystemName
     *             Name of GeoPackage subsystem
     * @param issuesFunction
     *             Function to be called later that gather's the verification
     *             messages from the subsystem
     */
    public SubsystemVerificationPane(final String subsystemName, final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction)
    {
        if(subsystemName == null || subsystemName.isEmpty())
        {
            throw new IllegalArgumentException("Subsystem name may not be null or empty");
        }

        if(issuesFunction == null)
        {
            throw new IllegalArgumentException("Funtional callback to get verification issue messages may not be null");
        }

        this.subsystemName  = subsystemName;
        this.issuesFunction = issuesFunction;

        final Label subsystemLabel = this.prettyLabel();

        this.getChildren().addAll(subsystemLabel,
                                  this.progressIndicator);
    }

    private Label prettyLabel()
    {
        final Label subsystemLabel = new Label(this.subsystemName);

        subsystemLabel.setFont(Font.font("SanSerif", FontWeight.BOLD, 16));
        subsystemLabel.setTextFill(Color.DARKSLATEBLUE);

        return subsystemLabel;
    }

    /**
     * @return the hasIssues
     */
    public boolean hasIssues()
    {
        return this.hasIssues;
    }

    /**
     * Returns the verification issues for this subsystem
     *
     * @param geoPackage
     *             GeoPackage to verify
     * @return returns a {@link Collection} of {@link VerificationIssue}s
     */
    public Collection<VerificationIssue> getIssues(final GeoPackage geoPackage)
    {
        final Collection<VerificationIssue> issues = this.issuesFunction.apply(geoPackage);

        this.hasIssues = !issues.isEmpty();

        return issues;
    }

    /**
     * Processes the verification issues, and updates the UI as necessary
     *
     * @param issues
     *             Verification issues for the subsystem of a GeoPackage
     */
    public void update(final Collection<VerificationIssue> issues)
    {
        if(issues != null && !issues.isEmpty())
        {
            final TextFlow textBox = new TextFlow();
            textBox.setStyle("-fx-border-color: gray;");

            for(final VerificationIssue issue : issues)
            {
                final Text severity = getSeverityLabel(issue.getRequirement().severity());

                final Text requirement = getRequirementLabel(issue.getRequirement());
                final Text reason = getReasonLabel(issue.getReason());

                textBox.getChildren().addAll(severity, requirement, reason);
            }

            this.getChildren().add(textBox);
        }
        else
        {
            //add pass
            this.getChildren().add(getPassText());
        }

        this.getChildren().remove(this.progressIndicator);
    }

    private static Text getPassText()
    {
        final Text passed = new Text("Passed");
        passed.setFont(Font.font("SanSerif", FontWeight.BOLD, 16));
        passed.setFill(Color.GREEN);

        return passed;
    }

    private static Text getSeverityLabel(final Severity severity)
    {
        final Text text = new Text(severity.name());

        text.setFill(getColor(severity));
        text.setFont(Font.font("SanSerif", FontWeight.BOLD, 12));

        return text;
    }

    private static Color getColor(final Severity severity)
    {
        switch(severity)
        {
            case Warning: return Color.ORANGE;
            case Error:   return Color.RED;

            default: return Color.BLACK;
        }
    }

    private static Text getRequirementLabel(final Requirement requirement)
    {
        final Text text = new Text();

        text.setText(String.format(" Requirement %d: \"%s\"\n\n",
                                    requirement.number(),
                                    requirement.text()));

        text.setFont(Font.font("SanSerif", FontWeight.BOLD, 12));

        return text;
    }

    private static Text getReasonLabel(final String reason)
    {
        final Text text = new Text(String.format("%s \n\n", reason));

        return text;
    }
}
