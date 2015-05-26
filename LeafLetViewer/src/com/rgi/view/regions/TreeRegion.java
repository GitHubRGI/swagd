package com.rgi.view.regions;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.Region;

import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.view.TileLoaderBridge;

public class TreeRegion extends Region
{
    private final Collection<TileStoreReader> tileStoreReaders;
    private final TreeView<TileLoaderBridge> tree;

    public TreeRegion(final Collection<TileStoreReader> tileStoreReaders, final TreeItem<TileLoaderBridge> rootItem)
    {
        this.tileStoreReaders = tileStoreReaders;

        this.tree = this.createTree(rootItem);

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
