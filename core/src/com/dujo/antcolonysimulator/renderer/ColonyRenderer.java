package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.world.World;

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
                colony.getPosition().x - Colony.COLONY_RADIUS,
                colony.getPosition().y - Colony.COLONY_RADIUS,
                Colony.COLONY_RADIUS * 2f,
                Colony.COLONY_RADIUS * 2f
        );

        batch.setColor(1f, 1f, 1f, 1f);

    }

    void renderAnts(SpriteBatch batch, TextureRegion[] textureRegions){
        for(Ant ant : colony.getAnts()) {

            batch.setColor(color);
            /*for (int i = 0; i < ant.samples.size(); ++i)
                batch.draw(
                        textureRegions[1],
                        ant.samples.get(i).x,
                        ant.samples.get(i).y,
                        World.CELL_SIZE,
                        World.CELL_SIZE
                );


            batch.draw(
                    textureRegions[1],
                    ant.getPosition().x,
                    ant.getPosition().y,
                    1f,
                    1f,
                    5f,
                    1f,
                    1f,
                    1f,
                    (float)Math.toDegrees(ant.getDirection().getTargetAngle()));*/

            batch.setColor(color);
            batch.draw(
                    textureRegions[0],
                    ant.getPosition().x - Ant.ANT_SIZE / 2f,
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
                Vector2 directionOffset = ant.getDirection().getCurrentVector().cpy().scl(3.0f * Ant.ANT_SIZE / 4.0f);

                batch.setColor(1f,1f,1f,1f);
                batch.draw(
                        textureRegions[1],
                        ant.getPosition().x - Ant.ANT_SIZE / 2.0f + directionOffset.x,
                        ant.getPosition().y - Ant.ANT_SIZE / 2.0f + directionOffset.y,
                        Ant.ANT_SIZE,
                        Ant.ANT_SIZE
                );
            }
        }

    }

}
