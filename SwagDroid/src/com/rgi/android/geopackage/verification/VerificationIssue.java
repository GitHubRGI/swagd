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

package com.rgi.android.geopackage.verification;

/**
 * @author Luke Lambert
 *
 */
public class VerificationIssue implements Comparable<VerificationIssue>
{
    /**
     * Constructor for unexpected non-assertion error  issues in the
     * verification process
     *
     * @param reason
     *             The explanation of how a GeoPackage didn't conform to a specific requirement
     * @param requirement
     *             The requirement that the GeoPackage didn't fully conform to
     */
    public VerificationIssue(final String reason, final Requirement requirement)
    {
        this(reason, requirement, Severity.Unknown);
    }

    /**
     * Constructor
     *
     * @param reason
     *             The explanation of how a GeoPackage didn't conform to a
     *             specific requirement
     * @param requirement
     *             The requirement that the GeoPackage didn't fully conform to
     * @param severity
     *             Indication of the level of problem encountered when testing
     *             the conformity of a file to the
     *             <a href="http://www.geopackage.org/spec/">GeoPackage
     *             Standards</a> specification
     */
    public VerificationIssue(final String      reason,
                             final Requirement requirement,
                             final Severity    severity)
    {
        this.reason      = reason;
        this.requirement = requirement;
        this.severity    = severity;
    }

    @Override
    public int compareTo(final VerificationIssue o)
    {
        return this.requirement.reference().compareTo(o.getRequirement().reference());
    }

    /**
     * @return the message
     */
    public String getReason()
    {
        return this.reason;
    }
    /**
     * @return the requirement
     */
    public Requirement getRequirement()
    {
        return this.requirement;
    }

    /**
     * @return the severity
     */
    public Severity getSeverity()
    {
        return this.severity;
    }

    final private String      reason;
    final private Requirement requirement;
    final private Severity    severity;
}
