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
    private boolean renderRepellentPheromones;

    public MyRenderer(World world){
        colonyRenderers = new ColonyRenderer[World.MAX_COLONY_COUNT];
        worldRenderer = new WorldRenderer(world);

        renderColonies = new boolean[World.MAX_COLONY_COUNT];
        Arrays.fill(renderColonies, true);
        renderAnts = true;
        renderToColonyPheromones = true;
        renderToFoodPheromones = true;
        renderRepellentPheromones = true;

    }

    public void render(SpriteBatch batch, TextureRegion[] textureRegions){
        worldRenderer.render(
                batch, textureRegions,
                renderColonies,
                renderToColonyPheromones, renderToFoodPheromones, renderRepellentPheromones
        );

        for(ColonyRenderer colonyRenderer : colonyRenderers){
            if(colonyRenderer != null) {
                colonyRenderer.render(batch, textureRegions);

                if (renderAnts) {
                    colonyRenderer.renderAnts(batch, textureRegions);
                }
            }
        }

    }

    public void addColony(Colony colony){
        colonyRenderers[colony.getIndex()] = new ColonyRenderer(colony, COLONY_COLORS[colony.getIndex()]);
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

    public void toggleRepellentPheromoneRendering(){
        renderRepellentPheromones = !renderRepellentPheromones;
    }

    public void toggleColonyRendering(int index){
        if(colonyRenderers[index] != null) {
            renderColonies[index] = !renderColonies[index];
        }
    }

}
