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
        final Set<CoordinateReferenceSystem> systemSet = CrsProfileFactory.getSupportedCoordinateReferenceSystems();

        for(final CoordinateReferenceSystem crs: systemSet)
        {
            final CrsProfile profile = CrsProfileFactory.create(crs);

            final CrsProfile profileCreatedWithAuthority = CrsProfileFactory.create(crs.getAuthority(), crs.getIdentifier());

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
        final CoordinateReferenceSystem unsupportedCrs = new CoordinateReferenceSystem("SWAGD-Authority", 1337);
        CrsProfileFactory.create(unsupportedCrs);
        fail("Expected a runtime exception when trying to create a CrsProfile from a coordinate reference system that is not supported.");
    }
}
