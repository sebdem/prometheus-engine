package dbast.prometheus.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dbast.prometheus.PrometheusGame;
import dbast.prometheus.TestThreeDim;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
		} else {
			// DS
			config.height = 192;
			config.width= 256;
			// 3DS
			config.height = 240;
			config.width = 320;
			config.height = 720;
			config.width = 1280;
			config.title = "It's " + LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +" mah dudes"; // TODO slap a name on this motherf
		}

		new LwjglApplication(new PrometheusGame(), config);

	}
}
