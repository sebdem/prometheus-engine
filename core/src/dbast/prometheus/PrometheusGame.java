package dbast.prometheus;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dbast.prometheus.engine.scene.AbstractScene;
import dbast.prometheus.engine.scene.DefaultScene;
import dbast.prometheus.engine.scene.MainMenuScene;
import dbast.prometheus.utils.TextureUtils;

public class PrometheusGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	public int windowWidth;
	public int windowHeight;

	public AbstractScene activeScene = new DefaultScene();


	public Stage stage;

	@Override
	public void create () {
		batch = new SpriteBatch();
		activeScene.create();
		stage = new Stage();
	}

	@Override
	public void render () {
		windowWidth = Gdx.graphics.getWidth();
		windowHeight = Gdx.graphics.getHeight();
		float aspect = (float)windowWidth/windowHeight;

		batch.begin();
		activeScene.render(batch, windowWidth, windowHeight, aspect);
		batch.end();
		stage.draw();

		activeScene.update(windowWidth, windowHeight);
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
