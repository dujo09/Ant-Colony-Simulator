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
import com.dujo.antcolonysimulator.ant.Ant;
import com.dujo.antcolonysimulator.colony.Colony;
import com.dujo.antcolonysimulator.renderer.MyRenderer;
import com.dujo.antcolonysimulator.world.World;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AntColonySimulation extends ApplicationAdapter {
	public enum TEXTURE_INDICES {
		ANT_TEXTURE_INDEX,
		HOLDING_FOOD_TEXTURE_INDEX,
		COLONY_TEXTURE_INDEX,
		TO_COLONY_PHEROMONE_TEXTURE_INDEX,
		TO_FOOD_PHEROMONE_TEXTURE_INDEX,
		REPELLENT_PHEROMONE_TEXTURE_INDEX,
		FOOD_TEXTURE_INDEX,
		WALL_TEXTURE_INDEX
	}


	private World world;
	private List<Colony> colonies;
	private MyRenderer renderer;
	private int timeScale;
	private boolean isPaused;
	private SpriteBatch spriteBatch;
	private OrthographicCamera camera;
	private Texture spriteSheet;
	private TextureRegion[] textureRegions;
	private int brushSize;
	private boolean isPlaceMode;
	private boolean isFoodSelected;


	@Override
	public void create(){
		loadWorldFromImage();
		colonies = new ArrayList<>();

		renderer = new MyRenderer(world);

		// Load textures
		spriteSheet = new Texture(Gdx.files.internal("spriteSheet.png"));
		textureRegions = new TextureRegion[8];
		for(TEXTURE_INDICES textureIndex : TEXTURE_INDICES.values()){
			textureRegions[textureIndex.ordinal()] = new TextureRegion(spriteSheet, 64 * textureIndex.ordinal(), 0, 64, 64);
		}
		spriteBatch = new SpriteBatch();

		// Setup camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(200, 200 * (h / w));
		camera.position.set(50f, 50f, 0f);
		camera.update();

		// Setup misc
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

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		float deltaTime = Gdx.graphics.getDeltaTime();

		deltaTime *= isPaused ? 0f : timeScale;

		world.update(deltaTime);
		for(Colony colony : colonies)
			colony.update(deltaTime);

		renderer.render(spriteBatch, textureRegions);

		spriteBatch.end();

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
			if(colonies.size() < World.MAX_COLONY_COUNT) {
				Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
				camera.unproject(touchPosition);
				Point2D.Float touchPosition2D = new Point2D.Float(touchPosition.x, touchPosition.y);

				Colony newColony = new Colony(colonies.size(), touchPosition2D, world);
				colonies.add(newColony);
				renderer.addColony(newColony);
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

		for(int y=0; y < heightPixmap; y++)
			for(int x=0; x < widthPixmap; x++){
				int colorData = worldImage.getPixel(x, y);
				int red = colorData >>> 24;
				int green = (colorData & 0xFF0000) >>> 16;
				int blue = (colorData & 0xFF00) >>> 8;
				int alpha = colorData & 0xFF;

				Color color = new Color(red, green, blue);
				if(color.equals(WALL_COLOR))
					world.getCell(heightPixmap - 1 - y, x).setWall(true);
				else if(color.equals(FOOD_COLOR))
					world.getCell(heightPixmap - 1 - y, x).setFoodOnCell(20);
			}
		
		worldImage.dispose();
	}

	@Override
	public void dispose () {
		spriteSheet.dispose();
		spriteBatch.dispose();
	}

}
