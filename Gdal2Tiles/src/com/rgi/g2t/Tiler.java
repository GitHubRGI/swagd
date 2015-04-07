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

package com.rgi.g2t;

import java.awt.Color;
import java.io.File;

import com.rgi.common.Dimensions;
import com.rgi.common.TaskMonitor;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Luke Lambert
 *
 */
public class Tiler
{
    final private File                file;
    final private TileStoreWriter     tileWriter;
    final private Dimensions<Integer> tileDimensions;
    final private Color               noDataColor;
    private final TaskMonitor         taskMonitor;

    /**
     * Constructor
     *
     * @param file
     *             Source image
     * @param tileWriter
     *             Destination tile store
     * @param tileDimensions
     *             Desired tile pixel width and height
     * @param noDataColor
     *             Default tile color
     * @param taskMonitor
     *             Callback to report the tiler progress
     */
    public Tiler(final File                file,
                 final TileStoreWriter     tileWriter,
                 final Dimensions<Integer> tileDimensions,
                 final Color               noDataColor,
                 final TaskMonitor         taskMonitor)
    {
        this.file           = file;
        this.tileWriter     = tileWriter;
        this.tileDimensions = tileDimensions;
        this.noDataColor    = noDataColor;
        this.taskMonitor    = taskMonitor;

        sanityCheckGdalInstallation();
    }

    /**
     * Creates the tiles
     *
     * @throws TilingException
     *             when GdalTileJob creation fails
     */
    public void execute() throws TilingException
    {
        final GdalTileJob tileJob = new GdalTileJob(this.file,
                                                    this.tileWriter,
                                                    this.tileDimensions,
                                                    this.noDataColor,
                                                    this.taskMonitor);
        tileJob.run();
    }

    private static void sanityCheckGdalInstallation()
    {
        // GDAL_DATA needs to be a valid path
        if(System.getenv("GDAL_DATA") == null)
        {
            throw new RuntimeException("Tiling will not work without GDAL_DATA environment variable.");
        }
        // Get the system path
        //String paths = System.getenv("PATH");
        // TODO
        // Parse the path entries
        // Check each path entry for the required dll's/so's
        // Throw an error if any of the required ones are missing
    }
}
