package dbast.prometheus;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.scene.*;

public class PrometheusGame extends ApplicationAdapter {

	/**
	 * TODO Global:
	 * Basic:
	 * - Refactor Game and Scene classes. Maybe implement https://github.com/crykn/libgdx-screenmanager ?
	 * Game Tech:
	 * - Maps "streamed" from a folder/file
	 * 		- may need a refactoring of the file system
	 * Graphical Fluff:
	 * - Refactor Rendering to be based on Decals (in a 3D environment instead of Sprites
	 */

	public int windowWidth;
	public int windowHeight;

	public AbstractScene activeScene = null;

	@Override
	public void create () {
		SceneRegistry.register(
				new FallbackScene("Fallback_2_revenge_of_the_fallback"),
				new DefaultScene("Test_default"),
				new WorldScene("OVER_WORLD")
		);

		activeScene = SceneRegistry.get("OVER_WORLD");
	}

	@Override
	public void render () {
		windowWidth = Gdx.graphics.getWidth();
		windowHeight = Gdx.graphics.getHeight();
		float aspect = (float)windowWidth/windowHeight;
		activeScene.update(Gdx.graphics.getDeltaTime());

		activeScene.render(windowWidth, windowHeight, aspect);
	}

	@Override
	public void dispose () {

	}
}
