package dbast.prometheus;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.scene.AbstractScene;
import dbast.prometheus.engine.scene.MainMenuScene;

public class PrometheusGame extends ApplicationAdapter {

	public int windowWidth;
	public int windowHeight;

	public AbstractScene activeScene = new MainMenuScene();

	@Override
	public void create () {
		activeScene.create();
	}

	@Override
	public void render () {
		windowWidth = Gdx.graphics.getWidth();
		windowHeight = Gdx.graphics.getHeight();
		float aspect = (float)windowWidth/windowHeight;
		activeScene.update(windowWidth, windowHeight);

		activeScene.render(windowWidth, windowHeight, aspect);
	}

	@Override
	public void dispose () {
	}
}
