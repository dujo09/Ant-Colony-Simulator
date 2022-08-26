package com.dujo.antcolonysimulator.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dujo.antcolonysimulator.ant.Pheromone;
import com.dujo.antcolonysimulator.world.World;
import com.dujo.antcolonysimulator.world.WorldCell;

public class WorldRenderer {
    private final World world;

    WorldRenderer(World world){
        this.world = world;
    }

    void render(SpriteBatch batch, TextureRegion[] textureRegions,
                boolean[] renderColonies,
                boolean renderToColonyPheromones, boolean renderToFoodPheromones, boolean renderRepellentPheromones) {
        for (int i = 0; i < World.COLUMN_COUNT * World.ROW_COUNT; ++i) {
            WorldCell[] cells = world.getCells();

            if (!cells[i].isEmpty()) {
                // Draw wall if on cell
                if (cells[i].isWall()) {
                    batch.draw(
                            textureRegions[7],
                            cells[i].getColumn() * World.CELL_SIZE,
                            cells[i].getRow() * World.CELL_SIZE,
                            World.CELL_SIZE,
                            World.CELL_SIZE
                    );
                }

                // Draw food if on cell
                if (cells[i].isFoodOnCell()) {
                    batch.setColor(1f, 1f, 1f, cells[i].getFoodOnCell() / World.MAX_FOOD_ON_CELL);
                    batch.draw(
                            textureRegions[6],
                            cells[i].getColumn() * World.CELL_SIZE,
                            cells[i].getRow() * World.CELL_SIZE,
                            World.CELL_SIZE,
                            World.CELL_SIZE
                    );
                }

                // Draw to colony pheromones if not disabled by the user
                if (renderToColonyPheromones) {
                    float intensity = 0f;
                    for (int j = 0; j < World.MAX_COLONY_COUNT; ++j) {
                        if (renderColonies[j]) {
                            intensity += cells[i].getPheromoneOnCell(Pheromone.TO_COLONY, j);
                        }
                    }
                    batch.setColor(1f, 1f, 1f, intensity / World.MAX_PHEROMONE_INTENSITY);
                    batch.draw(
                            textureRegions[3],
                            cells[i].getColumn() * World.CELL_SIZE,
                            cells[i].getRow() * World.CELL_SIZE,
                            World.CELL_SIZE,
                            World.CELL_SIZE
                    );
                }

                // Draw to food pheromones if not disabled by the user
                if (renderToFoodPheromones) {
                    float intensity = 0f;
                    for (int j = 0; j < World.MAX_COLONY_COUNT; ++j) {
                        if (renderColonies[j]) {
                            intensity += cells[i].getPheromoneOnCell(Pheromone.TO_FOOD, j);
                        }
                    }
                    batch.setColor(1f, 1f, 1f, intensity / World.MAX_PHEROMONE_INTENSITY);
                    batch.draw(
                            textureRegions[4],
                            cells[i].getColumn() * World.CELL_SIZE,
                            cells[i].getRow() * World.CELL_SIZE,
                            World.CELL_SIZE,
                            World.CELL_SIZE
                    );
                }

                // Draw to repellent pheromones if not disabled by the user
                if (renderRepellentPheromones) {
                    float intensity = 0f;
                    for (int j = 0; j < World.MAX_COLONY_COUNT; ++j) {
                        if (renderColonies[j]) {
                            intensity += cells[i].getPheromoneOnCell(Pheromone.REPELLENT, j);
                        }
                    }
                    batch.setColor(1f, 1f, 1f, intensity / World.MAX_PHEROMONE_INTENSITY);
                    batch.draw(
                            textureRegions[5],
                            cells[i].getColumn() * World.CELL_SIZE,
                            cells[i].getRow() * World.CELL_SIZE,
                            World.CELL_SIZE,
                            World.CELL_SIZE
                    );
                }

                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
    }

}
