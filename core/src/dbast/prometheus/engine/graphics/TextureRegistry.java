package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import dbast.prometheus.engine.scene.AbstractScene;
import dbast.prometheus.engine.scene.FallbackScene;

import java.util.HashMap;
import java.util.Map;

@Deprecated
// TODO migrate this to sprites. Who the fuck would store raw textures??!?!
public class TextureRegistry {
    private static Map<String, Texture> registry;

    private static final String MISSING_REFERENCE = "CORE:MISSING";

    // TODO load and build all sort of sprites here... or custom sprite related classes. I don't give a shit, future me take care of this
    public static void init() {
        registry = new HashMap<>();
        register(MISSING_REFERENCE, new Texture(Gdx.files.internal("missing.png")));
    }

    public static void register(String key, Texture texture) {
        registry.put(key, texture);
    }

    public static Texture get(String key) {
        Texture scene = registry.get(key);
        if (scene == null) {
            scene = registry.get(MISSING_REFERENCE);
        }
        return scene;
    }
}
