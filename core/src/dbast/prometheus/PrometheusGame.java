package dbast.prometheus;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.scene.*;

import java.util.Stack;

public class PrometheusGame extends ApplicationAdapter {

	/**
	 * TODO Global:
	 * Basic:
	 * - Refactor Game and Scene classes. Maybe implement https://github.com/crykn/libgdx-screenmanager ?
	 *
	 * - Refactor and organize Scene Management ?
	 * 		De-Static the SceneRegistry.
	 * 		Proper management methods in registry and Scene Interface to activate, layer on top, switch to, deactivate, initialize and dispose scenes for greater control
	 *
	 * Game Tech:
	 * - Maps "streamed" from a folder/file
	 * 		- may need a refactoring of the file system
	 * Graphical Fluff:
	 * - Refactor Rendering to be based on Decals (in a 3D environment instead of Sprites
	 */

	public int windowWidth;
	public int windowHeight;


	@Override
	public void create () {
		SceneRegistry.register(
				new FallbackScene("Fallback_2_revenge_of_the_fallback"),
				new DefaultScene("Test_default"),
				new WorldScene("OVER_WORLD"),
				new MenuScene("MENU_MAIN")
		);

		//activeScene = SceneRegistry.get("OVER_WORLD");
		SceneRegistry.switchActiveScene("MENU_MAIN");
	}

	@Override
	public void render () {
		windowWidth = Gdx.graphics.getWidth();
		windowHeight = Gdx.graphics.getHeight();
		float aspect = (float)windowWidth/windowHeight;
		AbstractScene activeScene = SceneRegistry.getActiveScene();

		activeScene.update(Gdx.graphics.getDeltaTime());
		activeScene.render(windowWidth, windowHeight, aspect);
	}

	@Override
	public void dispose () {
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
