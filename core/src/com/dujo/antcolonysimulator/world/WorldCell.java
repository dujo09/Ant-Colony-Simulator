package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.dujo.antcolonysimulator.ant.Pheromone;
import sun.awt.windows.WPrinterJob;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class WorldCell {
    private final int row;
    private final int column;
    private boolean isWall;
    private int food;
    private final float[][] colonyPheromones;
    private boolean isActive;

    public WorldCell(int row, int column){
        this.row = row;
        this.column = column;

        colonyPheromones = new float[World.MAX_COLONY_COUNT][Pheromone.values().length];
    }

    void update(boolean[] coloniesToDraw){
        isActive = false;

        if(food > 0 || isWall){
            isActive = true;
        }else{
            for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
                if(coloniesToDraw[i]){
                    for(int j = 0; j < Pheromone.values().length; ++j){
                        if (colonyPheromones[i][j] > 0f) {
                            isActive = true;
                            break;
                        }
                    }
                }
            }
        }

    }

    public int getRow() {
        return row;
    }

    public int getColumn(){
        return column;
    }

    public float getPheromoneOnCell(Pheromone pheromone, int colonyID){
        return colonyPheromones[colonyID][pheromone.ordinal()];
    }

    public float getPheromoneOnCell(Pheromone pheromone){
        float intensity = 0f;
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            intensity += colonyPheromones[i][pheromone.ordinal()];
        }
        return intensity;
    }

    public void setPheromoneOnCell(Pheromone pheromone, float intensity, int colonyID){
        intensity = MathUtils.clamp(intensity, 0f, World.MAX_PHEROMONE_INTENSITY);
        colonyPheromones[colonyID][pheromone.ordinal()] = Math.max(colonyPheromones[colonyID][pheromone.ordinal()], intensity);
    }


    public void degradePheromone(float ratio, int colonyID){
        degradePheromone(Pheromone.TO_COLONY, ratio, colonyID);
        degradePheromone(Pheromone.TO_FOOD, ratio, colonyID);
        degradePheromone(Pheromone.REPELLENT, ratio, colonyID);
    }

    public void degradePheromone(float ratio){
        degradePheromone(Pheromone.TO_COLONY, ratio);
        degradePheromone(Pheromone.TO_FOOD, ratio);
        degradePheromone(Pheromone.REPELLENT, ratio);
    }

   public void degradePheromone(Pheromone pheromone, float ratio, int colonyID){
        colonyPheromones[colonyID][pheromone.ordinal()] *= ratio;
   }

   public void degradePheromone(Pheromone pheromone, float ratio){
       for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
           colonyPheromones[i][pheromone.ordinal()] *= ratio;
       }
   }

    public void removePheromones(){
        degradePheromone(0f);
    }

    public void removePheromones(int colonyID){
        degradePheromone(0f, colonyID);
    }

    public int getFoodOnCell() {
        return food;
    }

    public void setFoodOnCell(int food) {
        this.food = food;
    }

    public int takeFoodOnCell(int amount){
        if(amount > food){
            amount = food;
            food = 0;
        }else{
            food -= amount;
        }

        return amount;
    }

    public boolean isFoodOnCell(){
        return food > 0;
    }

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    boolean isActive(){
        return isActive;
    }

}
