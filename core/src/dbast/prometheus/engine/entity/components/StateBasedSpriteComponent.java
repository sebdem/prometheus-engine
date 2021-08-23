package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.HashMap;
import java.util.Map;

// TODO refactor this. to texture atlas maybe?
public class StateBasedSpriteComponent extends SpriteComponent {

    protected Map<String, Sprite> spriteMap;
    protected StateComponent dependsOn;

    public StateBasedSpriteComponent(Sprite defaultSprite) {
        super(defaultSprite);
        spriteMap = new HashMap<>();
    }

    public static StateBasedSpriteComponent fromFile(FileHandle internal) {
        return new StateBasedSpriteComponent(new Sprite(new Texture(internal)));
    }

    public StateBasedSpriteComponent bindTo(StateComponent stateComponent) {
        this.dependsOn = stateComponent;
        return this;
    }

    public StateBasedSpriteComponent addState(String state, FileHandle internal) {
        this.spriteMap.put(state, new Sprite(new Texture(internal)));
        return this;
    }

    @Override
    public Sprite getSprite() {
        if (dependsOn != null) {
            return spriteMap.getOrDefault(dependsOn.getState(), this.defaultSprite);
        }
        return defaultSprite;
    }
}

