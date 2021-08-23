package dbast.prometheus.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dbast.prometheus.PrometheusGame;
import dbast.prometheus.TestThreeDim;

import java.awt.*;
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

		config.resizable = false;
		if (arguments.contains("fullscreen")) {
			config.fullscreen = true;

			config.height= 2160;
			config.width= 3840;
		} else {
			int setting = 4;
			boolean mobile = false;
			if (setting == 1) {
				// DS
				config.height = 192;
				config.width= 256;
			}
			if (setting == 2) {
				// 3DS
				config.height = 240;
				config.width = 320;
			}
			if (setting == 3) {
				// hdready
				config.height = 720;
				config.width = 1280;
			}
			if (setting == 4) {
				// more hd
				config.height = 900;
				config.width = 1600;
			}
			if (setting == 5) {
				// 2k baby
				config.height = 1440;
				config.width = 2560;
			}
			if (setting == 6) {
				// 4k omegapogchamp
				config.height = 2160;
				config.width = 3840;
			}
			if (mobile) {
				int width = config.width;
				config.width = config.height;
				config.height = width;
			}
			config.title = "It's " + LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +" mah dudes"; // TODO slap a name on this motherf
		}

		new LwjglApplication(new PrometheusGame(), config);

	}
}
