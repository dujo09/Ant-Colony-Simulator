package com.dujo.antcolonysimulator.ant;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    public static final float MOVE_SPEED = 12f;
    public static final float ROTATE_SPEED = (float) (6f * Math.PI);

    public static final float ANT_SIZE = 6f;
    public static final float ANT_VIEW_ANGLE = (float) (3 * Math.PI / 4);
    public static final int VIEW_FRACTION_COUNT = 3;
    public static final float ANT_VIEW_RANGE = ANT_SIZE * 7f;
    public static final float ANT_PICKUP_RANGE = 2.5f;
    public static final int MAX_FOOD_CARRY = 10;

    public static final float ROTATION_PERIOD = 0.25f;
    public static final float PHEROMONE_DROP_PERIOD = 0.25f;
    public static final float REPEL_PERIOD = 120f;
    public static final float VITALITY_DROP_PERIOD = 10f;

    public static final float MAX_VITALITY = 100f;

    public static final float DESIRE_TO_WANDER = 0.15f;
    final float CHANCE_TO_REPEL = 0.01f;

    private final Point2D.Float position;
    private final Direction direction;
    private AngleOffset[] angleOffsets;
    private Goal goal;
    private Pheromone pheromone;
    private float pheromoneIntensity;
    private float vitality;
    private int foodHolding;
    private final Cooldown vitalityDropCooldown;
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

        initialiseAngleOffsets();

        goal = Goal.LOOK_FOR_FOOD;
        pheromone = Pheromone.TO_COLONY;
        pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;

        vitality = MAX_VITALITY;

        vitalityDropCooldown = new Cooldown(VITALITY_DROP_PERIOD);
        rotationCooldown = new Cooldown(ROTATION_PERIOD);
        pheromoneDropCooldown = new Cooldown(PHEROMONE_DROP_PERIOD);
        repelCooldown = new Cooldown(REPEL_PERIOD);

        this.world = world;
        this.colony = colony;
    }

    private void initialiseAngleOffsets(){
        angleOffsets = new AngleOffset[VIEW_FRACTION_COUNT];
        float deltaAngle = ANT_VIEW_ANGLE / VIEW_FRACTION_COUNT;
        int zeroOffsetIndex = VIEW_FRACTION_COUNT / 2;

        angleOffsets[zeroOffsetIndex] = new AngleOffset(0f);

        for(int i = zeroOffsetIndex - 1; i >= 0; --i){
            angleOffsets[i] = new AngleOffset(deltaAngle / 2 * (zeroOffsetIndex - i));
        }

        for(int i = zeroOffsetIndex + 1; i < VIEW_FRACTION_COUNT; ++i){
            angleOffsets[i] = new AngleOffset(-deltaAngle / 2 * (i - zeroOffsetIndex));
        }

    }

    public void update(float deltaTime){
        vitalityDropCooldown.update(deltaTime);
        rotationCooldown.update(deltaTime);
        pheromoneDropCooldown.update(deltaTime);
        repelCooldown.update(deltaTime);

        if(vitalityDropCooldown.isReady()){
            vitalityDropCooldown.reset();

            vitality -= 1f;
            if(vitality <= 0f){
                // DIes
            }
            if(vitality < 0.75f * MAX_VITALITY){
                goal = Goal.RETURN_TO_COLONY;
                if(pheromone == Pheromone.TO_COLONY){
                    pheromone = null;
                }
                direction.setCurrentVector(direction.getCurrentVectorOpposite());
            }
        }

        if(pheromoneDropCooldown.isReady() && pheromone != null){
            pheromoneDropCooldown.reset();

            world.setPheromone(position, pheromone, pheromoneIntensity, colony.getID());
            pheromoneIntensity -= 0.1f;
        }

        if(rotationCooldown.isReady()) {
            rotationCooldown.reset();

            direction.rotate(deltaTime, ROTATE_SPEED);
        }

        updateAngleOffsets();

        updateTarget();

        updatePosition(deltaTime);

    }

    private void updateTarget(){
        if(direction.getGoalPoint() != null){
            direction.setTargetVector(position, direction.getGoalPoint());
            return;
        }

        if(Math.random() < DESIRE_TO_WANDER) {
            direction.setRandomTargetAngle();
            return;
        }

        float maxAngle = direction.getCurrentAngle();
        float maxAnglePheromoneIntensity = 0f;
        float maxRepellentIntensity = 0f;

        /* Ant view field is split into N fields, examine each field and set the target angle
        * to the field with the most intense pheromone trail or goal cell
        * */
        for(AngleOffset angleOffset : angleOffsets) {
            SampleResult sampleResult = new SampleResult();

            for (Point2D.Float samplePoint : getSamplePoints(
                    30,
                    ANT_VIEW_ANGLE / VIEW_FRACTION_COUNT,
                    angleOffset.collision == null ? ANT_VIEW_RANGE : angleOffset.collision.getDistance(),
                    angleOffset.offset,
                    ANT_PICKUP_RANGE
            )) {
                WorldCell sampleCell = world.getCell(samplePoint);

                if (sampleCell.isFoodOnCell() && pheromone == Pheromone.REPELLENT) {
                    goal = Goal.LOOK_FOR_FOOD;
                    pheromone = Pheromone.TO_COLONY;
                    sampleResult.goalPoint = samplePoint;
                    break;
                }

                if (goal == Goal.LOOK_FOR_FOOD) {
                    if (sampleCell.isFoodOnCell()) {
                        sampleResult.goalPoint = samplePoint;
                        break;
                    }

                    sampleResult.totalPheromoneIntensity += sampleCell.getPheromoneOnCell(Pheromone.TO_FOOD, colony.getID());

                    if (sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getID()) > sampleResult.maxRepellentIntensity) {
                        sampleResult.maxRepellentIntensity = sampleCell.getPheromoneOnCell(Pheromone.REPELLENT, colony.getID());
                    }

                } else if (goal == Goal.RETURN_TO_COLONY) {
                    if (arePointsInRangeOfEachOther(samplePoint, colony.getPosition(), ANT_PICKUP_RANGE)) {
                        sampleResult.goalPoint = samplePoint;
                        break;
                    }

                    sampleResult.totalPheromoneIntensity += sampleCell.getPheromoneOnCell(Pheromone.TO_COLONY, colony.getID());

                }
            }

            if(sampleResult.goalPoint != null){
                direction.setGoalPoint(sampleResult.goalPoint);
                return;
            }

            if(sampleResult.totalPheromoneIntensity > maxAnglePheromoneIntensity){
                maxAnglePheromoneIntensity = sampleResult.totalPheromoneIntensity;
                maxAngle = direction.getCurrentAngle() + angleOffset.offset;
            }

            if(sampleResult.maxRepellentIntensity > maxRepellentIntensity){
                maxRepellentIntensity = sampleResult.maxRepellentIntensity;
            }
        }

        if(repelCooldown.isReady() && foodHolding == 0 && maxRepellentIntensity > 0f && (float) Math.random() <= CHANCE_TO_REPEL){
            repelCooldown.reset();

            world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getID());
            goal = Goal.RETURN_TO_COLONY;
            pheromone = null;

        }else if(maxAnglePheromoneIntensity > 0f){
            direction.setTargetAngle(maxAngle);
        }

    }

    private void updatePosition(float deltaTime){
        // Degrade trail due to simply passing over the pheromones
        world.getCell(position).degradePheromone(0.99f);
        // Degrade repellent if holding food
        if(foodHolding > 0 && world.getCell(position).getPheromoneOnCell(Pheromone.REPELLENT, colony.getID()) > 0f){
            world.getCell(position).degradePheromone(Pheromone.REPELLENT, 0.99f, colony.getID());
        }

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
                                ANT_VIEW_RANGE / 5f,
                                0f,
                                0f)){
                            if (world.isFoodOnPoint(point)) {
                                setRepellent = false;
                                pheromone = Pheromone.TO_FOOD;
                                break;
                            }
                        }

                        if(setRepellent){
                            world.setPheromone(position, Pheromone.REPELLENT, 100f, colony.getID());
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

        Collision collision = angleOffsets[(int) (VIEW_FRACTION_COUNT / 2)].collision;

        if(collision != null && collision.getDistance() <= 5f){
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

    private void updateAngleOffsets(){
        for(AngleOffset angleOffset : angleOffsets){

            angleOffset.collision = world.getFirstCollision(
                    position,
                    direction.getCurrentAngle() + angleOffset.offset,
                    ANT_VIEW_RANGE
            );
        }

    }

    /**
     * Method for getting random points used for sampling the world by
     * generating a random vector
     *
     * @param sampleCount the number of points to generate
     * @param viewAngle Ants view angle
     * @param viewRange Ants view range
     * @param angleOffset the angle offset for this sample
     * @param rangeOffset the range offset for this sample
     * @return the list of points to sample
     */
    private List<Point2D.Float> getSamplePoints(int sampleCount, float viewAngle, float viewRange,
                                                float angleOffset, float rangeOffset){
        List<Point2D.Float> pointList = new ArrayList<>();

        for(int i = 0; i < sampleCount; ++i){
            float angle = direction.getCurrentAngle() + (float) Math.random() * viewAngle - viewAngle / 2 + angleOffset * 2;
            float scalar = (float) Math.random() * viewRange;
            scalar = Math.max(scalar, rangeOffset);

            Point2D.Float samplePoint =  new Point2D.Float(
                    (float) Math.cos(angle) * scalar + position.x,
                    (float) Math.sin(angle) * scalar + position.y
            );

            if(!World.isPointOutOfBounds(samplePoint) && !world.getCell(samplePoint).isWall()){
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

    private static class AngleOffset{
        float offset;
        Collision collision;

        AngleOffset(float offset){
            this.offset = offset;

        }

    }

    private static class SampleResult{
        float totalPheromoneIntensity;
        float maxRepellentIntensity;
        Point2D.Float goalPoint;

    }

    public void draw(SpriteBatch batch, Color color,
                     TextureRegion antTextureRegion, TextureRegion carriedFood){
        batch.setColor(color);
        batch.draw(
                antTextureRegion,
                position.x  - ANT_SIZE / 2f, position.y  - ANT_SIZE / 2f,
                ANT_SIZE / 2f, ANT_SIZE / 2f,
                ANT_SIZE, ANT_SIZE,
                1f, 1f,
                (float) Math.toDegrees(direction.getCurrentAngle()));
        batch.setColor(1f,1f,1f,1f);

        if(foodHolding > 0){
            batch.draw(carriedFood, position.x, position.y, 1f, 1f);
        }

    }

}
