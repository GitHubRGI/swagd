package com.rgi.verifiertool;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.rgi.common.util.functional.ThrowingConsumer;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * Pane representing a single GeoPackage file's verification output
 *
 * @author Luke Lambert
 * @author Jenifer Cochran
 *
 */
public class FileVerificationPane extends TitledPane
{
    private final VBox content = new VBox();
    private final ContextMenu deleteMenu = new ContextMenu();
    private VBox parent;
    private final HashMap<String, Collection<VerificationIssue>> fileErrorMessages = new HashMap<>();



    /**
     * Constructor
     *
     * @param geoPackageFile
     *             File handle to a GeoPackage file
     */
    public FileVerificationPane(final File geoPackageFile)
    {
        if(geoPackageFile == null || !geoPackageFile.canRead())
        {
            throw new IllegalArgumentException("GeoPackage file may not be null, and must be a valid filename");
        }

        this.setAnimated(false);
        this.setText(geoPackageFile.getName());
        this.setPrettyText();
        this.content.setStyle(String.format("-fx-background-color: %s;", Style.white.getHex()));
        this.createContextMenu();
        this.setOnMousePressed(e -> this.createDeleteListener(e));
        this.content.getChildren().add(this.createClipBoardButton());

        final List<SubsystemVerificationPane> subsystems = Arrays.asList(new SubsystemVerificationPane("Core",       (geoPackage) -> geoPackage.core()      .getVerificationIssues(geoPackage.getFile(), VerificationLevel.Full)),
                                                                         //new SubsystemVerificationPane("Features",   (geoPackage) -> Collections.emptyList()),
                                                                         new SubsystemVerificationPane("Tiles",      (geoPackage) -> geoPackage.tiles()     .getVerificationIssues(VerificationLevel.Full)),
                                                                         new SubsystemVerificationPane("Extensions", (geoPackage) -> geoPackage.extensions().getVerificationIssues(VerificationLevel.Full)),
                                                                         new SubsystemVerificationPane("Schema",     (geoPackage) -> geoPackage.schema()    .getVerificationIssues(VerificationLevel.Full)),
                                                                         new SubsystemVerificationPane("Metadata",   (geoPackage) -> geoPackage.metadata()  .getVerificationIssues(VerificationLevel.Full)));

        this.content.getChildren().addAll(subsystems);

        final Thread mainThread = new Thread(new Task<Void>()
                                             {
                                                 @Override
                                                 protected Void call() throws Exception
                                                 {
                                                     try(final GeoPackage geoPackage = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Open))
                                                     {
                                                         final List<Thread> updateThreads = subsystems.stream()
                                                                                                      .map(subsystem -> new Thread(FileVerificationPane.createTask(subsystem, geoPackage, FileVerificationPane.this.fileErrorMessages)))
                                                                                                      .collect(Collectors.toList());

                                                         updateThreads.forEach(thread -> thread.start());

                                                         updateThreads.forEach((ThrowingConsumer<Thread>)(thread -> thread.join()));
                                                     }
                                                     catch(final Exception ex)
                                                     {
                                                         throw new RuntimeException(ex);
                                                     }

                                                     return null;
                                                 }
                                             });
        mainThread.start();
    }

    private Button createClipBoardButton()
    {
        final Image     clipBoard     = new Image(FileVerificationPane.class.getResourceAsStream("Clipboard_Icon.png"));

        final ImageView clipBoardView = new ImageView(clipBoard);
        clipBoardView.setFitHeight(19);
        clipBoardView.setFitWidth(19);

        final Button    copyButton    = new Button();
        copyButton.setMaxSize(24, 24);
        copyButton.setMinSize(24, 24);
        copyButton.setStyle(String.format("-fx-border-radius: 2 2 2 2;"
                                        + "-fx-background-radius: 2 2 2 2; "));

        final Tooltip copyMessage = new Tooltip("Copy Message");
        Tooltip.install(copyButton, copyMessage);

        copyButton.setGraphic(clipBoardView);
        copyButton.setOnAction(e-> {
                                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                                        final ClipboardContent clipboardContent = new ClipboardContent();
                                        clipboard.clear();
                                        clipboardContent.clear();
                                        clipboardContent.putString(this.getVerificationIssues());
                                        clipboard.setContent(clipboardContent);
                                    });

        return copyButton;
    }

    private String getVerificationIssues()
    {
        String header = String.format("RGi\u00AE GeoPackage Verifier Tool Version %s.\nGeoPackage Encoding Standard Specification Version %s.\nFile Assessed: %s\n\n\n",
                                      VerifierMainWindow.rgiToolVersionNumber,
                                      VerifierMainWindow.geoPackageSpecificationVersionNumber,
                                      this.getText());

        String body = this.fileErrorMessages.keySet().stream()
                                                     .sorted((subsystem1, susbystem2) -> compareSubsystems(subsystem1, susbystem2))
                                                     .map(subsystem -> {
                                                                           return getVerificationIssuesForSubsystem(subsystem, this.fileErrorMessages.get(subsystem));
                                                                       })
                                                     .collect(Collectors.joining("\n"));
        if(!body.isEmpty())
        {
            return String.format("%s\n%s", header, body);
        }

        return String.format("%s\n\n\nNo Errors found.", header);
    }

    private static Integer compareSubsystems(final String subsystem1, final String susbystem2)
    {
        int firstSubInt = findSubsystemValue(subsystem1);
        int seconSubInt = findSubsystemValue(susbystem2);

        return Integer.compare(firstSubInt, seconSubInt);
    }

    private static int findSubsystemValue(final String subsystem1)
    {
        switch(subsystem1)
        {
            case "Core":       return 0;
            case "Tiles":      return 1;
            case "Extensions": return 2;
            case "Schema" :    return 3;
            case "Metadata" :  return 4;

            default: return 5;
        }
    }

    private static String getVerificationIssuesForSubsystem(final String subsystemName, final Collection<VerificationIssue> issues)
    {
          final String failedMessages = issues.stream()
                                        .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                                        .map(issue -> {
                                                        return String.format("\t\r(%s) Requirement %d: \"%s\"\n\n\t\r%s\n",
                                                                             issue.getRequirement().severity(),
                                                                             issue.getRequirement().number(),
                                                                             issue.getRequirement().text(),
                                                                             issue.getReason());
                                                      })
                                        .collect(Collectors.joining("\n"));

         return  String.format("%s Issues:\n\n%s",
                                subsystemName,
                                failedMessages);
    }

    private void createDeleteListener(final MouseEvent e)
    {
        if(e.isSecondaryButtonDown())
        {
            this.deleteMenu.show(this, e.getScreenX(), e.getScreenY());
        }
    }

    /**
     * @param parent the parent that this pane is added to
     */
    public void setParent(final VBox parent)
    {
        this.parent = parent;
    }

    private void createContextMenu()
    {
        final MenuItem remove = new MenuItem("Remove");
        final MenuItem cancel = new MenuItem("Cancel");
        this.deleteMenu.getItems().addAll(remove, cancel);

        remove.setOnAction(e -> {
                                if(this.parent != null)
                                  {
                                      this.parent.getChildren().remove(this);
                                  }
                                });
    }

    private void setPrettyText()
    {
        this.setContent(this.content);
        this.setTextFill(Style.brightBlue.toColor());
        this.setFont(Font.font(Style.getFont(), FontWeight.BOLD, 18));
        this.setStyle(String.format("-fx-body-color: %s;", Style.white.getHex()));
    }

    private static Task<Collection<VerificationIssue>> createTask(final SubsystemVerificationPane subsystemVerificationPane, final GeoPackage geoPackage, final HashMap<String, Collection<VerificationIssue>> fileErrorMessages2)
    {
        final Task<Collection<VerificationIssue>> task = new Task<Collection<VerificationIssue>>()
                                                         {
                                                             @Override
                                                             protected Collection<VerificationIssue> call() throws Exception
                                                             {
                                                                 try
                                                                 {
                                                                     final Collection<VerificationIssue> messages = subsystemVerificationPane.getIssues(geoPackage);
                                                                     if(!messages.isEmpty())
                                                                     {
                                                                         fileErrorMessages2.put(subsystemVerificationPane.getName(), messages);
                                                                     }
                                                                     this.updateValue(messages);
                                                                     return messages;
                                                                 }
                                                                 catch(final Exception ex)
                                                                 {
                                                                     return null;
                                                                 }

                                                             }
                                                         };

        task.valueProperty().addListener((ChangeListener<Collection<VerificationIssue>>)(observable, oldValue, newValue) -> subsystemVerificationPane.update(newValue));

        return task;
    }
}
