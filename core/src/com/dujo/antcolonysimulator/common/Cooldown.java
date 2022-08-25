package com.dujo.antcolonysimulator.common;

public class Cooldown {
    private float value;
    private final float target;

    public Cooldown(float target){
        value = 0;
        this.target = target;

    }

    public void update(float deltaTime){
        value += deltaTime;

    }

    public boolean isReady(){
        return value >= target;

    }

    public void reset(){
        value = 0f;
    }

    public void block(float time){
        value -= time;
    }

}
