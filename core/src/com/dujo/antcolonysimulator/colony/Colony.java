package com.dujo.antcolonysimulator.colony;

import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.common.Cooldown;
import com.dujo.antcolonysimulator.world.World;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Colony {
    public static final int ANT_COUNT = 100;

    public static final float COLONY_RADIUS = 10f;

    public static final float ANT_CREATE_PERIOD = 2f;
    public static final int ANT_CREATE_COST = 100;

    private final int index;
    private Point2D.Float position;
    private final List<Ant> ants;
    private int food;
    private final Cooldown antCreateCooldown;
    private final World world;

    public Colony(int index, Point2D.Float position, World world){
        this.index = index;

        this.position = position;

        this.world = world;

        ants = new ArrayList<>();

        float deltaAngle = (float) Math.PI * 2 / ANT_COUNT;
        float directionAngle = 0f;

        for(int i = 0; i < ANT_COUNT; ++i){
            directionAngle += deltaAngle;
            ants.add(new Ant(new Point2D.Float(position.x, position.y), directionAngle, world, this));
        }

        antCreateCooldown = new Cooldown(ANT_CREATE_PERIOD);

        food = 0;

    }

    public void update(float deltaTime){
        antCreateCooldown.update(deltaTime);

        if(antCreateCooldown.isReady()){
            createAnt();
        }

        for(Ant ant : ants){
            ant.update(deltaTime);
        }

        //Gdx.app.log("Colony", "COLONY " + index + ": " + food + " FOOD | " + ants.size() + " ANTS");
    }

    private void createAnt(){
        if(food > ANT_CREATE_COST){
            //ants.add(new Ant(new Point2D.Float(position.x, position.y), 0f, world, this));
            food -= ANT_CREATE_COST;
            antCreateCooldown.reset();
        }
    }

    public void addFood(int amount){
        food += amount;
    }

    public Point2D.Float getPosition() {
        return position;
    }

    public void setPosition(Point2D.Float position) {
        this.position = position;
    }

    public List<Ant> getAnts(){
        return ants;
    }

    public int getIndex(){
        return index;
    }

}
