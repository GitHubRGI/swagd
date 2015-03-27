package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import com.rgi.geopackage.verification.VerificationIssue;

/**
 * @author Jenifer Cochran
 *
 */
public class Result
{
        Label  geoPackageLabel;
        PassingLevel passingLevel;
        Collection<VerificationIssue> failedMessages;
        FailedRequirementsButton button;
        Label passingLabel;
        ProgressIndicator indicator;

        Result(final Label label, final PassingLevel passingLevel, final FailedRequirementsButton button)
        {
            this.geoPackageLabel = label;
            this.passingLevel = passingLevel;
            this.failedMessages = button.getFailedRequirements();
            this.button = button;
            this.passingLabel = new Label();
            this.indicator = new ProgressIndicator();
            this.passingLabel.setGraphic(this.indicator);
        }

        public void setLevelAndMessage(final PassingLevel level, final Collection<VerificationIssue> failedRequirements)
        {
            this.failedMessages = failedRequirements;
            this.passingLevel = level;
        }

}
