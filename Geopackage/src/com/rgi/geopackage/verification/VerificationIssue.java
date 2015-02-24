/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
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
