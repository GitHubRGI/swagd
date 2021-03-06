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

package com.rgi.geopackage.verification;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 *
 */
@SuppressWarnings("serial")
public class ConformanceException extends Exception
{
    /**
     * @param verificationIssue a collection of all the failed requirements of the GeoPackage that did not pass
     */
    public ConformanceException(final Collection<VerificationIssue> verificationIssue)
    {
        this.verificationIssues = verificationIssue;
    }

    @Override
    public String getMessage()
    {
        return this.toString();
    }

    @Override
    public String toString()
    {
        return String.format("GeoPackage failed to meet the following requirements:\n%s",
                             this.verificationIssues.stream()
                                                    .sorted((issue1, issue2) -> issue1.getRequirement()
                                                                                      .reference()
                                                                                      .compareTo(issue2.getRequirement()
                                                                                                       .reference()))
                                                    .map(verificationIssue -> String.format("* (%s) %s: \"%s\"\n%s",
                                                                                            verificationIssue.getSeverity().name(),
                                                                                            verificationIssue.getRequirement().reference(),
                                                                                            verificationIssue.getRequirement().text(),
                                                                                            verificationIssue.getReason()))
                                                    .collect(Collectors.joining("\n\n")));
    }

    private final Collection<VerificationIssue> verificationIssues;
}
