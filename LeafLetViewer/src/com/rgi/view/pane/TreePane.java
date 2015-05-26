package com.rgi.view.pane;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.Pane;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.view.TileLoaderBridge;

public class TreePane extends Pane
{
    private final Collection<TileStoreReader> tileStoreReaders;
    private final TreeView<TileLoaderBridge> tree;

    public TreePane(final Collection<TileStoreReader> tileStoreReaders) throws Exception
    {
        this.tileStoreReaders = tileStoreReaders;

        this.tree = this.createTree(this.createDummyRootNode());

        this.getChildren().add(this.tree);
    }


    private TreeView<TileLoaderBridge> createTree(final TreeItem<TileLoaderBridge> rootItem)
    {
        TreeView<TileLoaderBridge> tree = new TreeView<>(rootItem);
        tree.setEditable(false);
        /*
         * Create listener for nodes
         * set the bridge to the selected Item
         */
        tree.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) ->
        {
            if(newValue.getClass() == Boolean.class)
            {
                Boolean selectedItem = (Boolean) newValue;
                if(selectedItem.equals(Boolean.TRUE))
                {
                    //show
                }
                else
                {
                    //hide
                }
            }
        });
        tree.setCellFactory(CheckBoxTreeCell.<TileLoaderBridge>forTreeView());
        /*
         * Add the readers to the list
         */
        this.tileStoreReaders.stream().forEach(reader -> {
                                                            TreeItem<TileLoaderBridge> item = null;
                                                            try
                                                            {
                                                                item = new TreeItem<>(new TileLoaderBridge(reader));
                                                            } catch (Exception e)
                                                            {
                                                                throw new RuntimeException(String.format("Unable to create Tile Loader Bridge.\n%s", e.getMessage()));
                                                            }
                                                             rootItem.getChildren().add(item);
                                                         });

        tree.setRoot(rootItem);
        tree.setShowRoot(false);

        return tree;
    }

    public TreeView<TileLoaderBridge> getTree()
    {
        return this.tree;
    }

    public void addTreeItem(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        TreeItem<TileLoaderBridge> item = new TreeItem<>(new TileLoaderBridge(tileStoreReader));
        this.tree.getRoot().getChildren().add(item);
    }

    public void addTreeItem(final TileStoreReader tileStoreReader, final TreeItem<TileLoaderBridge> rootItem) throws TileStoreException
    {
        TreeItem<TileLoaderBridge> item = new TreeItem<>(new TileLoaderBridge(tileStoreReader));
        rootItem.getChildren().add(item);
    }

    private TreeItem<TileLoaderBridge> createDummyRootNode() throws Exception
    {
        try(TileStoreReader dummyReader = new TileStoreReader(){
                                                                   @Override
                                                                   public void close() throws Exception
                                                                   {
                                                                       //nada
                                                                   }

                                                                   @Override
                                                                   public BoundingBox getBounds() throws TileStoreException
                                                                   {
                                                                       return new BoundingBox(0.0,0.0,0.0,0.0);
                                                                   }

                                                                   @Override
                                                                   public long countTiles() throws TileStoreException
                                                                   {
                                                                       return 0;
                                                                   }

                                                                   @Override
                                                                   public long getByteSize() throws TileStoreException
                                                                   {
                                                                       return 0;
                                                                   }

                                                                   @Override
                                                                   public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public Set<Integer> getZoomLevels() throws TileStoreException
                                                                   {
                                                                       Set<Integer> set = new HashSet<>();
                                                                       set.add(-1);
                                                                       return set;
                                                                   }

                                                                   @Override
                                                                   public Stream<TileHandle> stream() throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public Stream<TileHandle> stream(final int zoomLevel)
                                                                           throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public CoordinateReferenceSystem getCoordinateReferenceSystem()
                                                                           throws TileStoreException
                                                                   {
                                                                       return new CoordinateReferenceSystem("EPSG", 4326);
                                                                   }

                                                                   @Override
                                                                   public String getName()
                                                                   {
                                                                       return "Dummy Root";
                                                                   }

                                                                   @Override
                                                                   public String getImageType() throws TileStoreException
                                                                   {
                                                                       return "none";
                                                                   }

                                                                   @Override
                                                                   public Dimensions<Integer> getImageDimensions()
                                                                           throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public TileScheme getTileScheme() throws TileStoreException
                                                                   {
                                                                       return null;
                                                                   }

                                                                   @Override
                                                                   public TileOrigin getTileOrigin()
                                                                   {
                                                                       return null;
                                                                   }
                                                               })
            {
                return new TreeItem<>(new TileLoaderBridge(dummyReader));
            }
    }

    /*
     * The following methods resize the window properly
     * (non-Javadoc)
     * @see javafx.scene.Parent#layoutChildren()
     */
    @Override protected void layoutChildren()
    {
        double width  = this.getWidth();
        double height = this.getHeight();
        this.layoutInArea(this.tree, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(final double height)
    {
        return 250;
    }

    @Override protected double computePrefHeight(final double width)
    {
        return 500;
    }
}
