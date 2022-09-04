package com.dujo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.renderer.MyRenderer;
import com.dujo.antcolonysimulator.world.World;

import java.awt.*;
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
		loadWorldFromImage();
		colonies = new Colony[World.MAX_COLONY_COUNT];

		renderer = new MyRenderer(world);

		spriteSheet = new Texture(Gdx.files.internal("spriteSheet.png"));
		textureRegions = new TextureRegion[8];
		for(int i = 0; i < 8; ++i){
			textureRegions[i] = new TextureRegion(spriteSheet, 64 * i, 0, 64, 64);
		}
		batch = new SpriteBatch();

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(200, 200 * (h / w));
		camera.position.set(50f, 50f, 0f);
		camera.update();

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
			brushSize = MathUtils.clamp(brushSize, 1, 50);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.L)){
			brushSize -= 1f;
			brushSize = MathUtils.clamp(brushSize, 1, 50);
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.Y) || Gdx.input.isKeyJustPressed(Input.Keys.Z)){
			renderer.toggleToColonyPheromoneRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
			renderer.toggleToFoodPheromoneRendering();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.C)){
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

	private void loadWorldFromImage(){
		final Color WALL_COLOR = new Color(161, 161, 161);
		final Color FOOD_COLOR = new Color(13, 255, 0);

		Pixmap worldImage = new Pixmap(Gdx.files.internal("worldMap300x300.png"));

		int widthPixmap = worldImage.getWidth();
		int heightPixmap = worldImage.getHeight();
		world = new World(widthPixmap, heightPixmap, 1);

		for(int y=0; y < heightPixmap; y++){
			for(int x=0; x < widthPixmap; x++){
				int colorData = worldImage.getPixel(x, y);
				int red = colorData >>> 24;
				int green = (colorData & 0xFF0000) >>> 16;
				int blue = (colorData & 0xFF00) >>> 8;
				int alpha = colorData & 0xFF;

				Color color = new Color(red, green, blue);
				if(color.equals(WALL_COLOR)){
					world.getCell(heightPixmap - 1 - y, x).setWall(true);
				}else if(color.equals(FOOD_COLOR)){
					world.getCell(heightPixmap - 1 - y, x).setFoodOnCell(100);
				}
			}
		}

		worldImage.dispose();
	}
	
	@Override
	public void dispose () {
		spriteSheet.dispose();
		batch.dispose();
	}

}
