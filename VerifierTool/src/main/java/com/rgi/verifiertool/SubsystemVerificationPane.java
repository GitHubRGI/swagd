package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationIssue;

public class SubsystemVerificationPane extends VBox
{

    private boolean hasIssues;

    private final String subsystemName;

    private final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction;

    private final ProgressIndicator progressIndicator = new ProgressIndicator();

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


        this.setStyle(String.format("-fx-background-color: %s;", whiteHex));
        Label subsystemLabel = this.prettyLabel();

        this.getChildren().addAll(subsystemLabel, this.progressIndicator);
    }

    private Label prettyLabel()
    {
        Label subsystemLabel = new Label(this.subsystemName);

        subsystemLabel.setFont(Font.font(font, FontWeight.BOLD, 16));
        subsystemLabel.setTextFill(darkAquaBlue);

        return subsystemLabel;
    }

    /**
     * @return the hasIssues
     */
    public boolean hasIssues()
    {
        return this.hasIssues;
    }

    public Collection<VerificationIssue> getIssues(final GeoPackage geoPackage)
    {
        final Collection<VerificationIssue> issues = this.issuesFunction.apply(geoPackage);

        this.hasIssues = !issues.isEmpty();

        return issues;
    }

    public void update(final Collection<VerificationIssue> issues)
    {
        if(issues != null && !issues.isEmpty())
        {
            TextFlow textBox = new TextFlow();
            textBox.setStyle(String.format("-fx-border-radius: 10 10 10 10; -fx-border-color: gray;-fx-background-radius: 10 10 10 10; -fx-background-color: %s;", greyBlueHex));

            for(final VerificationIssue issue : issues)
            {
                Text severity = getSeverityLabel(issue.getRequirement().severity());

                Text requirement = getRequirementLabel(issue.getRequirement());
                Text reason = getReasonLabel(issue.getReason());

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
        Text passed = new Text("Passed");
        passed.setFont(Font.font(font, FontWeight.BOLD, 16));
        passed.setFill(brightGreen);

        return passed;
    }

    private static Text getSeverityLabel(final Severity severity)
    {
        final Text text = new Text(String.format("%s ",severity.name()));

        text.setFill(getColor(severity));
        text.setFont(Font.font(font, FontWeight.BOLD, 14));

        return text;
    }

    private static Color getColor(final Severity severity)
    {
        switch(severity)
        {
            case Warning: return brightOrange;
            case Error:   return brightRed;

            default: return Color.BLACK;
        }
    }

    private static Text getRequirementLabel(final Requirement requirement)
    {
        final Text text = new Text();

        text.setText(String.format(" Requirement %d: \"%s\"\n",
                                    requirement.number(),
                                    requirement.text()));

        text.setFont(Font.font(font, FontWeight.EXTRA_BOLD, 12));
        text.setFill(white);

        return text;
    }

    private static Text getReasonLabel(final String reason)
    {
        final Text text = new Text(String.format("%s \n", reason));
        text.setFill(darkAquaBlue);
        text.setFont(Font.font(font, FontWeight.BOLD, 12));

        return text;
    }

    private final static String font = "SanSerif";
    private final static Paint darkAquaBlue = Color.rgb(41, 110, 163);
    private final static Paint brightGreen  = Color.rgb(0,  218,  125);
    private final static Color brightOrange = Color.rgb(255,  187,  16);
    private final static Color brightRed    = Color.rgb(217, 35, 52);
    private final static Paint white        = Color.rgb(252, 252, 253);
    private final static String whiteHex    = "#FCFCFD";
    private final static String greyBlueHex = "#9BBED6";

}
