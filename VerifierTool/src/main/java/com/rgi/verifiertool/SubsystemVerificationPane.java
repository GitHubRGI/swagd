package com.rgi.verifiertool;

import java.util.Collection;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
     * @param subsystemName The Subsystem of the GeoPackage that is being tested (i.e. Core, Tiles..)
     * @param issuesFunction The function that will call the subsystem's verification method (to run the verifier and collect the error messages)
     */
    public SubsystemVerificationPane(final String subsystemName, final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> issuesFunction)
    {
        super(3);
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
        this.setPadding(new Insets(7));

        final Label subsystemLabel = this.prettyLabel();

        this.progressIndicator.setMinSize(25, 25);
        this.progressIndicator.setMaxSize(25, 25);

        this.gridPane.add(subsystemLabel, 0, 0);
        this.gridPane.add(this.progressIndicator, 1, 0);
        //Columns for the top label
        ColumnConstraints columnLeft   = new ColumnConstraints(90);
        ColumnConstraints columnCenter = new ColumnConstraints(360);
        ColumnConstraints columnRight  = new ColumnConstraints(25, 25, 25);

        this.gridPane.getColumnConstraints().addAll(columnLeft,columnCenter, columnRight);
        this.gridPane.setVgap(10);

        this.getChildren().add(this.gridPane);
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
        this.gridPane.getChildren().remove(this.progressIndicator);

        if(issues != null && !issues.isEmpty())
        {
            final TextFlow textBox = new TextFlow();
            textBox.setStyle(String.format("-fx-border-radius: 10 10 10 10; -fx-border-color: gray;-fx-background-radius: 10 10 10 10; -fx-background-color: %s;", Style.darkAquaBlue.getHex()));

            for(final VerificationIssue issue : issues)
            {
                final Text severity    = getSeverityLabel(issue.getRequirement().severity());
                final Text requirement = getRequirementLabel(issue.getRequirement());
                final Text reason      = getReasonLabel(issue.getReason());

                textBox.getChildren().addAll(severity, requirement, reason);
            }


            this.gridPane.add(getSeverityLevel(issues), 1, 0);
            this.gridPane.add(this.createClipBoardButton(issues), 2, 0);
            this.getChildren().add(textBox);
        }
        else
        {
            //add pass
            this.gridPane.add(getPassText(), 1, 0);

        }
        this.snapshot(new SnapshotParameters(), new WritableImage(1,1));//added to refresh scroll pane

    }

    private Node createClipBoardButton(final Collection<VerificationIssue> issues)
    {
        Image     clipBoard     = new Image(SubsystemVerificationPane.class.getResourceAsStream("Clipboard_Icon.png"));

        ImageView clipBoardView = new ImageView(clipBoard);
        clipBoardView.setFitHeight(19);
        clipBoardView.setFitWidth(19);

        Button    copyButton    = new Button();
        copyButton.setMaxSize(24, 24);
        copyButton.setMinSize(24, 24);
        copyButton.setStyle(String.format("-fx-border-radius: 2 2 2 2;"
                                        + "-fx-background-radius: 2 2 2 2; "));

        Tooltip copyMessage = new Tooltip("Copy Message");
        Tooltip.install(copyButton, copyMessage);

        copyButton.setGraphic(clipBoardView);
        copyButton.setOnAction(e-> {
                                        Clipboard clipboard = Clipboard.getSystemClipboard();
                                        ClipboardContent content = new ClipboardContent();
                                        content.putString(this.getVerificationIssues(issues));
                                        clipboard.setContent(content);
                                    });

        return copyButton;
    }

    private String getVerificationIssues(final Collection<VerificationIssue> issues)
    {
          String failedMessages = issues.stream()
                                        .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                                        .map(issue -> {
                                                        return String.format("(%s) Requirement %d: \"%s\"\n\n%s\n",
                                                                             issue.getRequirement().severity(),
                                                                             issue.getRequirement().number(),
                                                                             issue.getRequirement().text(),
                                                                             issue.getReason());
                                                      })
                                        .collect(Collectors.joining("\n"));

         return  String.format("RGi GeoPackage Verfier Tool Version %.1f.\n\n\nGeoPackage failed to meet the following requirements for GeoPackage %s:\n\n%s",
                                VerifierMainWindow.versionNumber,
                                this.subsystemName,
                                failedMessages);
    }

    private static Node getSeverityLevel(final Collection<VerificationIssue> issues)
    {
        boolean hasError = issues.stream().anyMatch(issue -> issue.getRequirement().severity().equals(Severity.Error));

        if(hasError)
        {
            return getSeverityLabel(Severity.Error);
        }

        return getSeverityLabel(Severity.Warning);
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
        final Text text = new Text(String.format("%s \n", reason));
        text.setFill(Style.greyBlue.toColor());
        text.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 12));

        return text;
    }
}
