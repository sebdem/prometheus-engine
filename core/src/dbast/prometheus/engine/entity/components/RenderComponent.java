package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

import javax.swing.plaf.nimbus.State;
import java.util.HashMap;
import java.util.Map;

// TODO what from here can be delegated to a seperate Animator/RenderSystem?
public class RenderComponent extends Component {
    protected Map<String, Animation<TextureRegion>> animations;
    protected TextureRegion defaultTexture;

    public RenderComponent() {
        this.defaultTexture = new TextureRegion( new Texture(Gdx.files.internal("missing.png")));
        this.animations = new HashMap<>();
    }

    public TextureRegion getTexture(float stateTime) {
        StateComponent entityState = null;
        float stateAge = 0f;
        // Component is also currently assigned to Tile instances
        if (entity != null) {
            entityState = entity.getComponent(StateComponent.class);
        }

        Animation<TextureRegion> currentAnimation = null;
        if (entityState != null) {
            stateAge = entityState.getCurrentAge();
            currentAnimation = animations.getOrDefault(entityState.getState(), null);
        }
        if (currentAnimation == null ) {
            currentAnimation = animations.getOrDefault("default", null);
        }

        if (currentAnimation != null) {
            return currentAnimation.getKeyFrame(stateAge);
        }

        return this.defaultTexture;
    }

    // TODO think about Tiles, maybe they can keep this separate call. Somehow they should have the option to
    @Deprecated
    public TextureRegion getTexture(float stateTime, String stateAnimation) {
        Animation<TextureRegion> currentAnimation = animations.getOrDefault(stateAnimation, animations.getOrDefault("default", null));

        if (currentAnimation == null) {
            return this.defaultTexture;
        } else {
            return currentAnimation.getKeyFrame(stateTime);
        }
    }

    // TODO get TextureRegions from mostly one instance of Texture (TextureAtlas?)
    public RenderComponent setDefaultTexture(FileHandle fileHandle) {
        try {
            this.defaultTexture = new TextureRegion(new Texture(fileHandle));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return this;
    }

    public RenderComponent registerAnimation(FileHandle fileHandle, int columns, int rows, float frameDuration, boolean loop, String state)  {
        Texture unsplit = new Texture(fileHandle);
        TextureRegion[][] tmp = TextureRegion.split(unsplit, unsplit.getWidth() / columns, unsplit.getHeight() / rows);

        TextureRegion[] animationFrames = new TextureRegion[columns * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                animationFrames[index++] = tmp[i][j];
            }
        }
        Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, animationFrames);
        if (loop) {
            animation.setPlayMode(Animation.PlayMode.LOOP);
        }

        animations.put(state, animation);
        if (state.equals("default")) {
           this.defaultTexture = animation.getKeyFrames()[0];
        }
        return this;
    };
}
