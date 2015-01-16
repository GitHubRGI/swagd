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
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileException;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.store.TileStore;
import com.rgi.common.tile.store.TileStoreException;

public class SimpleFileStore implements TileStore {
  private File rootFolder = null;
  private final TileProfile tileProfile;
  private final TileOrigin tileOrigin;
  private String imageFormat;

  public SimpleFileStore(String name, final TileProfile tileProfile, final TileOrigin tileOrigin, String location, String imageFormat) throws TileStoreException {
	  if (!"png".equalsIgnoreCase(imageFormat) && !"jpg".equalsIgnoreCase(imageFormat))
    {
        throw new IllegalArgumentException("Only PNG and JPG formats supported");
    }
	  this.imageFormat = imageFormat;
	  this.tileProfile = tileProfile;
	  this.tileOrigin  = tileOrigin;
	  String filePart = name;
    if (filePart.contains(".")) {
      filePart = filePart.substring(0, filePart.indexOf('.'));
    }
    this.rootFolder = new File(new File(location, Integer.toString(this.tileProfile.getCoordinateReferenceSystem().getIdentifier())), filePart);
    if (!this.rootFolder.exists()) {
      if (!this.rootFolder.mkdirs()) {
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
    Set<Integer> zoomLevels = this.getZoomLevels();
    for (Integer z : zoomLevels) {
      File zFolder = new File(this.rootFolder, ""+z);
      for (String xName : zFolder.list()) {
        File xFolder = new File(zFolder, xName);
        for (String yName : xFolder.list()) {
          try {
            Integer.parseInt(yName.replaceAll("\\."+this.imageFormat, ""));
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
    Set<Integer> zoomLevels = this.getZoomLevels();
    for (Integer z : zoomLevels) {
      File zFolder = new File(this.rootFolder, ""+z);
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
  public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileException, TileStoreException {
    File zoomFolder = new File(this.rootFolder, ""+zoomLevel);
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
    return image;
  }

  @Override
  public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image)
      throws TileException, TileStoreException {
    File zoomFolder = new File(this.rootFolder, ""+zoomLevel);
    if (!zoomFolder.exists()) {
      if (!zoomFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }

    final AbsoluteTileCoordinate absTileCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate, zoomLevel, this.tileOrigin);

    File xFolder = new File(zoomFolder, ""+absTileCoordinate.getX());
    if (!xFolder.exists()) {
      if (!xFolder.mkdirs()) {
        throw new TileStoreException("Unable to create folders");
      }
    }
    File yFile = new File(xFolder, absTileCoordinate.getY()+"."+this.imageFormat);
    try {
      ImageIO.write(image, this.imageFormat, yFile);
    } catch (IOException ioe) {
      throw new TileStoreException("Unable to write tile to file", ioe);
    }
  }

  @Override
  public Set<Integer> getZoomLevels() throws TileStoreException {
    Set<Integer> zoomLevels = new TreeSet<Integer>();
    String[] files = this.rootFolder.list(new FilenameFilter() {
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
    return this.tileProfile.getCoordinateReferenceSystem();
  }
}
