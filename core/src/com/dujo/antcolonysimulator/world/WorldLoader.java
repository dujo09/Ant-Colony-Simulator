package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public final class WorldLoader {
    public static final Color WALL_COLOR = new Color(161f, 161f, 161f);
    public static final Color FOOD_COLOR = new Color(13f, 255f, 0f);
    public static final int CELL_SIZE = 1;

    public static void test(Pixmap image){
        return;
    }

    public static World loadWorld(Pixmap worldImage){
        int width = worldImage.getWidth();
        int height = worldImage.getHeight();
        int[] colorData = new int[width * height];

        for(int row = 0; row < height; ++row){
            for(int column = 0; column < width; ++column){
                colorData[row + column * width] = worldImage.getPixel(column, row);
            }
        }

        worldImage.dispose();

        World world = new World(width, height, CELL_SIZE);
        WorldCell[] cells = world.getCells();

        for(int i = 0; i < colorData.length; ++i){
            int blue = colorData[i] & 0xff;
            int green = (colorData[i] & 0xff00) >> 8;
            int red = (colorData[i] & 0xff0000) >> 16;
            int alpha = (colorData[i] & 0xff000000) >>> 24;

            Color color = new Color(red, green, blue);
            Gdx.app.log("", ""+color.getRGB());

            if(color == WALL_COLOR){
                cells[i].setWall(true);
            }else if(color == FOOD_COLOR){
                cells[i].setFoodOnCell((int)(alpha / 255f));
            }
        }

        return world;
    }

}
