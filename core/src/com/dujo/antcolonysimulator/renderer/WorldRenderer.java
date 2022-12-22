package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dujo.antcolonysimulator.ant.AntPheromone;
import com.dujo.antcolonysimulator.world.World;
import com.dujo.antcolonysimulator.world.WorldCell;
import com.dujo.AntColonySimulation.TEXTURE_INDICES;

public class WorldRenderer {
    private final World world;

    WorldRenderer(World world){
        this.world = world;
    }

    void render(SpriteBatch spriteBatch, TextureRegion[] textureRegions,
                boolean[] renderColonies,
                boolean renderToColonyPheromones, boolean renderToFoodPheromones) {
        WorldCell[] cells = world.getCells();
        for (int i = 0; i < world.getColumnCount() * world.getRowCount(); ++i) {
            spriteBatch.setColor(1f, 1f, 1f, 1f);

            if (cells[i].isWall()) {
                spriteBatch.draw(
                        textureRegions[TEXTURE_INDICES.WALL_TEXTURE_INDEX.ordinal()],
                        cells[i].getColumn() * world.getCellSize(),
                        cells[i].getRow() * world.getCellSize(),
                        world.getCellSize(),
                        world.getCellSize()
                );
                continue;
            }

            if (cells[i].isFoodOnCell()) {
                spriteBatch.setColor(1f, 1f, 1f, cells[i].getFoodOnCell() / World.MAX_FOOD_ON_CELL);
                spriteBatch.draw(
                        textureRegions[TEXTURE_INDICES.FOOD_TEXTURE_INDEX.ordinal()],
                        cells[i].getColumn() * world.getCellSize(),
                        cells[i].getRow() * world.getCellSize(),
                        world.getCellSize(),
                        world.getCellSize()
                );
                continue;
            }

            if (renderToColonyPheromones) {
                // Get the total intensity of this pheromone (from all colonies)
                float intensity = 0f;
                for (int j = 0; j < World.MAX_COLONY_COUNT; ++j)
                    if (renderColonies[j])
                        intensity += cells[i].getPheromoneOnCell(AntPheromone.TO_COLONY, j);

                spriteBatch.setColor(1f, 1f, 1f, intensity / World.MAX_PHEROMONE_INTENSITY);
                spriteBatch.draw(
                        textureRegions[TEXTURE_INDICES.TO_COLONY_PHEROMONE_TEXTURE_INDEX.ordinal()],
                        cells[i].getColumn() * world.getCellSize(),
                        cells[i].getRow() * world.getCellSize(),
                        world.getCellSize(),
                        world.getCellSize()
                );
            }

            if (renderToFoodPheromones) {
                // Get the total intensity of this pheromone (from all colonies)
                float intensity = 0f;
                for (int j = 0; j < World.MAX_COLONY_COUNT; ++j)
                    if (renderColonies[j])
                        intensity += cells[i].getPheromoneOnCell(AntPheromone.TO_FOOD, j);

                spriteBatch.setColor(1f, 1f, 1f, intensity / World.MAX_PHEROMONE_INTENSITY);
                spriteBatch.draw(
                        textureRegions[TEXTURE_INDICES.TO_FOOD_PHEROMONE_TEXTURE_INDEX.ordinal()],
                        cells[i].getColumn() * world.getCellSize(),
                        cells[i].getRow() * world.getCellSize(),
                        world.getCellSize(),
                        world.getCellSize()
                );
            }

            // Always draw repellent pheromones because they don't obscure anything
            float intensity = 0f;
            for (int j = 0; j < World.MAX_COLONY_COUNT; ++j)
                if (renderColonies[j])
                    intensity += cells[i].getPheromoneOnCell(AntPheromone.REPELLENT, j);

            spriteBatch.setColor(1f, 1f, 1f, intensity / World.MAX_REPELLENT_INTENSITY);
            spriteBatch.draw(
                    textureRegions[TEXTURE_INDICES.REPELLENT_PHEROMONE_TEXTURE_INDEX.ordinal()],
                    cells[i].getColumn() * world.getCellSize(),
                    cells[i].getRow() * world.getCellSize(),
                    world.getCellSize(),
                    world.getCellSize()
            );
        }
    }

}
