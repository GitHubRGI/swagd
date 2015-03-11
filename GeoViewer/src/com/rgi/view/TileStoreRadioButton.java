package com.rgi.view;

import javax.swing.JRadioButton;

import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Jenifer Cochran
 *
 */
public class TileStoreRadioButton extends JRadioButton
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    TileStoreReader store;
    
    /**
     * Creates a RadioButton that has the TileStoreReader information attached to it
     * @param store The TileStoreReader that with the tiles associated with this button
     */
    public TileStoreRadioButton(TileStoreReader store)
    {
        super(store.getName());
        this.store  = store;
    }
    
    /**
     * @return the TileStoreReader that reads the tiles with this button
     */
    public TileStoreReader getTileStore()
    {
        return this.store;
    }
}
