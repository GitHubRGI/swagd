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

package com.rgi.dem2gh;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.storage.DAType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.rgi.store.routingnetworks.osm.Constants.ELEVATION_NODE_ATTRIBUTE_NAME;
import static com.rgi.store.routingnetworks.osm.Constants.LATITUDE_NODE_ATTRIBUTE_NAME;
import static com.rgi.store.routingnetworks.osm.Constants.LONGITUDE_NODE_ATTRIBUTE_NAME;

/**
 * Class to provide an elevation data  at a given latitude and longitude.
 *
 * @author Matt Renner
 */
public class TagElevationProvider implements ElevationProvider
{
    private final SpatialIndex strTree = new STRtree();

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Gets the elevation at a give latitude and longitude
     *
     * @param lat  The latitude
     * @param lon  The longitude
     * @return The elevation (meters)
     */
    @Override
    public double getEle(final double lat,
                         final double lon)
    {
        final Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        final List<?> list = this.strTree.query(point.getEnvelopeInternal());

        return list.isEmpty() ? Double.NaN : (Double)list.get(0);
    }

    /**
     * Reads the elevation data and returns the data envelope
     *
     * @param baseURL The url for where the elevation data is stored
     * @return The data envelope containing the elevation data
     * @throws RuntimeException If the baseURL is empty or null, the file from
     *                          the baseURL does not exist or there is an error
     *                          reading the elevation data file.
     */
    @SuppressWarnings("ReturnOfThis")
    @Override
    public ElevationProvider setBaseURL(final String baseURL)
    {
        if(baseURL == null || baseURL.isEmpty())
        {
            throw new RuntimeException("Base URL of elevation data is null or empty.");
        }

        // Start: Parsing osm
        final File osmFile = new File(baseURL);
        if(!osmFile.exists())
        {
            throw new RuntimeException("Input file does not exist, cannot get elevation.");
        }

        //StAX
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        try(final FileInputStream fileInputStream = new FileInputStream(osmFile))
        {
            final XMLStreamReader reader = factory.createXMLStreamReader(fileInputStream);

            while(reader.hasNext())
            {
                final int event = reader.next();

                if(event == XMLStreamConstants.START_ELEMENT)
                {
                    if(reader.getLocalName().equals("node"))
                    {
                        final double longitude = Double.parseDouble(reader.getAttributeValue(null, LONGITUDE_NODE_ATTRIBUTE_NAME));
                        final double latitude  = Double.parseDouble(reader.getAttributeValue(null, LATITUDE_NODE_ATTRIBUTE_NAME));
                        final double elevation = Double.parseDouble(reader.getAttributeValue(null, ELEVATION_NODE_ATTRIBUTE_NAME));

                        final Point p = geometryFactory.createPoint(new Coordinate(longitude, latitude));

                        this.strTree.insert(p.getEnvelopeInternal(), elevation);
                    }
                }
            }
        }
        catch(final IOException | XMLStreamException exception)
        {
            throw new RuntimeException(exception);
        }

        return this;
    }

    /**
     * Not implemented method
     *
     * @param cacheDir File for the cache directory
     * @return UnsupportedOperationException
     */
    @Override
    public ElevationProvider setCacheDir(final File cacheDir)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented method
     *
     * @return UnsupportedOperationException
     */
    @Override
    public ElevationProvider setDAType(final DAType daType)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented method
     *
     * @return UnsupportedOperationException
     */
    @Override
    public void setCalcMean(final boolean calcMean)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented method
     */
    @Override
    public void release()
    {
        // TODO: release resources, but should strTree data be dumped?
    }
}
