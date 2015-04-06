package com.rgi.verifiertool;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.rgi.common.util.functional.ThrowingConsumer;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

public class FileVerificationPane extends TitledPane
{
    private final VBox content = new VBox();

    public FileVerificationPane(final File geoPackageFile)
    {
        if(geoPackageFile == null || !geoPackageFile.canRead())
        {
            throw new IllegalArgumentException("GeoPackage file may not be null, and must be a valid filename");
        }

        this.setAnimated(false);
        this.setText(geoPackageFile.getName());
        this.setPrettyText();

        final List<SubsystemVerificationPane> subsystems = Arrays.asList(new SubsystemVerificationPane("Core",       (geoPackage) -> geoPackage.core()      .getVerificationIssues(geoPackage.getFile(), VerificationLevel.Full)),
                                                                         new SubsystemVerificationPane("Features",   (geoPackage) -> Collections.emptyList()),
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
                                                                                                      .map(subsystem -> new Thread(FileVerificationPane.this.createTask(subsystem, geoPackage)))
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

    private void setPrettyText()
    {
        this.setContent(this.content);
        this.setTextFill(Color.NAVY);
        this.setFont(Font.font("SanSerif", FontWeight.BOLD, 18));
    }

    private Task<Collection<VerificationIssue>> createTask(final SubsystemVerificationPane subsystemVerificationPane, final GeoPackage geoPackage)
    {
        final Task<Collection<VerificationIssue>> task = new Task<Collection<VerificationIssue>>()
                                                         {
                                                             @Override
                                                             protected Collection<VerificationIssue> call() throws Exception
                                                             {
                                                                 try
                                                                 {
                                                                     final Collection<VerificationIssue> messages = subsystemVerificationPane.getIssues(geoPackage);
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
