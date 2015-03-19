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
package com.rgi.common.util.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Test;

import com.rgi.common.util.MimeTypeUtility;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
public class MimeTypeUtilityTest
{

    @Test
    public void createMimeTypeSetVerify() throws MimeTypeParseException
    {
        final String[]  mimeTypeStrings = {"image/png",
                                           "image/jpeg",
                                           null,
                                           "video/avi",
                                           "image/bmp",
                                           "text/plain"};

        final Set<MimeType> expectedMimeTypes = new HashSet<>(Arrays.asList(new MimeType("image/png"),
                                                                            new MimeType("image/jpeg"),
                                                                            new MimeType("video/avi"),
                                                                            new MimeType("image/bmp"),
                                                                            new MimeType("text/plain")));

        final Set<MimeType> returnedMimeTypes = MimeTypeUtility.createMimeTypeSet(mimeTypeStrings);

        MimeTypeUtilityTest.assertCreateMimeTypes(expectedMimeTypes, returnedMimeTypes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        MimeTypeUtility.createMimeTypeSet((String[])null);
        fail("Expected MimeTypeUtility method createMimeTypeSet to throw an IllegalArgumentException when passing null to types(String array).");
    }

    @Test
    public void containsVerifyTrue() throws MimeTypeParseException
    {
        final Set<MimeType> expectedMimeTypes = new HashSet<>(Arrays.asList(new MimeType("image/png"),
                                                                            new MimeType("image/jpeg"),
                                                                            new MimeType("video/avi"),
                                                                            new MimeType("image/bmp"),
                                                                            new MimeType("text/plain")));
        for(final MimeType mimeType: expectedMimeTypes)
        {
            assertContains(expectedMimeTypes, mimeType, true);
        }
    }

    @Test
    public void containsVerifyFalse() throws MimeTypeParseException
    {
        final Set<MimeType> collectionOfMimeTypes = new HashSet<>(Arrays.asList(new MimeType("image/png"),
                                                                                new MimeType("image/jpeg"),
                                                                                new MimeType("video/avi"),
                                                                                new MimeType("image/bmp"),
                                                                                new MimeType("text/plain")));
        final MimeType notInCollection = new MimeType("image/fif");

        assertContains(collectionOfMimeTypes, notInCollection, false);
    }

    @Test
    public void containsVerifyWithNullValues() throws MimeTypeParseException
    {
        final Set<MimeType> collectionOfMimeTypes = new HashSet<>(Arrays.asList(new MimeType("image/png"),
                                                                                new MimeType("image/jpeg"),
                                                                                new MimeType("video/avi"),
                                                                                new MimeType("image/bmp"),
                                                                                null));
        final MimeType mimeType = new MimeType("text/plain");

        assertTrue("Expected MimeTypeUtility method contains to return false",
                   MimeTypeUtility.contains(collectionOfMimeTypes, mimeType) == false);

    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2() throws MimeTypeParseException
    {
        MimeTypeUtility.contains(null, new MimeType("image/jpeg"));
        fail("Expected MimeTypeUtility method contains to throw an IllegalArgumentException when passing a null value for mimeTypes(Collection<MimeType>).");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3() throws MimeTypeParseException
    {
        MimeTypeUtility.contains(new HashSet<>(Arrays.asList(new MimeType("image/png"))), null);
        fail("Expected MimeTypeUtility to throw an IllegalArgumentException when passing a null value for MimeType.");
    }


    private static void assertContains(final Set<MimeType> expectedMimeTypes, final MimeType mimeType, final boolean outcome)
    {

            assertTrue(printContainsErrorMessage(expectedMimeTypes, mimeType, outcome),
                       MimeTypeUtility.contains(expectedMimeTypes, mimeType) == outcome);
    }

    private static String printContainsErrorMessage(final Set<MimeType> collectionMimeTypes, final MimeType mimeType, final boolean outcome)
    {
        return String.format("Expected MimeTypeUtility contains method to return %s for MimeType: %s. Collection: %s",
                             outcome,
                             mimeType.toString(),
                             collectionMimeTypes.stream()
                                                .map(type -> type.toString())
                                                .collect(Collectors.joining(", ")));
    }

    private static void assertCreateMimeTypes(final Set<MimeType> expectedMimeTypes, final Set<MimeType> returnedMimeTypes)
    {
        assertTrue(String.format("MimeTypeUtility did not return all the expected MimeType objects Missing Items: %s. Extra Items: %s",
                                  returnedMimeTypes.stream()
                                                   .filter(returned -> expectedMimeTypes.stream()
                                                                                        .allMatch(expected -> !expected.match(returned)))
                                                                                        .collect(Collectors.toList()).stream()
                                                                                                                     .map(missingType -> {return missingType.toString();})
                                                                                                                     .collect(Collectors.joining(", ")),
                                expectedMimeTypes.stream()
                                                 .filter(expected -> returnedMimeTypes.stream()
                                                                                      .allMatch(returned -> !expected.match(returned)))
                                                                                      .collect(Collectors.toList()).stream()
                                                                                                                   .map(extraType -> {return extraType.toString();})
                                                                                                                   .collect(Collectors.joining(", "))
                                                                                                                                       ),
                expectedMimeTypes.stream().allMatch(expected -> returnedMimeTypes.stream().anyMatch(returned -> expected.match(returned))) &&
                returnedMimeTypes.stream().allMatch(returned -> expectedMimeTypes.stream().anyMatch(expected -> returned.match(expected))) );
    }
}
