package com.rgi.verifiertool;

import java.util.Collection;
import java.util.stream.Collectors;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.VerificationIssue;

public class SubsystemVerificationPane extends HBox
{
    private final String subsystemName;

    private final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> messagesFunction;

    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public SubsystemVerificationPane(final String subsystemName, final ThrowingFunction<GeoPackage, Collection<VerificationIssue>> messagesFunction)
    {
        this.subsystemName    = subsystemName;
        this.messagesFunction = messagesFunction;

        this.getChildren().addAll(new Label(this.subsystemName + "..."),
                                  this.progressIndicator);
    }

    public Collection<VerificationIssue> getMessages(final GeoPackage geoPackage)
    {
        return this.messagesFunction.apply(geoPackage);
    }

    public void update(final Collection<VerificationIssue> messages)
    {
        //final Collection<VerificationIssue> messages = this.messagesFunction.apply(geoPackage);

        try
        {
            if(messages != null && !messages.isEmpty())
            {
                final String foo = String.join("\n", messages.stream()
                                                             .map(verificationIssue -> verificationIssue.getReason())
                                                             .collect(Collectors.toList()));

                this.getChildren().add(new Label(foo));
            }
        }
        catch(final Throwable th)
        {
            th.printStackTrace();
        }
        finally
        {
            this.getChildren().remove(this.progressIndicator);
        }
    }
}
