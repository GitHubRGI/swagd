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
    String text;
    JRadioButton button;
    
    /**
     * Creates a RadioButton that has the TileStoreReader information attached to it
     * @param store The TileStoreReader that with the tiles associated with this button
     */
    public TileStoreRadioButton(TileStoreReader store)
    {
        super(store.getName());
        this.store  = store;
        this.text   = store.getName();
        //this.button = new JRadioButton(this.text);
        
    }
    
    /**
     * @return the TileStoreReader that reads the tiles with this button
     */
    public TileStoreReader getTileStore()
    {
        return this.store;
    }
    
    /**
     * @return JRadioButton 
     */
    public JRadioButton getButton()
    {
        return this.button;
    }
}
