package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

// TODO what from here can be delegated to a seperate Animator/RenderSystem?
public class RenderComponent extends Component {
    protected Map<String, Animation<TextureRegion>> animations;
    protected TextureRegion defaultTexture;
    public String state;
    public float stateAge;

    public RenderComponent() {
        this.defaultTexture = new TextureRegion( new Texture(Gdx.files.internal("missing.png")));
        this.animations = new HashMap<>();
        this.state = "default";
    }

    public TextureRegion getTexture(float stateTime) {
        return this.getTexture(stateTime, this.state);
    }

    public TextureRegion getTexture(float stateTime, String stateAnimation) {
        Animation<TextureRegion> currentAnimation = animations.getOrDefault(stateAnimation, animations.getOrDefault("default", null));

        stateAge = stateTime;
        if (currentAnimation == null) {
            this.state = "default";
            return this.defaultTexture;
        } else {
            return currentAnimation.getKeyFrame(stateAge);
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
