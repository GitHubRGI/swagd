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

package com.rgi.geopackage.features;

import java.sql.Connection;

import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

/**
 * @author Jenifer Cochran
 * @author Luke Lambert
 *
 */
public class FeaturesVerifier extends Verifier
{
    /**
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @param sqliteConnection
     *             A connection handle to the database
     */
    public FeaturesVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel)
    {
        super(sqliteConnection, verificationLevel);
    }

    // TODO
}
