/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.verifiertool;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.rgi.geopackage.verification.VerificationIssue;

/**
 * @author Jenifer Cochran
 *
 */
public class FailedRequirementsWindow extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Collection<VerificationIssue> failedRequirements;

    /**
     * @param failedRequirements failed requirements to display
     */
    public FailedRequirementsWindow(final Collection<VerificationIssue> failedRequirements)
    {
        // TODO Auto-generated constructor stub
       this.failedRequirements = failedRequirements;
       JPanel mainPanel = new JPanel();
       this.add(mainPanel);
       mainPanel.setSize(500, 500);
       mainPanel.setLayout(new BorderLayout());

       JTextArea errorMessages = new JTextArea(this.getMessage());
       JScrollPane scrollPane = new JScrollPane(errorMessages);
       mainPanel.add(scrollPane, BorderLayout.CENTER);
       this.setSize(500, 500);
       this.setVisible(true);
    }

    private String getMessage()
    {
        return this.failedRequirements.stream()
                                             .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                                             .map(failedRequirement -> String.format("(%s) Requirement %d: \"%s\"\n%s\n",
                                                                                        failedRequirement.getRequirement().severity(),
                                                                                        failedRequirement.getRequirement().number(),
                                                                                        failedRequirement.getRequirement().text(),
                                                                                        failedRequirement.getReason()))
                                             .collect(Collectors.joining("\n"));
    }

}
