package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.math.Vector2;
import com.dujo.antcolonysimulator.ant.AntPheromone;
import com.dujo.antcolonysimulator.common.Cooldown;

import java.awt.geom.Point2D;


public class World {
    public static final int MAX_COLONY_COUNT = 3;
    public static final float MAX_PHEROMONE_INTENSITY = 100.0f;
    public static final float MAX_REPELLENT_INTENSITY = 200.0f;
    public static final float MAX_FOOD_ON_CELL = 100.0f;

    public static float PHEROMONE_DEGRADE_PERIOD = 1.0f;


    private final int columnCount;
    private final int rowCount;
    private final float cellSize;
    private final WorldCell[] cellsArray;
    private final Cooldown pheromoneDegradeCooldown;

    public World(int columnCount, int rowCount, int cellSize){
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cellSize = cellSize;

        cellsArray = new WorldCell[rowCount * columnCount];

        pheromoneDegradeCooldown = new Cooldown(PHEROMONE_DEGRADE_PERIOD);

        for(int i = 0; i < rowCount * columnCount; ++i){
            int row = i / columnCount;
            int column = i % columnCount;

            cellsArray[i] = new WorldCell(row, column, cellSize);

            // Create wall border around whole world
            if(row == 0 || row == 1 || row == rowCount - 1 || row == rowCount - 2 ||
                    column == 0 || column == 1 || column == columnCount - 1 || column == columnCount - 2)
                cellsArray[i].setWall(true);
        }
    }

    public void update(float deltaTime){
        pheromoneDegradeCooldown.update(deltaTime);

        if(pheromoneDegradeCooldown.isReadyAutoReset())
            for (int i = 0; i < rowCount * columnCount; ++i)
                cellsArray[i].degradeAllPheromonesOnCell(0.99f);
    }

    public Collision getFirstCollision(Point2D.Float position, float directionAngle, float targetDistance){
        Collision collision = new Collision(targetDistance);

        int column = (int) (position.x  / cellSize);
        int row = (int) (position.y / cellSize);
        Vector2 directionVector = new Vector2((float) Math.cos(directionAngle), (float) Math.sin(directionAngle));

        Vector2 stepVector =
                new Vector2(directionVector.x > 0f ? 1f : -1f , directionVector.y > 0f ? 1f : -1f);

        float distanceVertical =
                ((column + (stepVector.x > 0f ? 1f : 0f)) * cellSize - position.x) / directionVector.x;
        float distanceHorizontal =
                ((row + (stepVector.y > 0f ? 1f : 0f)) * cellSize - position.y) / directionVector.y;

        float deltaX = Math.abs(cellSize / directionVector.x);
        float deltaY = Math.abs(cellSize / directionVector.y);
        float distance = 0f;

        while(distance < targetDistance){
            boolean isDistanceVerticalSmaller = distanceVertical < distanceHorizontal;

            distance = isDistanceVerticalSmaller ? distanceVertical : distanceHorizontal;
            distanceVertical += isDistanceVerticalSmaller ? deltaX : 0f;
            distanceHorizontal += isDistanceVerticalSmaller ? 0f : deltaY;
            column += isDistanceVerticalSmaller ? stepVector.x : 0;
            row += isDistanceVerticalSmaller ? 0 : stepVector.y;

            if(checkCell(row, column) || getCell(row, column).isWall()){
                collision.setDistance(distance);
                collision.setNormalVector(new Vector2(
                        isDistanceVerticalSmaller ? 1f : 0f,
                        !isDistanceVerticalSmaller ? 1f : 0f));
                break;
            }
        }
        return collision;

    }

    public void setPheromone(Point2D.Float point, AntPheromone pheromone, float intensity, int colonyID){
        getCell(point).setPheromoneOnCell(pheromone, intensity, colonyID);
    }

    public void degradePheromone(Point2D.Float point, float ratio){
        getCell(point).degradeAllPheromonesOnCell(ratio);
    }

    public int takeFood(Point2D.Float point, int amount){
        return getCell(point).takeFoodOnCell(amount);
    }

    public boolean isFoodOnPoint(Point2D.Float point){
        return getCell(point).isFoodOnCell();
    }

    public void setFood(Point2D.Float point, int food, float brushSize){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / cellSize - brushSize / 2);
        int endRow = (int) (point.y / cellSize + brushSize / 2);
        int startColumn = (int) (point.x / cellSize - brushSize / 2);
        int endColumn = (int) (point.x / cellSize + brushSize / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= rowCount){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= columnCount){
                    continue;
                }

                WorldCell cell = getCell(i, j);
                if(cell.isWall()){
                    continue;
                }

                cell.setFoodOnCell(food);
            }
        }

    }
    public void removeFood(Point2D.Float point, float brushSize){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / cellSize - brushSize / 2);
        int endRow = (int) (point.y / cellSize + brushSize / 2);
        int startColumn = (int) (point.x / cellSize - brushSize / 2);
        int endColumn = (int) (point.x / cellSize + brushSize / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= rowCount){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= columnCount){
                    continue;
                }

                WorldCell cell = getCell(i, j);
                if(cell.isWall()){
                    continue;
                }

                cell.setFoodOnCell(0);
            }
        }

    }
    public void setWall(Point2D.Float point, float brushSize){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / cellSize - brushSize / 2);
        int endRow = (int) (point.y / cellSize + brushSize / 2);
        int startColumn = (int) (point.x / cellSize - brushSize / 2);
        int endColumn = (int) (point.x / cellSize + brushSize / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= rowCount){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= columnCount){
                    continue;
                }
                WorldCell cell = getCell(i, j);
                cell.setWall(true);
                cell.removePheromones();
            }
        }

    }
    public void removeWall(Point2D.Float point, float brushSize){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / cellSize - brushSize / 2);
        int endRow = (int) (point.y / cellSize + brushSize / 2);
        int startColumn = (int) (point.x / cellSize - brushSize / 2);
        int endColumn = (int) (point.x / cellSize + brushSize / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= rowCount){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= columnCount){
                    continue;
                }
                WorldCell cell = getCell(i, j);
                cell.setWall(false);
            }
        }

    }

    public WorldCell[] getCells() {
        return cellsArray;
    }

    public WorldCell getCell(Point2D.Float point) {
        return cellsArray[(int) (point.y / cellSize) * columnCount + (int) (point.x / cellSize)];
    }

    public WorldCell getCell(int row, int column){
        return cellsArray[row * columnCount + column];
    }

    public boolean checkCell(int row, int column){
        int index = row * columnCount + column;
        return index < 0 || index >= rowCount * columnCount;
    }

    public boolean isPointOutOfBounds(Point2D.Float point){
        return point.x < 0f || point.x >= columnCount * cellSize ||
                point.y < 0f || point.y >= rowCount * cellSize;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public float getCellSize() {
        return cellSize;
    }
}
