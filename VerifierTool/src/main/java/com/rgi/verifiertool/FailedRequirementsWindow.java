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

import java.util.Collection;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationIssue;

/**
 * @author Jenifer Cochran
 *
 */

public class FailedRequirementsWindow extends Stage
{
    TextFlow text = new TextFlow();

    private final Collection<VerificationIssue> failedRequirements;
    /**
     * @param failedRequirements failed requirements to display
     * @param Component The component that was failing
     */
    public FailedRequirementsWindow(final Collection<VerificationIssue> failedRequirements, final String Component)
    {
       super();
       this.setTitle(String.format("Failed Requirements for %s", Component));

       //set the failed requirements passed in
       this.failedRequirements = failedRequirements;
       this.createMessage();
       //create a scroll to scan through error messages
       ScrollPane scrollPane = new ScrollPane(this.text);
       //allow the pane to resize to main window
       Scene scene = new Scene(scrollPane, 400, 500);
       scrollPane.setFitToWidth(true);
       //create the window set up
       this.setScene(scene);
       this.show();
    }

    private void createMessage()
    {
       this.failedRequirements.stream()
                              .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                              .forEach(failedRequirement ->
                                                    {
                                                      Text severity = new Text(String.format("(%s) ",failedRequirement.getRequirement().severity()));

                                                      if(Severity.Error == failedRequirement.getRequirement().severity())
                                                      {
                                                          severity.setFill(Color.RED);
                                                      }
                                                      else
                                                      {
                                                          severity.setFill(Color.ORANGE);
                                                      }
                                                      severity.setFont(Font.font(null, FontWeight.BOLD, 12));

                                                      Text requirement = new Text(String.format("Requirement %d: \"%s\"\n\n", failedRequirement.getRequirement().number(), failedRequirement.getRequirement().text() ));
                                                      requirement.setFont(Font.font(null, FontWeight.BOLD, 12));

                                                      Text reason = new Text(String.format("%s\n\n",failedRequirement.getReason()));

                                                      this.text.getChildren().addAll(severity, requirement, reason);
                                                  });

    }

}
