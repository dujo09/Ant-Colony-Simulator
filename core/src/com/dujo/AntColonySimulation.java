package com.dujo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.world.World;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AntColonySimulation extends ApplicationAdapter {
	public static final Color[] COLONY_COLORS = {
			new Color(1f , 1f, 0f, 1f),
			new Color(1f, 0.5f, 0f, 1f),
			new Color(0.1f, 1f, 0f, 1f),
			new Color(1f, 1f, 1f, 1f)
	};

	private World world;
	private Colony[] colonies;
	private int colonyCount;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Texture spriteSheet;
	private TextureRegion[] textureRegions;
	private DrawSettings drawSettings;
	private boolean isPlaceMode;
	private boolean isFoodSelected;

	@Override
	public void create(){
		world = new World();
		colonies = new Colony[World.MAX_COLONY_COUNT];

		spriteSheet = new Texture("spritesheet.png");
		textureRegions = new TextureRegion[8];
		for(int i = 0; i < 8; ++i){
			textureRegions[i] = new TextureRegion(spriteSheet, 64 * i, 0, 64, 64);
		}

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(200, 200 * (h / w));
		camera.position.set(50f, 50f, 0f);
		camera.update();

		batch = new SpriteBatch();

		drawSettings = new DrawSettings();
		isPlaceMode = true;
		isFoodSelected = true;

	}

	@Override
	public void render () {
		handleInput();
		camera.update();

		ScreenUtils.clear(0f, 0f, 0f, 1f);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		float deltaTime = Gdx.graphics.getDeltaTime();

		world.updateAndDraw(
				deltaTime, batch,
				drawSettings.drawToColonyPheromone, drawSettings.drawToFoodPheromone, drawSettings.drawRepellent,
				drawSettings.coloniesToDraw, textureRegions
		);

		for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
			if(colonies[i] != null){
				colonies[i].updateAndDraw(
						deltaTime, batch,
						drawSettings.drawAnts, drawSettings.coloniesToDraw[i],textureRegions, COLONY_COLORS[i]
				);
			}
		}

		batch.end();

		}

	private void handleInput(){
		if(Gdx.input.isKeyPressed(Input.Keys.A)){
			camera.translate(-3f, 0f, 0f);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)){
			camera.translate(3f, 0f, 0f);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)){
			camera.translate(0f, -3f, 0f);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)){
			camera.translate(0f, 3f, 0f);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.Q)){
			camera.zoom -= 0.03;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.E)){
			camera.zoom += 0.03;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
			drawSettings.brushSize += 10f;
			drawSettings.brushSize = MathUtils.clamp(drawSettings.brushSize, 5f, 100f);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.L)){
			drawSettings.brushSize -= 10f;
			drawSettings.brushSize = MathUtils.clamp(drawSettings.brushSize, 5f, 100f);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
			isFoodSelected = true;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.V)){
			isFoodSelected = false;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
			isPlaceMode = false;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
			isPlaceMode = true;
		}
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
			camera.unproject(touchPosition);
			Point2D.Float touchPosition2D = new Point2D.Float(touchPosition.x, touchPosition.y);

			if(isFoodSelected){
				if(isPlaceMode){
					world.setFood(touchPosition2D, 100, drawSettings.brushSize);
				}else{
					world.removeFood(touchPosition2D, drawSettings.brushSize);
				}
			}else {
				if(isPlaceMode){
					world.setWall(touchPosition2D, drawSettings.brushSize);
				}else{
					world.removeWall(touchPosition2D, drawSettings.brushSize);
				}
			}
		}
		if(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)){
			if(colonyCount < World.MAX_COLONY_COUNT) {
				Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
				camera.unproject(touchPosition);
				Point2D.Float touchPosition2D = new Point2D.Float(touchPosition.x, touchPosition.y);

				colonies[colonyCount] = new Colony(colonyCount, touchPosition2D, world);
				++colonyCount;
			}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && colonyCount >= 1){
			drawSettings.coloniesToDraw[0] = !drawSettings.coloniesToDraw[0];
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && colonyCount >= 2){
			drawSettings.coloniesToDraw[1] = !drawSettings.coloniesToDraw[1];
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && colonyCount >= 3){
			drawSettings.coloniesToDraw[2] = !drawSettings.coloniesToDraw[2];
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)){
			drawSettings.drawToColonyPheromone = !drawSettings.drawToColonyPheromone;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)){
			drawSettings.drawToFoodPheromone = !drawSettings.drawToFoodPheromone;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)){
			drawSettings.drawRepellent = !drawSettings.drawRepellent;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)){
			drawSettings.drawAnts = !drawSettings.drawAnts;
		}

	}
	
	@Override
	public void dispose () {
		spriteSheet.dispose();
		batch.dispose();
	}

	private static class DrawSettings{
		float brushSize;
		boolean[] coloniesToDraw;
		boolean drawToColonyPheromone;
		boolean drawToFoodPheromone;
		boolean drawRepellent;
		boolean drawAnts;

		DrawSettings(){
			brushSize = 5f;
			coloniesToDraw = new boolean[World.MAX_COLONY_COUNT];
			for(int i = 0; i < World.MAX_COLONY_COUNT; ++i){
				coloniesToDraw[i] = true;
			}

			drawToColonyPheromone = true;
			drawToFoodPheromone = true;
			drawRepellent = true;
			drawAnts = true;
		}
	}
}
