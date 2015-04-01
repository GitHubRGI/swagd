package com.rgi.verifiertool;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

import com.rgi.geopackage.verification.VerificationIssue;

public class SubsystemVerificationPane extends HBox
{
    private final String subsystemName;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public SubsystemVerificationPane(final String subsystemName)
    {
        this.subsystemName = subsystemName;

        this.getChildren().addAll(new Label(this.subsystemName + "..."),
                                  this.progressIndicator);
    }

    public void update(final Supplier<Collection<VerificationIssue>> messagesSupplier)
    {
                Platform.runLater(() -> { final String foo = String.join("\n", messagesSupplier.get().stream().map(verificationIssue -> verificationIssue.toString()).collect(Collectors.toList()));

                                  this.getChildren().add(new Label(foo));
                                  this.getChildren().remove(this.progressIndicator);
                                });
    }
}
