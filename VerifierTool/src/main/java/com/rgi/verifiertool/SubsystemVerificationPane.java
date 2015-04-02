package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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

        this.getChildren().addAll(new Label(this.subsystemName),
                                  this.progressIndicator);
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
            final GridPane issueGrid = new GridPane();

            final int row = 0;

            for(final VerificationIssue issue : issues)
            {
                issueGrid.add(getSeverityLabel(issue.getRequirement().severity()), 0, row*2);

                issueGrid.add(getRequirementLabel(issue.getRequirement()), 1, row*2);
                issueGrid.add(getReasonLabel     (issue.getReason()),      1, row*2+1);
            }

            this.getChildren().add(issueGrid);
        }

        this.getChildren().remove(this.progressIndicator);
    }

    private static Label getSeverityLabel(final Severity severity)
    {
        final Label label = new Label(severity.name());

        label.setTextFill(getColor(severity));

        return label;
    }

    private static Color getColor(final Severity severity)
    {
        switch(severity)
        {
            case Warning: return Color.YELLOW;
            case Error:   return Color.RED;

            default: return Color.BLACK;
        }
    }

    private static Label getRequirementLabel(final Requirement requirement)
    {
        final Label label = new Label();

        label.setWrapText(true);

        label.setText(String.format("Requirement %d: \"%s\"",
                                    requirement.number(),
                                    requirement.text()));
        return label;
    }

    private static Label getReasonLabel(final String reason)
    {
        final Label label = new Label(reason);

        label.setWrapText(true);

        return label;
    }
}
