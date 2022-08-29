package com.dujo.antcolonysimulator.world;

import com.badlogic.gdx.math.Vector2;

public class Collision {
    private Vector2 normalVector;
    private float distance;

    Collision(float distance){
        this.distance = distance;
    }

    public Vector2 getNormalVector() {
        return normalVector;
    }

    public void setNormalVector(Vector2 normalVector) {
        this.normalVector = normalVector;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
