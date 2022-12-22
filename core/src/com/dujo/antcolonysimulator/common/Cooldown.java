package com.dujo.antcolonysimulator.common;

/**
 * Class that holds a value and a target value, updating means incrementing
 * the current value, used for limiting how often something happens (like
 * how often an ant drops a pheromone
 */
public class Cooldown {
    private float currentValue;
    private final float targetValue;

    public Cooldown(float targetValue){
        currentValue = 0;
        this.targetValue = targetValue;

    }

    public void update(float deltaTime){
        currentValue += deltaTime;

    }

    public boolean isReady(){
        return currentValue >= targetValue;

    }

    public boolean isReadyAutoReset(){
        if(currentValue < targetValue)
            return false;
        currentValue = 0f;
        return true;
    }

    public void reset(){
        currentValue = 0f;
    }

    public void block(float time){
        currentValue -= time;
    }

}
