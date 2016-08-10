/*
 * The MIT License (MIT)
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

package com.rgi.store.routingnetworks.triangle;

import com.rgi.store.routingnetworks.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rgi.store.routingnetworks.triangle.TriangleRoutingNetworkStoreReader.TRIANGLE_NO_DATA_LINE;

/**
 * Utility class that parses a Triangle .nodes file header line, and provides a
 * utility to parse subsequent data lines based on the header specification.
 * The Triangle documentation specifies the file as follows:
 *
 * <blockquote cite="https://www.cs.cmu.edu/~quake/triangle.node.html">
 *     <h2>.node files</h2>
 *     <ul>
 *         <li>
 *             First line:  &lt;# of vertices&gt; &lt;dimension (must be 2)&gt;
 *             &lt;# of attributes&gt; &lt;# of boundary markers (0 or 1)&gt;
 *         </li>
 *         <li>
 *             Remaining lines:  &lt;vertex #&gt; &lt;x&gt; &lt;y&gt;
 *             [attributes] [boundary marker]
 *         </li>
 *     </ul>
 *     <p>
 *         Blank lines and comments prefixed by `#' may be placed anywhere.
 *         Vertices must be numbered consecutively, starting from one or zero.
 *     </p>
 *     ...
 * </blockquote>
 *
 * @author Luke Lambert
 */
final class NodeFileHeader
{
    private NodeFileHeader(final int lineNumber,
                           final int nodeCount,
                           final int attributeCount,
                           final int boundaryMarkerCount)
    {
        this.lineNumber              = lineNumber;
        this.nodeCount               = nodeCount;
        this.attributeCount          = attributeCount;
        this.boundaryMarkerCount     = boundaryMarkerCount;

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("^\\s*");                   // Start of line, followed by any amount of space
        stringBuilder.append("(?<nodeIdentifier>\\d+)"); // capture one or more digits as the `nodeIdentifier`
        stringBuilder.append("\\s+");                    // whitespace delimiter
        stringBuilder.append("(?<x>-?\\d+(?:.\\d+))?");   // capture a number (integer or real) as `x`
        stringBuilder.append("\\s+");                    // whitespace delimiter
        stringBuilder.append("(?<y>-?\\d+(?:.\\d+))?");   // capture a number (integer or real) as `y`

        for(int attributeIndex = 0; attributeIndex < this.attributeCount; ++attributeIndex)
        {
            stringBuilder.append("\\s+");      // whitespace delimiter
            stringBuilder.append("([^\\s]+)"); // one or more non-whitespace character
        }

        if(boundaryMarkerCount > 0)
        {
            stringBuilder.append("\\s+");      // whitespace delimiter
            stringBuilder.append(".+");
        }

        stringBuilder.append('$'); // end of line

        this.pattern = Pattern.compile(stringBuilder.toString());
    }

    public int getLineNumber()
    {
        return this.lineNumber;
    }

    public int getNodeCount()
    {
        return this.nodeCount;
    }

    public int getAttributeCount()
    {
        return this.attributeCount;
    }

    public int getBoundaryMarkerCount()
    {
        return this.boundaryMarkerCount;
    }

    public Node parse(final String line,
                      final int elevationAttributeIndex)
    {
        if(elevationAttributeIndex > this.attributeCount-1)
        {
            throw new IllegalArgumentException("Elevation attribute index is out of bounds");
        }

        final Matcher matcher = this.pattern.matcher(line);

        if(!matcher.matches())
        {
            throw new RuntimeException(String.format("Node file line \"%s\" does not agree with the file's header specification",
                                                     line));
        }

        final int nodeIdentifier = Integer.parseInt(matcher.group("nodeIdentifier")); // TODO make constant

        final double x = Double.parseDouble(matcher.group("x")); // TODO make constant
        final double y = Double.parseDouble(matcher.group("y")); // TODO make constant

        final List<Object> attributes = new ArrayList<>(this.attributeCount);

        final int attributeGroupOffset = 4; // group 0 is always entire match, followed by the node identifier, x, and y

        for(int attributeIndex = 0; attributeIndex < this.attributeCount; ++attributeIndex)
        {
            attributes.add(matcher.group(attributeIndex + attributeGroupOffset));
        }

        Double elevation = null;

        if(elevationAttributeIndex >= 0)
        {
            elevation = Double.parseDouble(attributes.get(elevationAttributeIndex).toString());
            attributes.remove(elevationAttributeIndex);
        }

        return new Node(nodeIdentifier,
                        x,
                        y,
                        elevation,
                        attributes);
    }

    static NodeFileHeader from(final File nodeFile) throws IOException
    {
        try(final FileReader fileReader = new FileReader(nodeFile))
        {
            try(final BufferedReader bufferedReader = new BufferedReader(fileReader))
            {
                int lineNumber = 0;

                //noinspection NestedAssignment,ForLoopWithMissingComponent
                for(String line; (line = bufferedReader.readLine()) != null; /**/)
                {
                    if(!TRIANGLE_NO_DATA_LINE.matcher(line).matches())    // We're looking for the first line that's not empty, and is also not a comment
                    {
                        final Matcher matcher = NODE_FILE_HEADER.matcher(line);

                        if(matcher.matches())
                        {
                            return new NodeFileHeader(lineNumber,
                                    Integer.parseInt(matcher.group("nodeCount")),            // TODO make constant
                                    Integer.parseInt(matcher.group("attributeCount")),       // TODO make constant
                                    Integer.parseInt(matcher.group("boundaryMarkerCount"))); // TODO make constant
                        }
                    }

                    ++lineNumber;
                }

                throw new IOException("Node file is empty");
            }
        }
    }
    private final int     lineNumber;
    private final int     nodeCount;
    private final int     attributeCount;
    private final int     boundaryMarkerCount;
    private final Pattern pattern;

    private static final Pattern NODE_FILE_HEADER = Pattern.compile("^\\s*(?<nodeCount>\\d+)\\s+2\\s+(?<attributeCount>\\d+)\\s+(?<boundaryMarkerCount>0|1)\\s*$"); // TODO make group names constant
}
