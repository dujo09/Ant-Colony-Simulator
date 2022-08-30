package com.dujo.antcolonysimulator.ant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.common.Cooldown;
import com.dujo.antcolonysimulator.world.Collision;
import com.dujo.antcolonysimulator.world.World;
import com.dujo.antcolonysimulator.world.WorldCell;

import javax.swing.text.View;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Ant {
    public static final float MOVE_SPEED = 20f;
    public static final float ROTATE_SPEED = 30f;

    public static final float ANT_SIZE = 4f;
    public static final float ANT_VIEW_ANGLE = (float) (Math.PI / 2);
    public static final float ANT_VIEW_RANGE = ANT_SIZE * 7f;
    public static final float ANT_PICKUP_RANGE = 2.5f;
    public static final int MAX_FOOD_CARRY = 10;

    public static final float ROTATION_PERIOD = 0.25f;
    public static final float PHEROMONE_DROP_PERIOD = 0.5f;
    public static final float REPEL_PERIOD = 120f;

    public static final float DESIRE_TO_WANDER = 0.01f;
    final float CHANCE_TO_REPEL = 0.01f;

    private final Point2D.Float position;
    private final Direction direction;
    private Goal goal;
    private Pheromone pheromone;
    private float pheromoneIntensity;
    private int foodHolding;
    private final Cooldown rotationCooldown;
    private final Cooldown pheromoneDropCooldown;
    private final Cooldown repelCooldown;
    private final World world;
    private final Colony colony;

    public Ant(Point2D.Float position, float initialAngle, World world, Colony colony) {
        this.position = position;
        direction = new Direction();
        direction.setCurrentAngle(initialAngle);
        direction.setTargetAngle(initialAngle);

        goal = Goal.LOOK_FOR_FOOD;
        pheromone = Pheromone.TO_COLONY;
        pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;

        rotationCooldown = new Cooldown(ROTATION_PERIOD);
        pheromoneDropCooldown = new Cooldown(PHEROMONE_DROP_PERIOD);
        repelCooldown = new Cooldown(REPEL_PERIOD);

        this.world = world;
        this.colony = colony;
    }

    //public List<SamplePoint> samples = new ArrayList<>();
    public WorldCell maxCell;

    public void update(float deltaTime) {
        //samples = getSamplePoints(36, ANT_VIEW_ANGLE, ANT_VIEW_RANGE);

        rotationCooldown.update(deltaTime);
        pheromoneDropCooldown.update(deltaTime);
        repelCooldown.update(deltaTime);

        if (pheromoneDropCooldown.isReady() && pheromone != null) {
            pheromoneDropCooldown.reset();

            world.setPheromone(position, pheromone, pheromoneIntensity, colony.getIndex());
            pheromoneIntensity -= 0.1f;
            if (pheromoneIntensity == 0f) {
                goal = Goal.RETURN_TO_COLONY;
                pheromone = null;
            }
        }

        if (rotationCooldown.isReady()) {
            rotationCooldown.reset();

            direction.rotate(deltaTime, ROTATE_SPEED);
        }

        updateTarget();

        updatePosition(deltaTime);

    }

    private void updateTarget() {
        if (direction.getGoalPoint() != null) {
            direction.setTargetVector(position, direction.getGoalPoint());
            return;
        }

        if (Math.random() < DESIRE_TO_WANDER) {
            direction.setRandomTarget();
        }

        SampleResult sampleResult = new SampleResult();

        for (SamplePoint samplePoint : getSamplePoints(32, ANT_VIEW_ANGLE, ANT_VIEW_RANGE)) {
            WorldCell sampleCell = world.getCell(samplePoint.point);

            if (sampleCell.isFoodOnCell() && pheromone == Pheromone.REPELLENT) {
                goal = Goal.LOOK_FOR_FOOD;
                pheromone = Pheromone.TO_COLONY;
                sampleResult.maxCell = sampleCell;
                sampleResult.isGoalFound = true;
                break;
            }

            if (goal == Goal.LOOK_FOR_FOOD) {
                if (sampleCell.isFoodOnCell()) {
                    sampleResult.maxCell = sampleCell;
                    sampleResult.isGoalFound = true;
                    break;
                }

                if (sampleCell.getPheromoneOnCell(Pheromone.TO_FOOD, colony.getIndex()) * samplePoint.distance >
                        sampleResult.maxPheromoneIntensity * samplePoint.distance ) {
                    sampleResult.maxPheromoneIntensity = sampleCell.getPheromoneOnCell(Pheromone.TO_FOOD, colony.getIndex());
                    sampleResult.maxCell = sampleCell;
                }

                if (sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getIndex()) > sampleResult.maxRepellentIntensity) {
                    sampleResult.maxRepellentIntensity = sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getIndex());
                }

            } else if (goal == Goal.RETURN_TO_COLONY) {
                if (arePointsInRangeOfEachOther(samplePoint.point, colony.getPosition(), ANT_PICKUP_RANGE)) {
                    sampleResult.maxCell = sampleCell;
                    sampleResult.isGoalFound = true;
                    break;
                }

                if (sampleCell.getPheromoneOnCell(Pheromone.TO_COLONY, colony.getIndex()) * samplePoint.distance  >
                        sampleResult.maxPheromoneIntensity * samplePoint.distance) {
                    sampleResult.maxPheromoneIntensity = sampleCell.getPheromoneOnCell(Pheromone.TO_COLONY, colony.getIndex());
                    sampleResult.maxCell = sampleCell;
                }

            }
        }
        maxCell = sampleResult.maxCell;

    if(repelCooldown.isReady() && foodHolding == 0 && sampleResult.maxRepellentIntensity > 0f && (float) Math.random() <= CHANCE_TO_REPEL) {
        repelCooldown.reset();

        world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getIndex());
        goal = Goal.RETURN_TO_COLONY;
        pheromone = null;

    }else if(sampleResult.isGoalFound){
        direction.setTargetVector(position, sampleResult.maxCell.getCellCenter());
        direction.setGoalPoint(sampleResult.maxCell.getCellCenter());
    }else if(sampleResult.maxPheromoneIntensity > 0f){
        direction.setTargetVector(position, sampleResult.maxCell.getCellCenter());
    }
}


    private void updatePosition(float deltaTime){
        // Degrade trail due to simply passing over the pheromones
        world.getCell(position).degradePheromone(0.99f);
        // Degrade repellent if holding food
        if(foodHolding > 0 && world.getCell(position).getPheromoneOnCell(Pheromone.REPELLENT, colony.getIndex()) > 0f){
            world.getCell(position).degradePheromone(Pheromone.REPELLENT, 0.99f, colony.getIndex());
        }

        // If goal point for direction is set then check if point is still valid
        // (if food wasn't taken by another ant in the meantime)
        // and if ant is close enough to fulfill goal
        if(direction.getGoalPoint() != null){
            if(goal == Goal.LOOK_FOR_FOOD){
                if(world.isFoodOnPoint(direction.getGoalPoint())) {
                    if (isPositionInRangeOfPoint(direction.getGoalPoint())) {
                        foodHolding = world.takeFood(direction.getGoalPoint(), MAX_FOOD_CARRY);

                        goal = Goal.RETURN_TO_COLONY;
                        pheromone = null;
                        pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;
                        boolean setRepellent = true;

                        for (SamplePoint samplePoint : getSamplePoints(90, ANT_VIEW_ANGLE, ANT_VIEW_RANGE / 3f)){
                            if (world.isFoodOnPoint(samplePoint.point)) {
                                setRepellent = false;
                                pheromone = Pheromone.TO_FOOD;
                                break;
                            }
                        }

                        if(setRepellent){
                            world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getIndex());
                            repelCooldown.reset();
                        }

                        direction.setGoalPoint(null);
                        direction.setCurrentVector(direction.getCurrentVectorOpposite());
                    }
                }else{
                    direction.setGoalPoint(null);
                }
            }else if(goal == Goal.RETURN_TO_COLONY){
                if(isPositionInRangeOfPoint(colony.getPosition())){
                    if(foodHolding > 0){
                        colony.addFood(foodHolding);
                        foodHolding = 0;
                    }
                    goal = Goal.LOOK_FOR_FOOD;
                    pheromone = Pheromone.TO_COLONY;
                    pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;

                    direction.setGoalPoint(null);
                    direction.setCurrentVector(direction.getCurrentVectorOpposite());
                }
            }
        }

        Collision collision = world.getFirstCollision(position, direction.getCurrentAngle(), 5f);

        if(collision.getNormalVector() != null){
            Vector2 collisionVector = new Vector2(direction.getCurrentVector());
            collisionVector.x *= collision.getNormalVector().x != 0f ? -1f : 1f;
            collisionVector.y *= collision.getNormalVector().y != 0f ? -1f : 1f;

            direction.setGoalPoint(null);
            direction.setCurrentVector(collisionVector);
            direction.setTargetVector(collisionVector);

        }

        position.x += direction.getCurrentVector().x * MOVE_SPEED * deltaTime;
        position.y += direction.getCurrentVector().y * MOVE_SPEED * deltaTime;

    }

    public Point2D.Float getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isHoldingFood(){
        return foodHolding > 0;
    }

    private List<SamplePoint> getSamplePoints(int sampleCount, float viewAngle, float viewRange){
        List<SamplePoint> pointList = new ArrayList<>();

        for(int i = 0; i < sampleCount; ++i){
            float angle = direction.getCurrentAngle() + (float) Math.random() * viewAngle - viewAngle / 2;
            float maxDistance = world.getFirstCollision(position, angle, viewRange).getDistance();
            float scalar = (float) Math.random() * maxDistance;

            Point2D.Float point =  new Point2D.Float(
                    (float) Math.cos(angle) * scalar + position.x,
                    (float) Math.sin(angle) * scalar + position.y
            );

            if(!World.isPointOutOfBounds(point)){
                pointList.add(new SamplePoint(point, scalar));
            }
        }
        return pointList;
    }

    private boolean isPositionInRangeOfPoint(Point2D.Float point){
        return arePointsInRangeOfEachOther(position, point, ANT_PICKUP_RANGE);
    }

    private boolean arePointsInRangeOfEachOther(Point2D.Float pointA, Point2D.Float pointB, float radius) {
        return pointA.x <= pointB.x + radius && pointA.x >= pointB.x - radius &&
                pointA.y <= pointB.y + radius && pointA.y >= pointB.y - radius;
    }

    private static class SamplePoint{
        Point2D.Float point;
        float distance;

        SamplePoint(Point2D.Float point, float distance){
            this.point = point;
            this.distance = distance;

        }

    }

    private static class SampleResult{
        float maxPheromoneIntensity;
        float maxRepellentIntensity;
        WorldCell maxCell;
        boolean isGoalFound;

    }

}
