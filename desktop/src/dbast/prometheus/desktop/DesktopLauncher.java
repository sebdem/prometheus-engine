package dbast.prometheus.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import dbast.prometheus.PrometheusGame;
import dbast.prometheus.SimpleDecalTest;
import dbast.prometheus.engine.config.PrometheusConfig;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		List<String> arguments = Arrays.asList(arg);
		PrometheusConfig.init();
		PrometheusConfig.conf.put("gridSnapping", true);
		PrometheusConfig.conf.put("isometric", true);
		//PrometheusConfig.conf.put("renderDistance", 32f);
		PrometheusConfig.conf.put("renderDistance", 10f);
		//PrometheusConfig.conf.put("gridSnapIncrement", 0.0625f); //1 pixel
		PrometheusConfig.conf.put("gridSnapIncrement", 0.125f);	//2 pixel
		//PrometheusConfig.conf.put("gridSnapIncrement", 0.1875f); //3 pixel - a bit snappy
		//PrometheusConfig.conf.put("gridSnapIncrement", 0.25f);	//4 pixel
		//PrometheusConfig.conf.put("gridSnapIncrement", 0.5f); //8 pixel - a bit snappy
		//PrometheusConfig.conf.put("gridSnapIncrement", 1f); //16 pixel - a bit snappy

		PrometheusConfig.conf.put("useWorldTimeShading", false);

		if (PrometheusConfig.conf.get("isometric", Boolean.class)) {
			PrometheusConfig.conf.put("baseSpriteSize", 32f);
		} else {
			PrometheusConfig.conf.put("baseSpriteSize", 16f);
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("resources/native/icons/flame128.png", Files.FileType.Internal);
		config.addIcon("resources/native/icons/flame32.png", Files.FileType.Internal);
		config.addIcon("resources/native/icons/flame16.png", Files.FileType.Internal);

		// hdready
		config.height = 720;
		config.width = 1280;
		config.resizable = true;

		if (arguments.contains("fullscreen")) {
			config.fullscreen = true;

			config.height= 2160;
			config.width= 3840;
		} else {

			final Map<String, Vector2> resConfig = new HashMap<>();
			resConfig.put("nds", new Vector2(256, 192));
			resConfig.put("3ds", new Vector2(320, 240));
			resConfig.put("hd720", new Vector2(1280, 720));
			resConfig.put("hd900", new Vector2(1600, 900));
			resConfig.put("hd1080", new Vector2(1920, 1080));
			resConfig.put("ultraWide", new Vector2(	2560, 1080));
			resConfig.put("2k", new Vector2(2560, 1440));
			resConfig.put("4k", new Vector2(3840, 2160));
			resConfig.put("8k", new Vector2(7680, 4320));

			boolean mobile = arguments.contains("mobile");

			Vector2 resolution = resConfig.get(arguments.stream().filter(resConfig::containsKey).findFirst().orElse("hd900"));

			config.height = (int) (mobile ? resolution.x  : resolution.y);
			config.width = (int) (mobile ? resolution.y  : resolution.x);

			config.title = "It's " + LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +" mah dudes"; // TODO slap a name on this motherf
		}

		new LwjglApplication(new PrometheusGame(), config);

	}
}
