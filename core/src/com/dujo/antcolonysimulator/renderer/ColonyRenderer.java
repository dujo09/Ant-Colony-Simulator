package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dujo.AntColonySimulation.TEXTURE_INDICES;
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.colony.Colony;

public class ColonyRenderer {
    private final Colony colony;
    private final Color colonyColor;

    ColonyRenderer(Colony colony, Color colonyColor){
        this.colony = colony;
        this.colonyColor = colonyColor;
    }

    void render(SpriteBatch spriteBatch, TextureRegion[] textureRegions){
        spriteBatch.setColor(colonyColor);

        spriteBatch.draw(
                textureRegions[TEXTURE_INDICES.COLONY_TEXTURE_INDEX.ordinal()],
                colony.getPosition().x - Colony.COLONY_RADIUS,
                colony.getPosition().y - Colony.COLONY_RADIUS,
                Colony.COLONY_RADIUS * 2f,
                Colony.COLONY_RADIUS * 2f
        );

        spriteBatch.setColor(1f, 1f, 1f, 1f);
    }

    void renderAnts(SpriteBatch spriteBatch, TextureRegion[] textureRegions){
        spriteBatch.setColor(colonyColor);

        for(Ant ant : colony.getAnts()) {
            spriteBatch.draw(
                    textureRegions[TEXTURE_INDICES.ANT_TEXTURE_INDEX.ordinal()],
                    ant.getPosition().x - Ant.ANT_SIZE / 2f,
                    ant.getPosition().y  - Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE / 2f,
                    Ant.ANT_SIZE,
                    Ant.ANT_SIZE,
                    1f,
                    1f,
                    (float) Math.toDegrees(ant.getMoveDirection().getCurrentAngle())
            );

            if(ant.isHoldingFood()){
                Vector2 directionOffset = ant.getMoveDirection().getCurrentVector().cpy().scl(3.0f * Ant.ANT_SIZE / 4.0f);

                spriteBatch.setColor(1f,1f,1f,1f);
                spriteBatch.draw(
                        textureRegions[TEXTURE_INDICES.HOLDING_FOOD_TEXTURE_INDEX.ordinal()],
                        ant.getPosition().x - Ant.ANT_SIZE / 2.0f + directionOffset.x,
                        ant.getPosition().y - Ant.ANT_SIZE / 2.0f + directionOffset.y,
                        Ant.ANT_SIZE,
                        Ant.ANT_SIZE
                );
                spriteBatch.setColor(colonyColor);
            }
        }

    }

}
