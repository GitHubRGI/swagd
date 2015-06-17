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

package com.rgi.suite;

import org.kohsuke.args4j.CmdLineParser;

import com.rgi.suite.cli.Headless;

@SuppressWarnings("javadoc")
public class HeadlessTest {
    // TODO


	/**
	 * invalid arguments
	 */
	public void invalidTilingAndPackaging() throws Exception{
		final Headless opts = new Headless();
		final CmdLineParser parser = new CmdLineParser(opts);
	}

	public void invalidInputsGibberish
	public void invalidInputsTilingInputFile
	public void invalidOuputSRS
	public void invalidOutputSRSGPKG
	public void invalidInputSRS
	public void invalidTileWidthMax
	public void invalidTileWidthMin
	public void invalidTileHeightMax
	public void invalidTileHeightMin
	public void invalidImageFormat
	public void invalidCompressionType
	public void invalidImageQualityHigh
	public void invalidImageQualityLow
	/**
	 * missing arguments
	 */
	public void missingRequiredInputsOperation
	public void missingRequiredInputsInputFile
	public void missingRequiredInputsOutputFile
	public void missingRequiredInputsInputSRS
	public void missingRequiredInputsOutputSRS
	public void missingRequiredInputsTileSetNameGPKG
	public void missingTilesetNameTMS
	public void missingImageFormat
	public void missingCompressionType
	public void missingCompressionQuality

	/**
	 * check case/format of inputs
	 */
	public void malformedCaseInputPath
	public void malformedCaseOutputPath
	public void malformedInputHomeDir
	public void malformedInputImageFormatCase
	public void malformedInputCompressionTypeCase
	public void malformedInputInputPathCurrentDir
	/**
	 * valid arguments/extras
	 */
	public void inputValidTilingTMS
	public void inputValidTilingGPKG
	public void inputValidTMStoGPKG
	public void inputValidGPKGtoTMS
	/**
	 * TMS data set properly
	 */
	public void tmsOuputSRS
	public void tmsTileSize
	public void tmsImageSettings
	/**
	 * gpkg data set properly
	 */
	public void geopackageOutputSRS
	public void geopackageTileSize
	public void geopackageImageSettings
	public void geoPackageTilesetName
	public void geoPackageTilesetDescription
}
