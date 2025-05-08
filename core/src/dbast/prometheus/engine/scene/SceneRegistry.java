package dbast.prometheus.engine.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SceneRegistry {
    private static final Map<String, AbstractScene> registry = new HashMap<>();

    private static final Stack<AbstractScene> activeScenes = new Stack<>();

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

    public static AbstractScene getActiveScene() {
        return activeScenes.peek();
    }

    public static void layerActiveScene(String key) {
        if (!activeScenes.isEmpty()) {
            AbstractScene current = activeScenes.peek();
        }

        addNewScene(key);
    }
    public static void switchActiveScene(String key) {
        if (!activeScenes.isEmpty()) {
            AbstractScene current = activeScenes.pop();
            current.dispose();
        }

        addNewScene(key);
    }

    private static void addNewScene(String key) {
        AbstractScene newScene = registry.get(key);
        if (!activeScenes.contains(newScene)) {
            activeScenes.push(newScene);
            newScene.activateScene();
        }
    }
}
