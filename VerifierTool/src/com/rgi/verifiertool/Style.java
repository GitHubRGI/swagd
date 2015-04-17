package com.rgi.verifiertool;

import javafx.scene.paint.Color;

/**
 * @author Jenifer Cochran
 *
 */
public enum Style
{
    /**
     * Light Green "#B4D38D"
     */
    lightGreen("#B4D38D"),
    /**
     * Light Orange "#FAD167"
     */
    lightOrange("#FAD167"),
    /**
     * Light Red "#FFA859"
     */
    lightRed("#FFA859"),
    /**
     * Dark Blue "#296EA3"
     */
    darkAquaBlue("#296EA3"),
    /**
     * Bright Green "#547A14"
     */
    brightGreen("#547A14"),
    /**
     * Bright Orange "#E68206"
     */
    brightOrange("#E68206"),
    /**
     * Bright Red "#BC2F05"
     */
    brightRed("#BC2F05"),
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
    greyBlue("#9BBED6"),
    /**
     * Light Grey "#808080"
     */
    lightGrey("#808080"),

    /**
     * Black "#000000"
     */
    black("#000000");

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
     * @return Main font of the program
     */
    public static String getMainFont()
    {
        return "SanSerif";
    }

    /**
     * @return A font with fixed width (used for the reasons in error messages)
     */
    public static String getFixedWidthFont()
    {
        return "Consolas";
    }

    /**
     * @return The color in hex format with the pound sign (#) in front(i.e. "#123456")
     */
    public String getHex()
    {
        return this.hexColor;
    }
}
