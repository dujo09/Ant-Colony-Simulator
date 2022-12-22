package com.dujo.antcolonysimulator.colony;

import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.common.Cooldown;
import com.dujo.antcolonysimulator.world.World;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Colony {
    public static final int ANT_MAX_CAPACITY = 200;

    public static final float COLONY_RADIUS = 10f;

    public static final float ANT_CREATE_PERIOD = 2f;
    public static final int ANT_CREATE_COST = 100;

    private final int colonyIndex;
    private Point2D.Float position;
    private final List<Ant> ants;
    private int storedFood;
    private final Cooldown antCreateCooldown;
    private final World world;

    public Colony(int colonyIndex, Point2D.Float position, World world){
        this.colonyIndex = colonyIndex;

        this.position = position;

        this.world = world;

        ants = new ArrayList<>();

        float deltaAngle = (float) Math.PI * 2 / ANT_MAX_CAPACITY;
        float directionAngle = 0f;

        for(int i = 0; i < ANT_MAX_CAPACITY; ++i){
            directionAngle += deltaAngle;
            ants.add(new Ant(new Point2D.Float(position.x, position.y), directionAngle, world, this));
        }

        antCreateCooldown = new Cooldown(ANT_CREATE_PERIOD);

        storedFood = 0;

    }

    /**
     * Method that updates the colony and all the ants in the colony
     *
     * @param deltaTime time passed since last frame
     */
    public void update(float deltaTime){
        antCreateCooldown.update(deltaTime);

        if(antCreateCooldown.isReadyAutoReset()){
            createAnt();
        }

        for(Ant ant : ants)
            ant.update(deltaTime);
    }

    private void createAnt(){
        if(ants.size() < ANT_MAX_CAPACITY && storedFood > ANT_CREATE_COST){
            ants.add(new Ant(new Point2D.Float(position.x, position.y), 0f, world, this));
            storedFood -= ANT_CREATE_COST;
        }
    }

    public void addFood(int amount){
        storedFood += amount;
    }

    public Point2D.Float getPosition() {
        return position;
    }

    public List<Ant> getAnts(){
        return ants;
    }

    public int getColonyIndex(){
        return colonyIndex;
    }
}
