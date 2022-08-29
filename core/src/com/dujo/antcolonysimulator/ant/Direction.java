package com.dujo.antcolonysimulator.ant;

import com.badlogic.gdx.math.Vector2;

import java.awt.geom.Point2D;

public class Direction {
    private float currentAngle;
    private float targetAngle;
    private Vector2 currentVector;
    private Vector2 targetVector;
    private Point2D.Float goalPoint;

    public Direction(){
        currentVector = new Vector2(
                (float) Math.cos(currentAngle),
                (float) Math.sin(currentAngle)
        );
        currentAngle = currentVector.angleRad();

        targetVector = new Vector2(
                (float) Math.cos(targetAngle),
                (float) Math.sin(targetAngle)
        );
        targetAngle = targetVector.angleRad();
    }

    public void rotate(float deltaTime, float rotateSpeed){
        int moveDirection = 0;
        float currentAngleOpposite = getCurrentVectorOpposite().angleRad();

        if(Math.abs(targetAngle - currentAngle) <= 0.2f){
            return;
        }else if(currentAngle >= 0f) {
            if (targetAngle <= 0f) {
                if (targetAngle < currentAngleOpposite) {
                    moveDirection = 1;
                } else {
                    moveDirection = -1;
                }
            }else{
                if(targetAngle > currentAngle){
                    moveDirection = 1;
                }else{
                    moveDirection = -1;
                }
            }
        } else {
            if(targetAngle >= 0f) {
                if (targetAngle > currentAngleOpposite) {
                    moveDirection = -1;
                } else {
                    moveDirection = 1;
                }
            }else{
                if(targetAngle > currentAngle){
                    moveDirection = 1;
                }else{
                    moveDirection = -1;
                }
            }
        }
        setCurrentAngle(currentAngle + moveDirection * rotateSpeed * deltaTime);

    }

    public float getCurrentAngle(){
        return currentAngle;
    }

    public void setCurrentAngle(float currentAngle){
        currentVector.x = (float) Math.cos(currentAngle);
        currentVector.y = (float) Math.sin(currentAngle);
        this.currentAngle = currentVector.angleRad();
    }

    public Vector2 getCurrentVector() {
        return currentVector;
    }

    public void setCurrentVector(Vector2 currentVector) {
        this.currentVector = currentVector;
        currentVector.nor();
        currentAngle = currentVector.angleRad();
    }

    public Vector2 getCurrentVectorOpposite(){
        return new Vector2(currentVector).scl(-1f);
    }

    public float getTargetAngle() {
        return targetAngle;
    }

    public void setTargetAngle(float targetAngle) {
        targetVector.x = (float) Math.cos(targetAngle);
        targetVector.y = (float) Math.sin(targetAngle);
        this.targetAngle = targetVector.angleRad();
    }

    public Vector2 getTargetVector() {
        return targetVector;
    }

    public void setTargetVector(Vector2 targetVector) {
        this.targetVector = targetVector;
        targetVector.nor();
        targetAngle = targetVector.angleRad();
    }

    public void setTargetVector(Point2D.Float pointA, Point2D.Float pointB){
        Vector2 targetVector = new Vector2(pointB.x - pointA.x, pointB.y - pointA.y);
        setTargetVector(targetVector);
    }

    public Point2D.Float getGoalPoint() {
        return goalPoint;
    }

    public void setGoalPoint(Point2D.Float goalPoint) {
        this.goalPoint = goalPoint;
    }

}
