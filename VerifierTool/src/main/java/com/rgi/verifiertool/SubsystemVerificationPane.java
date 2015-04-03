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

        Label subsystemLabel = this.prettyLabel();

        this.getChildren().addAll(subsystemLabel,
                                  this.progressIndicator);
    }

    private Label prettyLabel()
    {
        Label subsystemLabel = new Label(this.subsystemName);

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
            textBox.setStyle("-fx-border-radius: 10 10 10 10; -fx-border-color: gray; -fx-background-color: white;");

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

        this.getChildren().remove(this.progressIndicator);
    }

    private static Text getPassText()
    {
        Text passed = new Text("Passed");
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
