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

import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.EdgeDirecctionality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rgi.store.routingnetworks.triangle.TriangleRoutingNetworkStoreReader.TRIANGLE_NO_DATA_LINE;

/**
 * Utility class that parses a Triangle .edge file header line, and provides a
 * utility to parse subsequent data lines based on the header specification.
 * The Triangle documentation specifies the file as follows:
 *
<blockquote cite="https://www.cs.cmu.edu/~quake/triangle.edge.html">
 *     <h2>.edge files</h2>
 *     <ul>
 *         <li>
 *             First line:  &lt;# of edges&gt; &lt;# of boundary markers (0 or
 *             1)&gt;
 *         </li>
 *         <li>
 *             Following lines:  &lt;edge #&gt; &lt;endpoint&gt; &lt;endpoint
 *             &gt; [boundary marker]
 *         </li>
 *     </ul>
 *     <p>
 *         Blank lines and comments prefixed by `#' may be placed anywhere.
 *         Edges are numbered consecutively, starting from one or zero.
 *         Endpoints are indices into the corresponding <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.node.html">.node</a> file.
 *     </p>
 *     ...
 * </blockquote>
 *
 * @author Luke Lambert
 */
final class EdgeFileHeader
{
    private EdgeFileHeader(final int lineNumber,
                           final int edgeCount,
                           final int boundaryMarkerCount)
    {
        this.lineNumber          = lineNumber;
        this.edgeCount           = edgeCount;
        this.boundaryMarkerCount = boundaryMarkerCount;

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("^\\s*");                   // Start of line, followed by any amount of space
        stringBuilder.append("(?<edgeIdentifier>\\d+)"); // capture one or more digits as the `edgeIdentifier`
        stringBuilder.append("\\s+");                    // whitespace delimiter
        stringBuilder.append("(?<endpoint0>\\d+)");      // capture the first endpoint
        stringBuilder.append("\\s+");                    // whitespace delimiter
        stringBuilder.append("(?<endpoint1>\\d+)");      // capture the second endpoint

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

    public int getEdgeCount()
    {
        return this.edgeCount;
    }

    public int getBoundaryMarkerCount()
    {
        return this.boundaryMarkerCount;
    }

    public Edge parse(final String line)
    {
        final Matcher matcher = this.pattern.matcher(line);

        if(!matcher.matches())
        {
            throw new RuntimeException(String.format("edge file line \"%s\" does not agree with the file's header specification",
                                                     line));
        }

        final int edgeIdentifier = Integer.parseInt(matcher.group("edgeIdentifier")); // TODO make constant

        final int endpoint0 = Integer.parseInt(matcher.group("endpoint0")); // TODO make constant
        final int endpoint1 = Integer.parseInt(matcher.group("endpoint1")); // TODO make constant

        return new Edge(edgeIdentifier,
                        endpoint0,
                        endpoint1,
                        EdgeDirecctionality.TwoWay,
                        Collections.emptyList());
    }

    static EdgeFileHeader from(final File edgeFile) throws IOException
    {
        try(final FileReader fileReader = new FileReader(edgeFile))
        {
            try(final BufferedReader bufferedReader = new BufferedReader(fileReader))
            {
                int lineNumber = 0;

                //noinspection NestedAssignment,ForLoopWithMissingComponent
                for(String line; (line = bufferedReader.readLine()) != null; /**/)
                {
                    if(!TRIANGLE_NO_DATA_LINE.matcher(line).matches())    // We're looking for the first line that's not empty, and is also not a comment
                    {
                        final Matcher matcher = EDGE_FILE_HEADER.matcher(line);

                        if(matcher.matches())
                        {
                            return new EdgeFileHeader(lineNumber,
                                                      Integer.parseInt(matcher.group("edgeCount")),
                                                      Integer.parseInt(matcher.group("boundaryMarkerCount")));
                        }
                    }

                    ++lineNumber;
                }

                throw new IOException("Edge file is empty");
            }
        }
    }

    private final int     lineNumber;
    private final int     edgeCount;
    private final int     boundaryMarkerCount;
    private final Pattern pattern;

    private static final Pattern EDGE_FILE_HEADER   = Pattern.compile("^\\s*(?<edgeCount>\\d+)\\s+(?<boundaryMarkerCount>0|1)\\s*$"); // TODO make group names constant
}
