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
