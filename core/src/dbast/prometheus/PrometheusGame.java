package dbast.prometheus;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.scene.*;

public class PrometheusGame extends ApplicationAdapter {

	public int windowWidth;
	public int windowHeight;

	public AbstractScene activeScene = null;

	@Override
	public void create () {
		SceneRegistry.register(
				new FallbackScene("Fallback_2_revenge_of_the_fallback"),
				new DefaultScene("Test_default"),
				new WorldScene("LEVEL_DEBUG")
		);
		activeScene = SceneRegistry.get("LEVEL_DEBUG");
	}

	@Override
	public void render () {
		windowWidth = Gdx.graphics.getWidth();
		windowHeight = Gdx.graphics.getHeight();
		float aspect = (float)windowWidth/windowHeight;
		activeScene.update();

		activeScene.render(windowWidth, windowHeight, aspect);
	}

	@Override
	public void dispose () {

	}
}
