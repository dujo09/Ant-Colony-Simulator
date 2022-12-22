package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.world.World;

import java.util.Arrays;

public class MyRenderer {
    public static final Color[] COLONY_COLORS = {
            new Color(1f , 1f, 0f, 1f),
            new Color(1f, 0.5f, 0f, 1f),
            new Color(0.1f, 1f, 0f, 1f),
            new Color(1f, 1f, 1f, 1f)
    };

    private final ColonyRenderer[] colonyRenderers;
    private final WorldRenderer worldRenderer;
    private final boolean[] renderColonies;
    private boolean renderAnts;
    private boolean renderToColonyPheromones;
    private boolean renderToFoodPheromones;

    public MyRenderer(World world){
        colonyRenderers = new ColonyRenderer[World.MAX_COLONY_COUNT];
        worldRenderer = new WorldRenderer(world);

        renderColonies = new boolean[World.MAX_COLONY_COUNT];
        Arrays.fill(renderColonies, true);
        renderAnts = true;
        renderToColonyPheromones = true;
        renderToFoodPheromones = true;
    }

    public void render(SpriteBatch spriteBatch, TextureRegion[] textureRegions){
        worldRenderer.render(
                spriteBatch, textureRegions,
                renderColonies,
                renderToColonyPheromones, renderToFoodPheromones
        );

        for(int i = 0; i < colonyRenderers.length; ++i){
            if(colonyRenderers[i] != null && renderColonies[i]) {
                colonyRenderers[i].render(spriteBatch, textureRegions);

                if (renderAnts)
                    colonyRenderers[i].renderAnts(spriteBatch, textureRegions);
            }
        }

    }

    public void addColony(Colony colony){
        colonyRenderers[colony.getColonyIndex()] = new ColonyRenderer(colony, COLONY_COLORS[colony.getColonyIndex()]);
    }

    public void toggleAntRendering(){
        renderAnts = !renderAnts;
    }

    public void toggleToColonyPheromoneRendering(){
        renderToColonyPheromones = !renderToColonyPheromones;
    }

    public void toggleToFoodPheromoneRendering(){
        renderToFoodPheromones = !renderToFoodPheromones;
    }

    public void toggleColonyRendering(int index){
        if(colonyRenderers[index] != null) {
            renderColonies[index] = !renderColonies[index];
        }
    }

}
