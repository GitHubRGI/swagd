package com.rgi.verifiertool;

import java.util.Collection;

import javafx.scene.control.Label;

import com.rgi.geopackage.verification.VerificationIssue;

/**
 * @author Jenifer Cochran
 *
 */
public class Result
{
        Label  label;
        PassingLevel passingLevel;
        Collection<VerificationIssue> failedMessages;
        FailedRequirementsButton button;

        Result(final Label label, final PassingLevel passingLevel, final Collection<VerificationIssue> failedMessages, final FailedRequirementsButton button)
        {
            this.label = label;
            this.passingLevel = passingLevel;
            this.failedMessages = failedMessages;
            this.button = button;
        }

}
