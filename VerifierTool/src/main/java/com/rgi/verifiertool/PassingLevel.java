package com.rgi.verifiertool;

import java.awt.Color;

public enum PassingLevel
{
    /**
     * Set the font to green if GeoPackage passes verifier
     */
    Pass(Color.GREEN, "Passed"),
    /**
     * Set the font to yellow if GeoPackage has warnings but no errors in verifier
     */
    Warning(Color.orange, "Warning"),
    /**
     * Set the font to red if GeoPackage has failing requirements
     */
    Fail(Color.RED, "Failed");
    private Color color;
    private String text;

    PassingLevel(final Color color, final String text)
    {
        this.setColor(color);
        this.setText(text);
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(final String text)
    {
        this.text = text;
    }

    public Color getColor()
    {
        return this.color;
    }

    public void setColor(final Color color)
    {
        this.color = color;
    }
}
