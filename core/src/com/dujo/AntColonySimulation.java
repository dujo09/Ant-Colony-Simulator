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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.ant.Pheromone;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.renderer.ColonyRenderer;
import com.dujo.antcolonysimulator.renderer.MyRenderer;
import com.dujo.antcolonysimulator.world.World;

import java.awt.geom.Point2D;

public class AntColonySimulation extends ApplicationAdapter {
	private World world;
	private Colony[] colonies;
	private int colonyCount;
	private MyRenderer renderer;
	private int timeScale;
	private boolean isPaused;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Texture spriteSheet;
	private TextureRegion[] textureRegions;
	private int brushSize;
	private boolean isPlaceMode;
	private boolean isFoodSelected;

	@Override
	public void create(){
		world = new World();
		colonies = new Colony[World.MAX_COLONY_COUNT];

		renderer = new MyRenderer(world);

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

		brushSize = 1;
		isPlaceMode = true;
		isFoodSelected = true;

		timeScale = 1;

	}

	@Override
	public void render () {
		handleInput();
		camera.update();

		ScreenUtils.clear(0f, 0f, 0f, 1f);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		float deltaTime = Gdx.graphics.getDeltaTime();

		deltaTime *= isPaused ? 0f : timeScale;

		world.update(deltaTime);
		for(Colony colony : colonies){
			if(colony != null){
				colony.update(deltaTime);
			}
		}

		renderer.render(batch, textureRegions);

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
			brushSize += 1f;
			brushSize = MathUtils.clamp(brushSize, 1, 20);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.L)){
			brushSize -= 1f;
			brushSize = MathUtils.clamp(brushSize, 1, 20);
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
					world.setFood(touchPosition2D, 100, brushSize);
				}else{
					world.removeFood(touchPosition2D, brushSize);
				}
			}else {
				if(isPlaceMode){
					world.setWall(touchPosition2D, brushSize);
				}else{
					world.removeWall(touchPosition2D, brushSize);
				}
			}
		}
		if(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)){
			if(colonyCount < World.MAX_COLONY_COUNT) {
				Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
				camera.unproject(touchPosition);
				Point2D.Float touchPosition2D = new Point2D.Float(touchPosition.x, touchPosition.y);

				colonies[colonyCount] = new Colony(colonyCount, touchPosition2D, world);
				renderer.addColony(colonies[colonyCount]);
				++colonyCount;
			}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.H)){
			renderer.toggleColonyRendering(0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.J)){
			renderer.toggleColonyRendering(1);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.K)){
			renderer.toggleColonyRendering(2);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.C)){
			renderer.toggleToColonyPheromoneRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.B)){
			renderer.toggleToFoodPheromoneRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.N)){
			renderer.toggleRepellentPheromoneRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
			renderer.toggleAntRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			isPaused = !isPaused;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)){
			timeScale = 1;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)){
			timeScale = 2;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)){
			timeScale = 3;
		}

	}
	
	@Override
	public void dispose () {
		spriteSheet.dispose();
		batch.dispose();
	}

}
