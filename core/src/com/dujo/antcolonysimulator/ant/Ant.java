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
    public static final float ROTATE_SPEED = 10f;

    public static final float ANT_SIZE = 4f;
    public static final float ANT_FIELD_OF_VIEW = (float) (Math.PI);
    public static final float VIEW_PARTITION_COUNT = 3;
    public static final float ANT_DELTA_FIELD_OF_VIEW = ANT_FIELD_OF_VIEW / VIEW_PARTITION_COUNT;
    public static final int TOTAL_SAMPLE_COUNT = 100;
    public static final float ANT_VIEW_RANGE = ANT_SIZE * 10f;
    public static final float ANT_PICKUP_RANGE = 2f;
    public static final int MAX_FOOD_CARRY = 10;
    public static final float PHEROMONE_INTENSITY_THRESHOLD = 10f;

    public static final float ROTATION_PERIOD = 0.05f;
    public static final float PHEROMONE_DROP_PERIOD = 0.25f;

    public static final float DESIRE_TO_WANDER = 0.01f;
    public final float CHANCE_TO_REPEL = 0.5f;


    private final Point2D.Float position;
    private final MoveDirection moveDirection;
    private AntGoal goal;
    private AntPheromone pheromone;
    private float pheromoneIntensity;
    private int foodHoldingAmount;
    private final Cooldown rotationCooldown;
    private final Cooldown pheromoneDropCooldown;
    private final World world;
    private final Colony colony;

    public Ant(Point2D.Float spawnPoint, float spawnAngle, World world, Colony colony){
        position = spawnPoint;
        moveDirection = new MoveDirection();
        moveDirection.setCurrentAngle(spawnAngle);
        moveDirection.setTargetAngle(spawnAngle);

        goal = AntGoal.LOOK_FOR_FOOD;
        pheromone = AntPheromone.TO_COLONY;
        pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;

        rotationCooldown = new Cooldown(ROTATION_PERIOD);
        pheromoneDropCooldown = new Cooldown(PHEROMONE_DROP_PERIOD);

        this.world = world;
        this.colony = colony;
    }

    /**
     * Base update method for Ant class that calls all other update methods
     *
     * @param deltaTime time passed since last frame
     */
    public void update(float deltaTime){
        if(world.isPointOutOfBounds(position))
            return;

        pheromoneDropCooldown.update(deltaTime);
        if(pheromoneDropCooldown.isReadyAutoReset() && pheromone != null){
            world.setPheromone(position, pheromone, pheromoneIntensity, colony.getColonyIndex());

            pheromoneIntensity *= 0.99f;
            if(pheromoneIntensity < PHEROMONE_INTENSITY_THRESHOLD){
                goal = AntGoal.RETURN_TO_COLONY;
                pheromone = null;
            }
        }

        rotationCooldown.update(deltaTime);
        if(rotationCooldown.isReadyAutoReset())
            moveDirection.rotate(deltaTime, ROTATE_SPEED);

        updateTarget();
        checkGoal();
        updatePosition(deltaTime);
    }

    /**
     * Method that samples random points in ants view and chooses the most favourable target
     */
    private void updateTarget() {
        if(moveDirection.getGoalPoint() != null)
            return;

        SampleResult totalSampleResult = new SampleResult(0f);
        for(int i = 0; i < 3; ++i) {
            // Go through view field in partition, turn towards the partition with the highest pheromone intensity
            SampleResult partitionSampleResult = new SampleResult(ANT_DELTA_FIELD_OF_VIEW - ANT_DELTA_FIELD_OF_VIEW * i);
            List<SamplePoint> samplePoints = getSamplePoints(
                    TOTAL_SAMPLE_COUNT / 3,
                    ANT_FIELD_OF_VIEW / 3, ANT_VIEW_RANGE,
                    partitionSampleResult.angleOffset
            );

            for (SamplePoint samplePoint : samplePoints) {
                WorldCell sampleCell = world.getCell(samplePoint.point);

                // Distance to samples goes into calculation of score for this partition
                partitionSampleResult.averageSampleDistance += samplePoint.distance;

                if (goal == AntGoal.LOOK_FOR_FOOD) {
                    if (sampleCell.isFoodOnCell()) {
                        partitionSampleResult.goalPoint = samplePoint.point;
                        break;
                    }

                    partitionSampleResult.pheromoneIntensity += sampleCell.getPheromoneOnCell(AntPheromone.TO_FOOD, colony.getColonyIndex());
                    partitionSampleResult.repellentIntensity += sampleCell.getPheromoneOnCell(AntPheromone.REPELLENT, colony.getColonyIndex());

                } else if (goal == AntGoal.RETURN_TO_COLONY || goal == AntGoal.REPEL_FROM_TRAIL) {
                    // If returning home with no food and food is in sight, update goal
                    if (foodHoldingAmount == 0 && sampleCell.isFoodOnCell()) {
                        partitionSampleResult.goalPoint = samplePoint.point;
                        goal = AntGoal.LOOK_FOR_FOOD;
                        break;
                    }

                    if (arePointsInRangeOfEachOther(samplePoint.point, colony.getPosition(), ANT_PICKUP_RANGE)) {
                        partitionSampleResult.goalPoint = samplePoint.point;
                        break;
                    }

                    partitionSampleResult.pheromoneIntensity += sampleCell.getPheromoneOnCell(AntPheromone.TO_COLONY, colony.getColonyIndex());
                }
            }

            partitionSampleResult.averageSampleDistance /= samplePoints.size();
            partitionSampleResult.calculateScore();

            // If goal is found ignore everything else
            if(partitionSampleResult.goalPoint != null){
                moveDirection.setGoalPoint(partitionSampleResult.goalPoint);
                moveDirection.setTargetVector(position, moveDirection.getGoalPoint());
                return;
            }

            if(partitionSampleResult.score > totalSampleResult.score){
                totalSampleResult.score = partitionSampleResult.score;
                totalSampleResult.pheromoneIntensity = partitionSampleResult.pheromoneIntensity;
                totalSampleResult.angleOffset = partitionSampleResult.angleOffset;
            }

            if(partitionSampleResult.repellentIntensity > totalSampleResult.repellentIntensity){
                totalSampleResult.repellentIntensity = partitionSampleResult.repellentIntensity;
            }
        }

        // If ants sees repellent there is only a small chance that it will follow and also start repelling
        if(foodHoldingAmount == 0 && totalSampleResult.repellentIntensity > 0f && (float) Math.random() <= CHANCE_TO_REPEL){
            goal = AntGoal.REPEL_FROM_TRAIL;
            pheromone = null;
        }else if(totalSampleResult.pheromoneIntensity > 0f) // Some pheromones were sampled
            moveDirection.setTargetAngle(moveDirection.getCurrentAngle() + totalSampleResult.angleOffset);
        else if(Math.random() < DESIRE_TO_WANDER) // No pheromones in sight, chance to choose random target
            moveDirection.setRandomTarget();
    }

    /**
     * Method that simply moves the ant after making sure he will not run into a wall
     *
     * @param deltaTime time passed since last frame
     */
    private void updatePosition(float deltaTime){
        // Degrade pheromones even if not repelling simply for walking over them
        if(goal == AntGoal.REPEL_FROM_TRAIL)
            world.degradePheromone(position, 0.5f);
        else
            world.degradePheromone(position, 0.99f);

        Collision collision = world.getFirstCollision(
                position,
                moveDirection.getCurrentAngle(),
                5f
        );

        // Update target so ant doesn't hit wall
        if(collision.getNormalVector() != null){
            Vector2 collisionVector = new Vector2(moveDirection.getCurrentVector());
            collisionVector.x *= collision.getNormalVector().x != 0f ? -1f : 1f;
            collisionVector.y *= collision.getNormalVector().y != 0f ? -1f : 1f;

            moveDirection.setGoalPoint(null);
            moveDirection.setTargetVector(collisionVector);

            Vector2 averageVector = new Vector2(collisionVector);
            averageVector.add(moveDirection.getTargetVector());
            averageVector.nor();
            moveDirection.setCurrentVector(averageVector);

        }

        position.x += moveDirection.getCurrentVector().x * MOVE_SPEED * deltaTime;
        position.y += moveDirection.getCurrentVector().y * MOVE_SPEED * deltaTime;
    }

    /**
     * Method that checks if ant is close enough to his goal and if that goal is still valid
     */
    private void checkGoal(){
        if(moveDirection.getGoalPoint() == null || !isAntInRangeOfPoint(moveDirection.getGoalPoint()))
            return;

        if(goal == AntGoal.LOOK_FOR_FOOD){
            if(!world.isFoodOnPoint(moveDirection.getGoalPoint())) {
                moveDirection.setGoalPoint(null);
                return;
            }

            foodHoldingAmount = world.takeFood(moveDirection.getGoalPoint(), MAX_FOOD_CARRY);

            boolean isFoodLeft = false;
            // Sample random points and start repelling if you don't see any more food
            for (SamplePoint samplePoint : getSamplePoints(
                    90,
                    ANT_FIELD_OF_VIEW,
                    ANT_VIEW_RANGE / 2f,
                    0f)){
                if (world.isFoodOnPoint(samplePoint.point)) {
                    isFoodLeft = true;
                    break;
                }
            }

            if(!isFoodLeft) {
                goal = AntGoal.REPEL_FROM_TRAIL;
                pheromone = null;
                pheromoneIntensity = World.MAX_REPELLENT_INTENSITY;
                world.setPheromone(position, AntPheromone.REPELLENT, pheromoneIntensity, colony.getColonyIndex());
            } else{
                goal = AntGoal.RETURN_TO_COLONY;
                pheromone = AntPheromone.TO_FOOD;
                pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;
            }

        }else {
            colony.addFood(foodHoldingAmount);
            foodHoldingAmount = 0;

            goal = AntGoal.LOOK_FOR_FOOD;
            pheromone = AntPheromone.TO_COLONY;
            pheromoneIntensity = World.MAX_PHEROMONE_INTENSITY;
        }

        moveDirection.setGoalPoint(null);
        moveDirection.setCurrentVector(moveDirection.getCurrentVectorOpposite());
        moveDirection.setTargetVector(moveDirection.getCurrentVectorOpposite());
    }

    /**
     * Method that gets a number of random points in front of the ant sampling
     *
     * @param sampleCount number of points to generate
     * @param fieldOfView angle under which points must fall
     * @param viewRange max distance from ant to point
     * @param angleOffset angle offset for partition sample points generation
     * @return list of generated points
     */
    private List<SamplePoint> getSamplePoints(int sampleCount, float fieldOfView, float viewRange, float angleOffset){
        List<SamplePoint> pointList = new ArrayList<>();

        for(int i = 0; i < sampleCount; ++i){
            float angle = moveDirection.getCurrentAngle() + (float) Math.random() * fieldOfView - fieldOfView / 2 + angleOffset;

            viewRange = world.getFirstCollision(position, angle, viewRange).getDistance();
            float scalar = (float) Math.random() * viewRange;

            Point2D.Float point =  new Point2D.Float(
                    (float) Math.cos(angle) * scalar + position.x,
                    (float) Math.sin(angle) * scalar + position.y
            );

            if(!world.isPointOutOfBounds(point))
                pointList.add(new SamplePoint(point, scalar));
        }
        return pointList;
    }

    private boolean isAntInRangeOfPoint(Point2D.Float point){
        return arePointsInRangeOfEachOther(position, point, ANT_PICKUP_RANGE);
    }

    private boolean arePointsInRangeOfEachOther(Point2D.Float pointA, Point2D.Float pointB, float radius) {
        return pointA.x <= pointB.x + radius && pointA.x >= pointB.x - radius &&
                pointA.y <= pointB.y + radius && pointA.y >= pointB.y - radius;
    }

    /**
     * Class that describes the result of a sampling
     */
    private static class SampleResult{
        final float DISTANCE_FACTOR = 25f;

        float angleOffset;
        float pheromoneIntensity;
        float repellentIntensity;
        Point2D.Float goalPoint;
        float averageSampleDistance;
        float score;

        SampleResult(float angleOffset){
            this.angleOffset = angleOffset;
        }

        void calculateScore() {
            score = pheromoneIntensity + averageSampleDistance * DISTANCE_FACTOR;
        }
    }

    /**
     * Class that wraps a world point and the distance between it and the ant
     */
    private static class SamplePoint{
        Point2D.Float point;
        float distance;

        SamplePoint(Point2D.Float point, float distance){
            this.point = point;
            this.distance = distance;
        }

    }

    public Point2D.Float getPosition() {
        return position;
    }

    public MoveDirection getMoveDirection() {
        return moveDirection;
    }

    public boolean isHoldingFood(){
        return foodHoldingAmount > 0;
    }

}
