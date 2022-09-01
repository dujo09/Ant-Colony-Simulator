package com.dujo.antcolonysimulator.ant;

import com.badlogic.gdx.math.Vector2;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.common.Cooldown;
import com.dujo.antcolonysimulator.world.Collision;
import com.dujo.antcolonysimulator.world.World;
import com.dujo.antcolonysimulator.world.WorldCell;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Ant {
    public static final float MOVE_SPEED = 20f;
    public static final float ROTATE_SPEED = 20f;

    public static final float ANT_SIZE = 4f;
    public static final float ANT_VIEW_ANGLE = (float) (3 * Math.PI / 4);
    public static final float DELTA_VIEW_ANGLE = ANT_VIEW_ANGLE / 3; // View angle is split into 3 parts: left, right and center
    public static final float ANT_VIEW_RANGE = ANT_SIZE * 7f;
    public static final float ANT_PICKUP_RANGE = World.CELL_SIZE * 2;
    public static final int MAX_FOOD_CARRY = 10;

    public static final float ROTATION_PERIOD = 0f;
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

    public Ant(Point2D.Float position, float initialAngle, World world, Colony colony){
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

    public List<Point2D.Float> samples = new ArrayList<>(); //DEBUG

    public void update(float deltaTime){
        // DEBUG
        samples.clear();
        for(int i = 0; i < 3; ++i) {
            samples.addAll(getSamplePoints(
                    20,
                    ANT_VIEW_ANGLE / 3,
                    ANT_VIEW_RANGE,
                    ANT_VIEW_ANGLE / 3  / 3 * i
            ));
        }

        rotationCooldown.update(deltaTime);
        pheromoneDropCooldown.update(deltaTime);
        repelCooldown.update(deltaTime);

        if(pheromoneDropCooldown.isReady() && pheromone != null){
            pheromoneDropCooldown.reset();

            world.setPheromone(position, pheromone, pheromoneIntensity, colony.getIndex());
            pheromoneIntensity -= 0.1f;
            if(pheromoneIntensity == 0f){
                goal = Goal.RETURN_TO_COLONY;
                pheromone = null;
            }
        }

        if(rotationCooldown.isReady()) {
            rotationCooldown.reset();

            direction.rotate(deltaTime, ROTATE_SPEED);
        }

        updateTarget();

        updatePosition(deltaTime);

    }

    private void updateTarget(){
        // If goal found just update direction to goal
        if(direction.getGoalPoint() != null){
            direction.setTargetVector(position, direction.getGoalPoint());
            return;
        }

        float maxAngle = direction.getCurrentAngle();
        float maxAnglePheromoneIntensity = 0f;
        float maxRepellentIntensity = 0f;

        // Go through view field in parts, look at left portion, then in front and then to the right
        // Turn towards the field with the highest pheromone intensity
        for(int i = 0; i < 3; ++i) {
            SampleResult sampleResult = new SampleResult(DELTA_VIEW_ANGLE - DELTA_VIEW_ANGLE * i);
            List<Point2D.Float> samplePoints = getSamplePoints(
                    20,
                    ANT_VIEW_ANGLE / 3, ANT_VIEW_RANGE,
                    sampleResult.angleOffset
            );

            for (Point2D.Float samplePoint : samplePoints) {
                WorldCell sampleCell = world.getCell(samplePoint);

                if (goal == Goal.LOOK_FOR_FOOD) {
                    if (sampleCell.isFoodOnCell()) {
                        sampleResult.goalPoint = samplePoint;
                        break;
                    }

                    sampleResult.totalPheromoneIntensity += sampleCell.getPheromoneOnCell(Pheromone.TO_FOOD, colony.getIndex());

                    if (sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getIndex()) > sampleResult.maxRepellentIntensity) {
                        sampleResult.maxRepellentIntensity = sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getIndex());
                    }

                } else if (goal == Goal.RETURN_TO_COLONY) {
                    if (arePointsInRangeOfEachOther(samplePoint, colony.getPosition(), ANT_PICKUP_RANGE)) {
                        sampleResult.goalPoint = samplePoint;
                        break;
                    }

                    sampleResult.totalPheromoneIntensity += sampleCell.getPheromoneOnCell(Pheromone.TO_COLONY, colony.getIndex());

                }
            }

            if(sampleResult.goalPoint != null){
                direction.setGoalPoint(sampleResult.goalPoint);
                return;
            }

            if(sampleResult.totalPheromoneIntensity > maxAnglePheromoneIntensity){
                maxAnglePheromoneIntensity = sampleResult.totalPheromoneIntensity;
                maxAngle = direction.getCurrentAngle() + sampleResult.angleOffset;
            }

            if(sampleResult.maxRepellentIntensity > maxRepellentIntensity){
                maxRepellentIntensity = sampleResult.maxRepellentIntensity;
            }

        }

        // If ants sees repellent there is a small chance that it will follow and also start repelling
        if(repelCooldown.isReady() && foodHolding == 0 && maxRepellentIntensity > 0f && (float) Math.random() <= CHANCE_TO_REPEL){
            repelCooldown.reset();

            world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getIndex());
            goal = Goal.RETURN_TO_COLONY;
            pheromone = null;

        }else if(maxAnglePheromoneIntensity > 0f){
            direction.setTargetAngle(maxAngle);
        }else if(false && Math.random() < DESIRE_TO_WANDER) {
            direction.setRandomTarget();
        }

    }

    private void updatePosition(float deltaTime){
        // Degrade trail due to simply passing over the pheromones
        world.getCell(position).degradePheromone(0.99f);

        // Check if goal is still valid and if close enough to fulfill it
        if(direction.getGoalPoint() != null){
            if(goal == Goal.LOOK_FOR_FOOD){
                if(world.isFoodOnPoint(direction.getGoalPoint())) {
                    if (isPositionInRangeOfPoint(direction.getGoalPoint())) {
                        foodHolding = world.takeFood(direction.getGoalPoint(), MAX_FOOD_CARRY);

                        goal = Goal.RETURN_TO_COLONY;
                        pheromone = null;
                        pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;
                        boolean setRepellent = true;

                        for (Point2D.Float point : getSamplePoints(
                                90,
                                ANT_VIEW_ANGLE,
                                ANT_VIEW_RANGE / 2f,
                                0f)){
                            if (world.isFoodOnPoint(point)) {
                                setRepellent = false;
                                pheromone = Pheromone.TO_FOOD;
                                break;
                            }
                        }

                        if(setRepellent){
                            world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getIndex());
                        }

                        direction.setGoalPoint(null);
                        direction.setCurrentVector(direction.getCurrentVectorOpposite());
                    }
                }else{
                    direction.setGoalPoint(null);
                }
            }else if(goal == Goal.RETURN_TO_COLONY){
                if(arePointsInRangeOfEachOther(position, colony.getPosition(), Colony.COLONY_RADIUS)){
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

        Collision collision = world.getFirstCollision(
                position,
                direction.getCurrentAngle(),
                5f
        );

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

    private List<Point2D.Float> getSamplePoints(int sampleCount, float viewAngle, float viewRange, float angleOffset){
        List<Point2D.Float> pointList = new ArrayList<>();

        for(int i = 0; i < sampleCount; ++i){
            float angle = direction.getCurrentAngle() + (float) Math.random() * viewAngle - viewAngle / 2 + angleOffset;

            viewRange = world.getFirstCollision(position, angle, viewRange).getDistance();
            float scalar = (float) Math.random() * viewRange;

            Point2D.Float samplePoint =  new Point2D.Float(
                    (float) Math.cos(angle) * scalar + position.x,
                    (float) Math.sin(angle) * scalar + position.y
            );

            if(!World.isPointOutOfBounds(samplePoint)){
                pointList.add(samplePoint);
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

    private static class SampleResult{
        float angleOffset;
        float totalPheromoneIntensity;
        float maxRepellentIntensity;
        Point2D.Float goalPoint;

        SampleResult(float angleOffset){
            this.angleOffset = angleOffset;
        }

    }

}
