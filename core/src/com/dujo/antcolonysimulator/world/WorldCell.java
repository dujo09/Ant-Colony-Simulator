package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.math.MathUtils;
import com.dujo.antcolonysimulator.ant.Pheromone;

import java.awt.geom.Point2D;

public class WorldCell {
    private final int row;
    private final int column;
    private final float cellSize;
    private boolean isWall;
    private int food;
    private final float[][] colonyPheromones;
    private boolean isEmpty;

    public WorldCell(int row, int column, float cellSize){
        this.row = row;
        this.column = column;
        this.cellSize = cellSize;

        colonyPheromones = new float[World.MAX_COLONY_COUNT][Pheromone.values().length];
    }

    void update(){
        isEmpty = true;

        if(food > 0 || isWall){
            isEmpty = false;
        }else{
            for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
                for(int j = 0; j < Pheromone.values().length; ++j){
                    if (colonyPheromones[i][j] > 0f) {
                        isEmpty = false;
                        break;
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

    public Point2D.Float getCellCenter(){
        return new Point2D.Float(
                column * cellSize - cellSize / 2f,
                row * cellSize - cellSize / 2f
        );
    }

    public float getPheromoneOnCell(Pheromone pheromone, int colonyID){
        return colonyPheromones[colonyID][pheromone.ordinal()];
    }

    public void setPheromoneOnCell(Pheromone pheromone, float intensity, int colonyID){
        if(!isWall) {
            if (pheromone == Pheromone.REPELLENT) {
                intensity = MathUtils.clamp(intensity, 0f, World.MAX_REPELLENT_INTENSITY);
            } else {
                intensity = MathUtils.clamp(intensity, 0f, World.MAX_PHEROMONE_INTENSITY);
            }
            colonyPheromones[colonyID][pheromone.ordinal()] = Math.max(colonyPheromones[colonyID][pheromone.ordinal()], intensity);
        }
    }

    public void degradePheromone(float ratio, int colonyID){
        degradePheromone(Pheromone.TO_COLONY, ratio, colonyID);
        degradePheromone(Pheromone.TO_FOOD, ratio, colonyID);
        degradePheromone(Pheromone.REPELLENT, ratio, colonyID);
    }

    public void degradePheromone(float ratio){
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][Pheromone.TO_COLONY.ordinal()] *= ratio;
        }
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][Pheromone.TO_FOOD.ordinal()] *= ratio;
        }
        for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
            colonyPheromones[i][Pheromone.REPELLENT.ordinal()] *= ratio;
        }
    }

    public void degradePheromone(Pheromone pheromone, float ratio, int colonyID){
        colonyPheromones[colonyID][pheromone.ordinal()] *= ratio;
   }

    public void removePheromones(){
        degradePheromone(0f);
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

    public boolean isEmpty(){
        return isEmpty;
    }

}
