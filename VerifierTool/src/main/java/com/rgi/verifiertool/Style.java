package com.rgi.verifiertool;

import javafx.scene.paint.Color;

/**
 * @author Jenifer Cochran
 *
 */
public enum Style
{
    /**
     * Dark Blue "#296EA3"
     */
    darkAquaBlue("#296EA3"),
    /**
     * Bright Green "#00DA7D"
     */
    brightGreen("#00DA7D"),
    /**
     * Bright Orange "#FFBB10"
     */
    brightOrange("#FFBB10"),
    /**
     * Bright Red "#D92334"
     */
    brightRed("#D92334"),
    /**
     * White "#FCFCFD"
     */
    white("#FCFCFD"),
    /**
     * Bright Blue "#2D8CD5"
     */
    brightBlue("#2D8CD5"),
    /**
     * Grey Blue "#9BBED6"
     */
    greyBlue("#9BBED6");

    private String hexColor;
    Style(final String color)
    {
        this.hexColor = color;
    }

    /**
     * @return Color object associated with the String Hex Value
     */
    public Color toColor()
    {
        return Color.rgb(Integer.valueOf(this.hexColor.substring( 1, 3 ), 16 ),
                         Integer.valueOf(this.hexColor.substring( 3, 5 ), 16 ),
                         Integer.valueOf(this.hexColor.substring( 5, 7 ), 16 ));
    }

    /**
     * @return font of the program
     */
    public static String getFont()
    {
        return "SanSerif";
    }

    /**
     * @return The color in hex format with the pound sign (#) in front(i.e. "#123456")
     */
    public String getHex()
    {
        return this.hexColor;
    }
}
