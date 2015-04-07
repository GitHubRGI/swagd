package com.rgi.verifiertool;

import javafx.scene.paint.Color;

public enum Style
{
    darkAquaBlue("#296EA3"),
    brightGreen("#00DA7D"),
    brightOrange("#FFBB10"),
    brightRed("#D92334"),
    white("#FCFCFD"),
    brightBlue("#2D8CD5"),
    greyBlue("#9BBED6");

    private String hexColor;
    Style(final String color)
    {
        this.hexColor = color;
    }

    public Color toColor()
    {
        return Color.rgb(Integer.valueOf(this.hexColor.substring( 1, 3 ), 16 ),
                         Integer.valueOf(this.hexColor.substring( 3, 5 ), 16 ),
                         Integer.valueOf(this.hexColor.substring( 5, 7 ), 16 ));
    }

    public static String getFont()
    {
        return "SanSerif";
    }

    public String getHex()
    {
        return this.hexColor;
    }
}
