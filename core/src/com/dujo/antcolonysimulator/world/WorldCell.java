package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.math.MathUtils;
import com.dujo.antcolonysimulator.ant.AntPheromone;

import java.awt.geom.Point2D;

public class WorldCell {
    private final int row;
    private final int column;
    private final float cellSize;
    private boolean isWall;
    private int foodOnCell;
    private final float[][] colonyPheromones;


    public WorldCell(int row, int column, float cellSize){
        this.row = row;
        this.column = column;
        this.cellSize = cellSize;

        colonyPheromones = new float[World.MAX_COLONY_COUNT][AntPheromone.values().length];
    }

    public int getRow() {
        return row;
    }

    public int getColumn(){
        return column;
    }

    public float getPheromoneOnCell(AntPheromone pheromone, int colonyID){
        return colonyPheromones[colonyID][pheromone.ordinal()];
    }

    public void setPheromoneOnCell(AntPheromone pheromone, float intensity, int colonyID){
        if(!isWall) {
            if (pheromone == AntPheromone.REPELLENT) {
                intensity = MathUtils.clamp(intensity, 0f, World.MAX_REPELLENT_INTENSITY);
            } else {
                intensity = MathUtils.clamp(intensity, 0f, World.MAX_PHEROMONE_INTENSITY);
            }
            colonyPheromones[colonyID][pheromone.ordinal()] = Math.max(colonyPheromones[colonyID][pheromone.ordinal()], intensity);
        }
    }

    public void degradeAllPheromonesOnCell(float ratio){
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][AntPheromone.TO_COLONY.ordinal()] *= ratio;
        }
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][AntPheromone.TO_FOOD.ordinal()] *= ratio;
        }
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][AntPheromone.REPELLENT.ordinal()] *= ratio;
        }
    }

    public void removePheromones(){
        degradeAllPheromonesOnCell(0f);
    }

    public int getFoodOnCell() {
        return foodOnCell;
    }

    public void setFoodOnCell(int food) {
        this.foodOnCell = food;
    }

    public int takeFoodOnCell(int amount){
        if(amount > foodOnCell){
            amount = foodOnCell;
            foodOnCell = 0;
        }else{
            foodOnCell -= amount;
        }

        return amount;
    }

    public boolean isFoodOnCell(){
        return foodOnCell > 0;
    }

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }
}
