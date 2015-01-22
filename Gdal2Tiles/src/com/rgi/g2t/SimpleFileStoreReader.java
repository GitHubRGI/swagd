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
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Duff Means
 * @author Luke Lambert
 *
 */
public class SimpleFileStoreReader extends SimpleFileStore implements TileStoreReader
{
    public SimpleFileStoreReader(String name, TileProfile tileProfile, TileOrigin tileOrigin, String location, String imageFormat) throws TileStoreException
    {
        super(name, tileProfile, tileOrigin, location, imageFormat);
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException {
      throw new UnsupportedOperationException("Not implemented by Simple File Store");
    }

    @Override
    public long countTiles() throws TileStoreException {
      long count = 0;
      final Set<Integer> zoomLevels = this.getZoomLevels();
      for (final Integer z : zoomLevels) {
        final File zFolder = new File(this.rootFolder, ""+z);
        for (final String xName : zFolder.list()) {
          final File xFolder = new File(zFolder, xName);
          for (final String yName : xFolder.list()) {
            try {
              Integer.parseInt(yName.replaceAll("\\."+this.imageFormat, ""));
              ++count;
            } catch (final NumberFormatException nfe) {
              // do nothing
            }
          } // for each y
        } // for each x
      } // for each zoom level
      return count;
    }

    @Override
    public long getByteSize() throws TileStoreException {
      long count = 0;
      final Set<Integer> zoomLevels = this.getZoomLevels();
      for (final Integer z : zoomLevels) {
        final File zFolder = new File(this.rootFolder, ""+z);
        for (final String xName : zFolder.list()) {
          final File xFolder = new File(zFolder, xName);
          for (final String yName : xFolder.list()) {
            count += new File(yName).length();
          } // for each y
        } // for each x
      } // for each zoom level
      return count;
    }

      @Override
      public BufferedImage getTile(int row, int column, int zoomLevel) throws TileStoreException
      {
          final File zoomFolder = new File(this.rootFolder, "" + zoomLevel);
          if(!zoomFolder.exists())
          {
              if(!zoomFolder.mkdirs())
              {
                  throw new TileStoreException("Unable to create folders");
              }
          }
          final File xFolder = new File(zoomFolder, "" + column);
          if(!xFolder.exists())
          {
              if(!xFolder.mkdirs())
              {
                  throw new TileStoreException("Unable to create folders");
              }
          }
          final File yFile = new File(xFolder, row + ".png");
          BufferedImage image;
          try
          {
              image = ImageIO.read(yFile);
          }
          catch(final IOException ioe)
          {
              throw new TileStoreException("Unable to read tile from file", ioe);
          }

          return image;
      }

      @Override
      public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
      {
          if(coordinate == null)
          {
              throw new IllegalArgumentException("Coordinate may not be null");
          }

          if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
          {
              throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
          }

          final AbsoluteTileCoordinate absCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate, zoomLevel, this.tileOrigin);

          return this.getTile(absCoordinate.getRow(),
                              absCoordinate.getColumn(),
                              zoomLevel);
      }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException {
      final Set<Integer> zoomLevels = new TreeSet<>();
      final String[] files = this.rootFolder.list((dir, name) -> {
          try {
            Integer.parseInt(name);
          } catch (final NumberFormatException nfe) {
            return false;
          }
          return new File(name).isDirectory();
        });
      for (final String file : files) {
        zoomLevels.add(Integer.parseInt(file));
      }
      return zoomLevels;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
      return this.tileProfile.getCoordinateReferenceSystem();
    }
}
