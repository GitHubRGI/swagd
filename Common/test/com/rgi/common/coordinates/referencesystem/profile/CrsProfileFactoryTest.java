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

package com.rgi.common.coordinates.referencesystem.profile;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;

@SuppressWarnings({"javadoc", "static-method"})
public class CrsProfileFactoryTest
{
    
    /**
     * Tests if using different constructors still return the same result
     */
    @Test
    public void createCrsProfilesWithBothConstructors()
    {
        Set<CoordinateReferenceSystem> systemSet = CrsProfileFactory.getSupportedCoordinateReferenceSystems();
        
        for(CoordinateReferenceSystem crs: systemSet)
        {
            CrsProfile profile = CrsProfileFactory.create(crs);
            
            CrsProfile profileCreatedWithAuthority = CrsProfileFactory.create(crs.getAuthority(), crs.getIdentifier());
            
            assertTrue("The CoordinateReferenceSystem created with the constructor (authority, identifier) versus the constructor (CoordianteReferenceSystem) returned different CoordinateReferenceSystems.",
                       profile.equals(profileCreatedWithAuthority));
        }
    }
    /**
     * Tests if a runtime exception is thrown when it encounters a coordinate reference system that is not supported
     */
    @Test(expected = RuntimeException.class)
    public void runtimeException()
    {
        CoordinateReferenceSystem unsupportedCrs = new CoordinateReferenceSystem("Rgi-Authority", 1337);
        CrsProfileFactory.create(unsupportedCrs);
        fail("Expected a runtime exception when trying to create a CrsProfile from a coordinate reference system that is not supported.");
    }
}
