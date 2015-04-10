package com.rgi.verifiertool;

import java.util.Collection;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
 * @author Jenifer Cochran
 *
 */
public class SubsystemVerificationPane extends VBox
{
    private boolean hasIssues;

    private final String subsystemName;

    private final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction;

    private final GridPane gridPane = new GridPane();

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
        super(5);
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
        this.setStyle(String.format("-fx-background-color: %s;", Style.greyBlue.getHex()));
        this.setPadding(new Insets(7));

        this.progressIndicator.setMinSize(25, 25);
        this.progressIndicator.setMaxSize(25, 25);

        final Label subsystemLabel = this.prettyLabel();

        this.gridPane.add(subsystemLabel, 0, 0);
        this.gridPane.add(this.progressIndicator, 1, 0);
        //Columns for the top label
        final ColumnConstraints columnLeft   = new ColumnConstraints(98);
        final ColumnConstraints columnCenter = new ColumnConstraints(200);

        this.gridPane.getColumnConstraints().addAll(columnLeft,columnCenter);
        this.gridPane.setVgap(10);

        this.getChildren().add(this.gridPane);
    }

    private Label prettyLabel()
    {
        final Label subsystemLabel = new Label(this.subsystemName);

        subsystemLabel.setFont(Font.font(Style.getMainFont(), FontWeight.BOLD, 18));
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
        this.gridPane.getChildren().remove(this.progressIndicator);

        if(issues != null && !issues.isEmpty())
        {
            final TextFlow textBox = new TextFlow();

            textBox.setStyle(String.format("-fx-padding: 5;-fx-border-radius: 10; -fx-border-color: gray;-fx-background-radius: 10; -fx-background-color: %s;", Style.darkAquaBlue.getHex()));

            for(final VerificationIssue issue : issues)
            {
                final Text severity    = getSeverityText   (issue.getRequirement().severity());
                final Text requirement = getRequirementText(issue.getRequirement());
                final Text reason      = getReasonText     (issue.getReason());

                textBox.getChildren().addAll(severity, requirement, reason);
            }
            this.gridPane.add(getSeverityLevel(issues), 1, 0);
            this.getChildren().add(textBox);
        }
        else
        {
            //add pass
            this.gridPane.add(createIconAndText(getPassText(), "passedIcon.png", Style.lightGreen), 1, 0);

        }
        this.snapshot(new SnapshotParameters(), new WritableImage(1,1));//added to refresh scroll pane

    }


    private static Node getSeverityLevel(final Collection<VerificationIssue> issues)
    {
        final boolean hasError = issues.stream().anyMatch(issue -> issue.getRequirement().severity().equals(Severity.Error));

        //add correct label
        if(hasError)
        {
            return createIconAndText(getSeverityText(Severity.Error), "errorIcon.png", Style.lightRed);
        }

        return createIconAndText(getSeverityText(Severity.Warning), "WarningIcon.png", Style.lightOrange);
    }

    private static Node createIconAndText(final Text passingLevel, final String imageFileName, final Style backgroundColor)
    {
        //set up pane
        GridPane severityLevelPane = new GridPane();
        final ColumnConstraints columnLeft   = new ColumnConstraints();
        final ColumnConstraints columnCenter = new ColumnConstraints();
        severityLevelPane.setVgap(5);
        severityLevelPane.setHgap(5);
        severityLevelPane.getColumnConstraints().addAll(columnLeft,columnCenter);

        ImageView errorImage = new ImageView( new Image(SubsystemVerificationPane.class.getResourceAsStream(imageFileName)));
        errorImage.setFitHeight(20);
        errorImage.setFitWidth(20);
        severityLevelPane.add(passingLevel, 1, 0);
        severityLevelPane.add(errorImage, 0, 0);
        severityLevelPane.setMaxSize(80 + errorImage.getFitWidth(), passingLevel.getLayoutBounds().getHeight() + errorImage.getFitHeight());

        severityLevelPane.setStyle(String.format("-fx-padding: 5;-fx-border-radius: 10; -fx-border-color: grey;-fx-background-radius: 10; -fx-background-color: %s;", backgroundColor.getHex()));

        return severityLevelPane;

    }

    private static Text getPassText()
    {
        final Text passed = new Text("Passed");

        passed.setFont(Font.font(Style.getMainFont(), FontWeight.BOLD, 14));
        passed.setFill(Style.brightGreen.toColor());

        return passed;
    }

    private static Text getSeverityText(final Severity severity)
    {
        final Text text = new Text(String.format("%s ",severity.name()));

        text.setFill(getColor(severity));
        text.setFont(Font.font(Style.getMainFont(), FontWeight.BOLD, 14));

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

    private static Text getRequirementText(final Requirement requirement)
    {
        final Text text = new Text();

        text.setText(String.format(" Requirement %d: \"%s\"\n\n",
                                    requirement.number(),
                                    requirement.text()));

        text.setFont(Font.font(Style.getMainFont(), FontWeight.EXTRA_BOLD, 12));
        text.setFill(Style.white.toColor());

        return text;
    }

    private static Text getReasonText(final String reason)
    {
        final Text text = new Text(String.format("%s \n", reason));
        text.setFill(Style.greyBlue.toColor());
        text.setFont(Font.font(Style.getFixedWidthFont(), FontWeight.EXTRA_BOLD, 13));

        return text;
    }

    /**
     * @return
     *         The Name of the Subsystem (i.e. Core, Tiles, etc..)
     */
    public String getName()
    {
        return this.subsystemName;
    }
}
