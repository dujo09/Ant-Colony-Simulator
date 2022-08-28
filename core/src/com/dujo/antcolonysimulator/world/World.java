package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.math.Vector2;
import com.dujo.antcolonysimulator.ant.Pheromone;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.common.Cooldown;

import java.awt.geom.Point2D;

public class World {
    public static final int MAX_COLONY_COUNT = 3;
    public static final float MAX_PHEROMONE_INTENSITY = 100f;
    public static final float MAX_FOOD_ON_CELL = 100f;

    public static final int COLUMN_COUNT = 400;
    public static final int ROW_COUNT = 400;
    public static final float CELL_SIZE = 2;

    public static float PHEROMONE_DEGRADE_PERIOD = 5f;

    private final WorldCell[] cells;
    private final Cooldown pheromoneDegradeCooldown;

    public World(){
        cells = new WorldCell[ROW_COUNT * COLUMN_COUNT];

        pheromoneDegradeCooldown = new Cooldown(PHEROMONE_DEGRADE_PERIOD);

        for(int i = 0; i < ROW_COUNT * COLUMN_COUNT; ++i){
            int row = i / COLUMN_COUNT;
            int column = i % COLUMN_COUNT;

            cells[i] = new WorldCell(row, column);

            // Create wall border around whole world
            if(row == 0 || row == ROW_COUNT - 1 || column == 0 || column == COLUMN_COUNT - 1){
                cells[i].setWall(true);
            }
        }
    }

    public void update(float deltaTime){
        pheromoneDegradeCooldown.update(deltaTime);

        for(int i = 0; i < ROW_COUNT * COLUMN_COUNT; ++i){
            cells[i].update();
            if(pheromoneDegradeCooldown.isReady()){
                cells[i].degradePheromone(0.99f);
            }
        }

        if(pheromoneDegradeCooldown.isReady()){
            pheromoneDegradeCooldown.reset();
        }

    }

    /**
     * Method for getting the first collision in a direction
     *
     * @param position the current position of the Ant
     * @param directionAngle the current angle of the Ant
     * @param targetDistance the target distance of the Ant
     * @return null if no collision, a collision otherwise
     */
    public Collision getFirstCollision(Point2D.Float position, float directionAngle, float targetDistance){
        int column = (int) (position.x  / CELL_SIZE);
        int row = (int) (position.y / CELL_SIZE);
        Vector2 directionVector = new Vector2((float) Math.cos(directionAngle), (float) Math.sin(directionAngle));

        Vector2 stepVector =
                new Vector2(directionVector.x > 0f ? 1f : -1f , directionVector.y > 0f ? 1f : -1f);

        float distanceVertical =
                ((column + (stepVector.x > 0f ? 1f : 0f)) * CELL_SIZE - position.x) / directionVector.x;
        float distanceHorizontal =
                ((row + (stepVector.y > 0f ? 1f : 0f)) * CELL_SIZE - position.y) / directionVector.y;

        float deltaX = Math.abs(CELL_SIZE / directionVector.x);
        float deltaY = Math.abs(CELL_SIZE / directionVector.y);
        float distance = 0f;

        while(distance < targetDistance){
            boolean isDistanceVerticalSmaller = distanceVertical < distanceHorizontal;

            distance = isDistanceVerticalSmaller ? distanceVertical : distanceHorizontal;
            distanceVertical += isDistanceVerticalSmaller ? deltaX : 0f;
            distanceHorizontal += isDistanceVerticalSmaller ? 0f : deltaY;
            column += isDistanceVerticalSmaller ? stepVector.x : 0;
            row += isDistanceVerticalSmaller ? 0 : stepVector.y;

            if(checkCell(row, column) || getCell(row, column).isWall()){
                Collision collision = new Collision();
                collision.setDistance(distance);
                collision.setNormalVector(new Vector2(
                        isDistanceVerticalSmaller ? 1f : 0f,
                        !isDistanceVerticalSmaller ? 1f : 0f));
                return collision;
            }
        }
        return null;

    }

    public float getPheromone(Point2D.Float point, Pheromone pheromone, int colonyID){
        return getCell(point).getPheromoneOnCell(pheromone, colonyID);
    }

    public void setPheromone(Point2D.Float point, Pheromone pheromone, float intensity, int colonyID){
        getCell(point).setPheromoneOnCell(pheromone, intensity, colonyID);

    }

    public int getFood(Point2D.Float point){
        return getCell(point).getFoodOnCell();

    }

    public void setFood(Point2D.Float point, int food){
        getCell(point).setFoodOnCell(food);
    }


    public int takeFood(Point2D.Float point, int amount){
        return getCell(point).takeFoodOnCell(amount);
    }

    public boolean isFoodOnPoint(Point2D.Float point){
        return getCell(point).isFoodOnCell();
    }

    public void setFood(Point2D.Float point, int food, float size){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / CELL_SIZE - size / 2);
        int endRow = (int) (point.y / CELL_SIZE + size / 2);
        int startColumn = (int) (point.x / CELL_SIZE - size / 2);
        int endColumn = (int) (point.x / CELL_SIZE + size / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= ROW_COUNT){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= COLUMN_COUNT){
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
    public void removeFood(Point2D.Float point, float size){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / CELL_SIZE - size / 2);
        int endRow = (int) (point.y / CELL_SIZE + size / 2);
        int startColumn = (int) (point.x / CELL_SIZE - size / 2);
        int endColumn = (int) (point.x / CELL_SIZE + size / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= ROW_COUNT){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= COLUMN_COUNT){
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
    public void setWall(Point2D.Float point, float size){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / CELL_SIZE - size / 2);
        int endRow = (int) (point.y / CELL_SIZE + size / 2);
        int startColumn = (int) (point.x / CELL_SIZE - size / 2);
        int endColumn = (int) (point.x / CELL_SIZE + size / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= ROW_COUNT){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= COLUMN_COUNT){
                    continue;
                }
                WorldCell cell = getCell(i, j);
                cell.setWall(true);
                cell.removePheromones();
            }
        }

    }
    public void removeWall(Point2D.Float point, float size){
        if(isPointOutOfBounds(point)){
            return;
        }

        int startRow = (int) (point.y / CELL_SIZE - size / 2);
        int endRow = (int) (point.y / CELL_SIZE + size / 2);
        int startColumn = (int) (point.x / CELL_SIZE - size / 2);
        int endColumn = (int) (point.x / CELL_SIZE + size / 2);

        for(int i = startRow; i < endRow; ++i){
            if(i < 0 || i >= ROW_COUNT){
                continue;
            }

            for(int j = startColumn; j < endColumn; ++j){
                if(j < 0 || j >= COLUMN_COUNT){
                    continue;
                }
                WorldCell cell = getCell(i, j);
                cell.setWall(false);
            }
        }

    }

    public WorldCell[] getCells() {
        return cells;
    }

    public WorldCell getCell(Point2D.Float point){
        return cells[(int) (point.y / CELL_SIZE) * COLUMN_COUNT + (int) (point.x / CELL_SIZE)];
    }

    public WorldCell getCell(int row, int column){
        return cells[row * COLUMN_COUNT + column];
    }

    public boolean checkCell(int row, int column){
        int index = row * COLUMN_COUNT + column;
        return index < 0 || index >= ROW_COUNT * COLUMN_COUNT;
    }

    public static boolean isPointOutOfBounds(Point2D.Float point){
        return point.x < 0f || point.x >= COLUMN_COUNT * CELL_SIZE ||
                point.y < 0f || point.y >= ROW_COUNT * CELL_SIZE;
    }

}
