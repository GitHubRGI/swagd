package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.WritableImage;
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
 * @author Luke Lambert
 * @author Jenifer Cochran
 *
 */
public class SubsystemVerificationPane extends VBox
{

    private boolean hasIssues;

    private final String subsystemName;

    private final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction;

    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    /**
     * @param subsystemName The Subsystem of the GeoPackage that is being tested (i.e. Core, Tiles..)
     * @param issuesFunction The function that will call the subsystem's verification method (to run the verifier and collect the error messages)
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
        this.setStyle(String.format("-fx-background-color: %s;", Style.white.getHex()));

        final Label subsystemLabel = this.prettyLabel();

        this.progressIndicator.setPrefSize(10, 10);

        this.getChildren().addAll(subsystemLabel, this.progressIndicator);
    }

    private Label prettyLabel()
    {
        final Label subsystemLabel = new Label(this.subsystemName);

        subsystemLabel.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 16));
        subsystemLabel.setTextFill(Style.darkAquaBlue.toColor());

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
     * @param geoPackage The GeoPackage file that is being tested
     * @return The Collection of Verification issues that contain the error messages
     */
    public Collection<VerificationIssue> getIssues(final GeoPackage geoPackage)
    {
        final Collection<VerificationIssue> issues = this.issuesFunction.apply(geoPackage);

        this.hasIssues = !issues.isEmpty();

        return issues;
    }

    /**
     * @param issues The Verification Error messages from the tested GeoPackage (for each subsystem)
     */
    public void update(final Collection<VerificationIssue> issues)
    {
        if(issues != null && !issues.isEmpty())
        {
            final TextFlow textBox = new TextFlow();
            textBox.setStyle(String.format("-fx-border-radius: 10 10 10 10; -fx-border-color: gray;-fx-background-radius: 10 10 10 10; -fx-background-color: %s;", Style.darkAquaBlue.getHex()));

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
        this.snapshot(new SnapshotParameters(), new WritableImage(1,1));//added to refresh scroll pane
        this.getChildren().remove(this.progressIndicator);
    }

    private static Text getPassText()
    {
        final Text passed = new Text("Passed");
        passed.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 16));
        passed.setFill(Style.brightGreen.toColor());

        return passed;
    }

    private static Text getSeverityLabel(final Severity severity)
    {
        final Text text = new Text(String.format("%s ",severity.name()));

        text.setFill(getColor(severity));
        text.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 14));

        return text;
    }

    private static Color getColor(final Severity severity)
    {
        switch(severity)
        {
            case Warning: return Style.brightOrange.toColor();
            case Error:   return Style.brightRed.toColor();

            default: return Color.BLACK;
        }
    }

    private static Text getRequirementLabel(final Requirement requirement)
    {
        final Text text = new Text();

        text.setText(String.format(" Requirement %d: \"%s\"\n",
                                    requirement.number(),
                                    requirement.text()));

        text.setFont(Font.font(Style.getFont(), FontWeight.EXTRA_BOLD, 12));
        text.setFill(Style.white.toColor());

        return text;
    }

    private static Text getReasonLabel(final String reason)
    {
        final Text text = new Text(String.format("%s \n\n", reason));
        text.setFill(Style.greyBlue.toColor());
        text.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 12));

        return text;
    }
}
