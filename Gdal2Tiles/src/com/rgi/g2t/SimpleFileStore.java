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

package com.rgi.g2t;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.tile.Tile;
import com.rgi.common.tile.TileException;
import com.rgi.common.tile.store.TileStore;
import com.rgi.common.tile.store.TileStoreException;

public class SimpleFileStore implements TileStore {
  private File rootFolder = null;
  private int projection;
  private String imageFormat;

  public SimpleFileStore(String name, int projection, String location, String imageFormat) throws TileStoreException {
	  if (!"png".equalsIgnoreCase(imageFormat) && !"jpg".equalsIgnoreCase(imageFormat))
		  throw new IllegalArgumentException("Only PNG and JPG formats supported");
	  this.imageFormat = imageFormat;
	  String filePart = name;
    if (filePart.contains(".")) {
      filePart = filePart.substring(0, filePart.indexOf('.'));
    }
    rootFolder = new File(new File(location, Integer.toString(projection)), filePart);
    if (!rootFolder.exists()) {
      if (!rootFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
  }

  @Override
  public BoundingBox calculateBounds() throws TileStoreException {
    throw new UnsupportedOperationException("Not implemented by Simple File Store");
  }

  @Override
  public long countTiles() throws TileStoreException {
    long count = 0;
    Set<Integer> zoomLevels = getZoomLevels();
    for (Integer z : zoomLevels) {
      File zFolder = new File(rootFolder, ""+z);
      for (String xName : zFolder.list()) {
        File xFolder = new File(zFolder, xName);
        for (String yName : xFolder.list()) {
          try {
            Integer.parseInt(yName.replaceAll("\\."+imageFormat, ""));
            ++count;
          } catch (NumberFormatException nfe) {
            // do nothing
          }
        } // for each y
      } // for each x
    } // for each zoom level
    return count;
  }

  @Override
  public long calculateSize() throws TileStoreException {
    long count = 0;
    Set<Integer> zoomLevels = getZoomLevels();
    for (Integer z : zoomLevels) {
      File zFolder = new File(rootFolder, ""+z);
      for (String xName : zFolder.list()) {
        File xFolder = new File(zFolder, xName);
        for (String yName : xFolder.list()) {
          count += new File(yName).length();
        } // for each y
      } // for each x
    } // for each zoom level
    return count;
  }

  @Override
  public Tile getTile(AbsoluteTileCoordinate coordinate) throws TileException, TileStoreException {
    File zoomFolder = new File(rootFolder, ""+coordinate.getZoomLevel());
    if (!zoomFolder.exists()) {
      if (!zoomFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
    File xFolder = new File(zoomFolder, ""+coordinate.getX());
    if (!xFolder.exists()) {
      if (!xFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
    File yFile = new File(xFolder, coordinate.getY()+".png");
    BufferedImage image;
    try {
      image = ImageIO.read(yFile);
    } catch (IOException ioe) {
      throw new TileStoreException("Unable to read tile from file", ioe);
    }
    return new Tile(coordinate, image);
  }

  @Override
  public Tile addTile(AbsoluteTileCoordinate coordinate, BufferedImage image)
      throws TileException, TileStoreException {
    File zoomFolder = new File(rootFolder, ""+coordinate.getZoomLevel());
    if (!zoomFolder.exists()) {
      if (!zoomFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
    File xFolder = new File(zoomFolder, ""+coordinate.getX());
    if (!xFolder.exists()) {
      if (!xFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
    File yFile = new File(xFolder, coordinate.getY()+"."+imageFormat);
    try {
      ImageIO.write(image, imageFormat, yFile);
    } catch (IOException ioe) {
      throw new TileStoreException("Unable to write tile to file", ioe);
    }
    return new Tile(coordinate, image);
  }

  @Override
  public Set<Integer> getZoomLevels() throws TileStoreException {
    Set<Integer> zoomLevels = new TreeSet<Integer>();
    String[] files = rootFolder.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        try {
          Integer.parseInt(name);
        } catch (NumberFormatException nfe) {
          return false;
        }
        return new File(name).isDirectory();
      }
    });
    for (String file : files) {
      zoomLevels.add(Integer.parseInt(file));
    }
    return zoomLevels;
  }

  @Override
  public CoordinateReferenceSystem getCoordinateReferenceSystem() {
    return new CoordinateReferenceSystem("EPSG", projection);
  }
}
