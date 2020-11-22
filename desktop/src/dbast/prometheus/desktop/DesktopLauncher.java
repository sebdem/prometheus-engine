package dbast.prometheus.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import dbast.prometheus.PrometheusGame;
import dbast.prometheus.TestThreeDim;

import java.util.Arrays;
import java.util.List;

public class DesktopLauncher {
	public static void main (String[] arg) {
		List<String> arguments = Arrays.asList(arg);

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("misc/flame128.png", Files.FileType.Internal);
		config.addIcon("misc/flame32.png", Files.FileType.Internal);
		config.addIcon("misc/flame16.png", Files.FileType.Internal);

		if (arguments.contains("fullscreen")) {
			config.fullscreen = true;
			config.height= 2160;
			config.width= 3840;
		}

		new LwjglApplication(new PrometheusGame(), config);

	}
}
