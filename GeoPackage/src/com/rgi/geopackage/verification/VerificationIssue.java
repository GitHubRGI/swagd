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

/**
 * @author Luke Lambert
 *
 */
public class VerificationIssue
{
    /**
     * Constructor
     *
     * @param message
     *             The explanation of how a GeoPackage didn't conform to a specific requirement
     * @param requirement
     *             The requirement that the GeoPackage didn't fully conform to
     */
    public VerificationIssue(final String message, final Requirement requirement)
    {
        this.message     = message;
        this.requirement = requirement;
    }

    /**
     * @return the message
     */
    public String getReason()
    {
        return this.message;
    }
    /**
     * @return the requirement
     */
    public Requirement getRequirement()
    {
        return this.requirement;
    }

    final private String      message;
    final private Requirement requirement;
}
