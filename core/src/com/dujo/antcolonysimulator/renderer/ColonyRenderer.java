package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.colony.Colony;

import java.awt.geom.Point2D;

public class ColonyRenderer {
    private Colony colony;
    private Color color;

    ColonyRenderer(Colony colony, Color color){
        this.colony = colony;
        this.color = color;
    }

    void render(SpriteBatch batch, TextureRegion[] textureRegions){
        batch.setColor(color);

        batch.draw(
                textureRegions[2],
                colony.getPosition().x - Colony.COLONY_SIZE / 2f,
                colony.getPosition().y - Colony.COLONY_SIZE / 2f,
                Colony.COLONY_SIZE,
                Colony.COLONY_SIZE
        );

        batch.setColor(1f, 1f, 1f, 1f);

    }

    void renderAnts(SpriteBatch batch, TextureRegion[] textureRegions){
        for(Ant ant : colony.getAnts()) {
            /*for (Point2D.Float sample : ant.samples)
                batch.draw(
                        textureRegions[1],
                        sample.x,
                        sample.y,
                        1f,
                        1f
                );*/


            batch.setColor(color);
            batch.draw(
                    textureRegions[0],
                    ant.getPosition().x  - Ant.ANT_SIZE / 2f,
                    ant.getPosition().y  - Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE,
                    Ant.ANT_SIZE,
                    1f,
                    1f,
                    (float) Math.toDegrees(ant.getDirection().getCurrentAngle())
            );

            if(ant.isHoldingFood()){
                batch.setColor(1f,1f,1f,1f);
                batch.draw(
                        textureRegions[1],
                        ant.getPosition().x,
                        ant.getPosition().y,
                        1f,
                        1f
                );
            }
        }

    }

}
