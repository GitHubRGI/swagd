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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.OSMReader;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.DAType;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.Lock;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Unzipper;

import java.io.File;
import java.io.IOException;

/**
 * @author Luke Lambert
 */
public class AdditionalFieldGraphHopper extends GraphHopper
{
    public AdditionalFieldGraphHopper(final EdgeAdditionalFieldProvider edgeAdditionalFieldProvider)
    {
        this.edgeAdditionalFieldProvider = edgeAdditionalFieldProvider;
    }

    /**
     * This is a direct copy+paste from GraphHopper.java. Deviations are
     * denoted with *****'d sections.
     *
     * Copying this huge chunk of code is far from ideal, but it's an attempt
     * to minimize how much of GraphHopper needs to be changed to suit our
     * needs. In this case, we've got to override 'load' so we can set our
     * own GraphExtension to allow for the use of the AdditionalField
     */
    @Override
    @SuppressWarnings("ALL") // This is 99% from GraphHopper.java. In order to minimize the changes (in case we need to diff/update in the future) I'm just suppressing the warnings from their code.
    public boolean load( String graphHopperFolder )
    {
        if (Helper.isEmpty(graphHopperFolder))
            throw new IllegalStateException("GraphHopperLocation is not specified. Call setGraphHopperLocation or init before");

        if (fullyLoaded)
            throw new IllegalStateException("graph is already successfully loaded");

        if (graphHopperFolder.endsWith("-gh"))
        {
            // do nothing
        } else if (graphHopperFolder.endsWith(".osm") || graphHopperFolder.endsWith(".xml"))
        {
            throw new IllegalArgumentException("GraphHopperLocation cannot be the OSM file. Instead you need to use importOrLoad");
        } else if (!graphHopperFolder.contains("."))
        {
            if (new File(graphHopperFolder + "-gh").exists())
                graphHopperFolder += "-gh";
        } else
        {
            File compressed = new File(graphHopperFolder + ".ghz");
            if (compressed.exists() && !compressed.isDirectory())
            {
                try
                {
                    new Unzipper().unzip(compressed.getAbsolutePath(), graphHopperFolder, removeZipped);
                } catch (IOException ex)
                {
                    throw new RuntimeException("Couldn't extract file " + compressed.getAbsolutePath()
                            + " to " + graphHopperFolder, ex);
                }
            }
        }

        setGraphHopperLocation(graphHopperFolder);

        if (encodingManager == null)
            setEncodingManager(EncodingManager.create(flagEncoderFactory, ghLocation));

        if (!allowWrites && dataAccessType.isMMap())
            dataAccessType = DAType.MMAP_RO;

        GHDirectory dir = new GHDirectory(ghLocation, dataAccessType);

        // ********************************************************************
        //GraphExtension ext = encodingManager.needsTurnCostsSupport()
        //        ? new TurnCostExtension() : new GraphExtension.NoOpExtension();

        if(encodingManager.needsTurnCostsSupport())
        {
            throw new RuntimeException("The encoding manager requires 'turn costs support' which isn't supported.");
        }

        final GraphExtension ext = new AdditionalFieldGraphExtension();

        // ********************************************************************

        if (chFactoryDecorator.isEnabled())
        {
            initCHAlgoFactoryDecorator();
            ghStorage = new GraphHopperStorage(chFactoryDecorator.getWeightings(), dir, encodingManager, hasElevation(), ext);
        } else
        {
            ghStorage = new GraphHopperStorage(dir, encodingManager, hasElevation(), ext);
        }

        ghStorage.setSegmentSize(defaultSegmentSize);

        if (!new File(graphHopperFolder).exists())
            return false;

        Lock lock = null;
        try
        {
            // create locks only if writes are allowed, if they are not allowed a lock cannot be created
            // (e.g. on a read only filesystem locks would fail)
            if (ghStorage.getDirectory().getDefaultType().isStoring() && isAllowWrites())
            {
                lockFactory.setLockDir(new File(ghLocation));
                lock = lockFactory.create(fileLockName, false);
                if (!lock.tryLock())
                    throw new RuntimeException("To avoid reading partial data we need to obtain the read lock but it failed. In " + ghLocation, lock.getObtainFailedReason());
            }

            if (!ghStorage.loadExisting())
                return false;

            postProcessing();
            fullyLoaded = true;
            return true;
        } finally
        {
            if (lock != null)
                lock.release();
        }
    }

    @Override
    protected DataReader createReader(final GraphHopperStorage ghStorage)
    {
        final OSMReader osmReader = new OSMReader(ghStorage)
                                    {
                                        @Override
                                        protected EdgeIteratorState addEdge(final int       fromIndex,
                                                                            final int       toIndex,
                                                                            final PointList pointList,
                                                                            final long      flags,
                                                                            final long      wayOsmId)
                                        {
                                            final EdgeIteratorState edge = super.addEdge(fromIndex,
                                                                                         toIndex,
                                                                                         pointList,
                                                                                         flags,
                                                                                         wayOsmId);

                                            edge.setAdditionalField(AdditionalFieldGraphHopper.this
                                                                                              .edgeAdditionalFieldProvider
                                                                                              .provide(fromIndex,
                                                                                                       toIndex,
                                                                                                       pointList,
                                                                                                       flags,
                                                                                                       wayOsmId));

                                            return edge;
                                        }
                                    };

        return this.initOSMReader(osmReader);
    }

    private static class AdditionalFieldGraphExtension extends GraphExtension.NoOpExtension
    {
        @Override
        public boolean isRequireEdgeField()
        {
            return true;
        }

        @Override
        public int getDefaultEdgeFieldValue()
        {
            return Helper.doubleToE6Int(1.0);
        }

        @Override
        public String toString()
        {
            return "AdditionalField";
        }

    }

    private final EdgeAdditionalFieldProvider edgeAdditionalFieldProvider;
}
