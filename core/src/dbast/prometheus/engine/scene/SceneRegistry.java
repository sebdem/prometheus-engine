package dbast.prometheus.engine.scene;

import java.util.HashMap;
import java.util.Map;

public class SceneRegistry {
    private static final Map<String, AbstractScene> registry = new HashMap<>();

    public static void register(AbstractScene... newScene) {
        for(AbstractScene scene : newScene) {
            registry.put(scene.getKey(), scene.create());
        }
    }

    public static AbstractScene get(String sceneName) {
        AbstractScene scene = registry.get(sceneName);
        if (scene == null) {
            scene = FallbackScene.DEFAULT;
        }
        return scene;
    }
}
