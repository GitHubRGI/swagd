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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * @author Luke Lambert
 */
public class CommandLineOptions
{
	private File    inputFile;
    private int     rasterBand;
    private double  contourElevationInterval;
    private Double  noDataValue;
    private int     coordinatePrecision = -1;
    private double  simplificationTolerance;
    private double  triangulationTolerance;
    private boolean outputRasterizedNetwork = true;
    private double  outputRasterScale = 1.0;

     @Option(required = true,
            name     = "-d",
            aliases  = "-dem",
            metaVar  = "<Input File Path>",
            usage    = "File containing the digital elevation model dataset")
	public void setInputFile(final String inputFilename)
	{
		final String path = getFullPath(inputFilename);
		final File   file = new File(path);

        if(!file.exists())
		{
			throw new IllegalArgumentException(String.format("Input file '%s' does not exist",
															 path));
		}

		this.inputFile = file;
	}

	public File getInputFile()
    {
        return this.inputFile;
    }

    @Option(required = true,
            name     = "-b",
            aliases  = "-band",
            metaVar  = "<Raster Band>",
            usage    = "Raster band containing elevation data")
	public void setRasterBand(final int rasterBand)
	{
		if(rasterBand < 1)
		{
			throw new IllegalArgumentException("Raster band must be greater than 0");
		}

		this.rasterBand = rasterBand;
	}

	public int getRasterBand()
    {
        return this.rasterBand;
    }

    @Option(required = true,
            name     = "-i",
            aliases  = "-interval",
            metaVar  = "<Contour Elevation Interval>",
            usage    = "Contour elevation interval (elevation values will be multiples of the interval). In CRS elevation units.")
	public void setContourElevationInterval(final double contourElevationInterval)
	{
		if(contourElevationInterval <= 0.0)
		{
			throw new IllegalArgumentException("Raster band must be greater than 0");
		}

		this.contourElevationInterval = contourElevationInterval;
	}

	public double getContourElevationInterval()
    {
        return this.contourElevationInterval;
    }

    @Option(name     = "-v",
            aliases  = "-noDataValue",
            metaVar  = "<No Data Value>",
            usage    = "Value that indicates that a pixel in the DEM contains no elevation data, and is to be ignored")
	public void setNoDataValue(final Double noDataValue)
	{
		this.noDataValue = noDataValue;
	}

	public Double getNoDataValue()
    {
        return this.noDataValue;
    }

    @Option(name     = "-p",
            aliases  = "-precision",
            metaVar  = "<Coordinate Precision>",
            usage    = "Number of decimal places to round the coordinates. A negative value will cause no rounding to occur")
	public void setCoordinatePrecision(final int coordinatePrecision)
	{
		if(coordinatePrecision < 0)
		{
			throw new IllegalArgumentException("Coordinate precision must be greater than 0");
		}

		this.coordinatePrecision = coordinatePrecision;
	}

	public int getCoordinatePrecision()
    {
        return this.coordinatePrecision;
    }

    @Option(required = true,
            name     = "-s",
            aliases  = "-simplificationTolerance",
            metaVar  = "<Contour Simplification Tolerance>",
            usage    = "Tolerance used to simplify the contour rings that are used in the triangulation of the data. All nodes in the simplified geometry will be within this tolerance of the original geometry. The tolerance value must be non-negative. A tolerance value of zero is effectively a no-op.")
	public void setSimplificationTolerance(final double simplificationTolerance)
	{
		if(simplificationTolerance < 0.0)
		{
			throw new IllegalArgumentException("Contour simplification tolerance must be greater than 0");
		}

		this.simplificationTolerance = simplificationTolerance;
	}

	public double getSimplificationTolerance()
    {
        return this.simplificationTolerance;
    }

    @Option(required = true,
            name     = "-t",
            aliases  = "-triangulationTolerance",
            metaVar  = "<Triangulation Tolerance>",
            usage    = "Snaps points that are within a tolerance's distance from one another.")
	public void setTriangulationTolerance(final double triangulationTolerance)
	{
		if(triangulationTolerance < 0.0)
		{
			throw new IllegalArgumentException("Triangulation tolerance must be greater than 0");
		}

		this.triangulationTolerance = triangulationTolerance;
	}

	public double getTriangulationTolerance()
    {
        return this.triangulationTolerance;
    }

    @Option(name     = "-r",
            aliases  = "-rasterize",
            metaVar  = "<TRUE>",
            usage    = "Additionally create a rasterization of the network")
	public void setOutputRasterizedNetwork(final boolean outputRasterizedNetwork)
    {
        this.outputRasterizedNetwork = outputRasterizedNetwork;
    }

    public boolean getOutputRasterizedNetwork()
    {
        return this.outputRasterizedNetwork;
    }

    @Option(name     = "-rasterScale",
            metaVar  = "<10.0>",
            usage    = "Scale of the original image for the rasterized network output. Ignored if -r is set to false.")
	public void setOutputRasterizedNetwork(final double outputRasterScale)
    {
        this.outputRasterScale = outputRasterScale;
    }

    public double getOutputRasterScale()
    {
        return this.outputRasterScale;
    }

	@Option(help = true,
            name = "-h",
            aliases = "--help",
            usage = "Show Help")
	public void showHelp(final boolean showHelp) throws CmdLineException
	{
		final CmdLineParser parser = new CmdLineParser(this);
		parser.printUsage(System.out);
	}

	/**
	 * returns a full path to the specified file, replacing cmdline based shortcuts
	 *
	 * @param filePath - filepath to replace local charaters in
	 * @return absolute path of string
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	private static String getFullPath(final String filePath)
	{
		if(filePath != null && !filePath.isEmpty())
		{
			String path = filePath;
			if(path.startsWith('~' + File.separator))
			{
				path = String.format("%s%s", System.getProperty("user.home"), path.substring(1));
			}
			else if(path.startsWith('.' + File.separator))
			{
				path = String.format("%s%s", System.getProperty("user.dir"), path.substring(1));
			}
			return path;
		}
		throw new IllegalArgumentException("File Path provided is null or empty!");
	}
}
