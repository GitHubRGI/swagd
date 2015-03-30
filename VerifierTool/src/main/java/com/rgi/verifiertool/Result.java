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
        private final Label                         geoPackageLabel;
        private PassingLevel                  passingLevel;
        private Collection<VerificationIssue> failedMessages;
        private final FailedRequirementsButton      button;
        private final Label                         passingLabel;
        private final ProgressIndicator             indicator;

        Result(final Label label, final PassingLevel passingLevel, final FailedRequirementsButton button)
        {
            this.geoPackageLabel = label;
            this.setPassingLevel(passingLevel);
            this.setFailedMessages(button.getFailedRequirements());
            this.button = button;
            this.passingLabel = new Label();
            this.indicator       = new ProgressIndicator();
            this.getPassingLabel().setGraphic(this.indicator);
        }

        public Label getGeoPackageLabel()
        {
            return this.geoPackageLabel;
        }

        public PassingLevel getPassingLevel()
        {
            return this.passingLevel;
        }

        public void setPassingLevel(final PassingLevel passingLevel)
        {
            this.passingLevel = passingLevel;
        }

        public Collection<VerificationIssue> getFailedMessages()
        {
            return this.failedMessages;
        }

        public void setFailedMessages(final Collection<VerificationIssue> failedMessages)
        {
            this.failedMessages = failedMessages;
        }

        public FailedRequirementsButton getButton()
        {
            return this.button;
        }

        public Label getPassingLabel()
        {
            return this.passingLabel;
        }
}
