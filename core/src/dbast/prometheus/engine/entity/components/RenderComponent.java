package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.graphics.SpriteRenderData;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

import javax.swing.plaf.nimbus.State;
import javax.xml.soap.Text;
import java.util.HashMap;
import java.util.Map;

import static dbast.prometheus.engine.LockOnCamera.useIsometric;

// TODO what from here can be delegated to a seperate Animator/RenderSystem?
public class RenderComponent extends Component {
    protected Map<String, Animation<SpriteRenderData>> animations;
    protected SpriteRenderData defaultTexture;

    // In world offset
    public Vector3 offset = Vector3.Zero;

    public RenderComponent() {
        this.defaultTexture = new SpriteRenderData(new TextureRegion( new Texture(Gdx.files.internal("missing.png"))), null);
        this.animations = new HashMap<>();
    }

    public SpriteRenderData getDefaultRenderData() {
        return this.defaultTexture;
    }
    public Map<String, Animation<SpriteRenderData>> getAnimations() {
        return this.animations;
    }


/*
    // TODO get TextureRegions from mostly one instance of Texture (TextureAtlas?)
    public RenderComponent setDefaultTexture(FileHandle fileHandle) {
        try {
            this.defaultTexture = new TextureRegion(new Texture(fileHandle));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return this;
    }
*/

    public RenderComponent registerAnimation(FileHandle fileHandle, String state)  {
        return this.registerAnimation(fileHandle, null, null, 1, 1, 1, true, state);
    }
    public RenderComponent registerAnimation(FileHandle fileHandle, int columns, int rows, float frameDuration, boolean loop, String state)  {
        return this.registerAnimation(fileHandle, null, null, columns, rows, frameDuration, loop, state);
    }
    public RenderComponent registerAnimation(FileHandle fileHandle, FileHandle normalHandle, Vector3 renderOffset, int columns, int rows, float frameDuration, boolean loop, String state)  {
        Texture unsplit = new Texture(fileHandle);
        TextureRegion[][] tmp = TextureRegion.split(unsplit, unsplit.getWidth() / columns, unsplit.getHeight() / rows);

        Gdx.app.getApplicationLogger().log("AnimationRegistry", String.format("Normal of path %s is present: %s", fileHandle.name(), (normalHandle != null)));
        Texture unsplitNormal = (normalHandle != null) ? new Texture(normalHandle) : null;
        TextureRegion[][] tmpNormal = (normalHandle != null) ? TextureRegion.split(unsplitNormal, unsplitNormal.getWidth() / columns, unsplitNormal.getHeight() / rows) : null;

        SpriteRenderData[] animationFrames = new SpriteRenderData[columns * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                animationFrames[index++] = new SpriteRenderData(
                        tmp[i][j],
                        (normalHandle != null) ? tmpNormal[i][j] : null,
                        renderOffset
                );
            }
        }
        Animation<SpriteRenderData> animation = new Animation<>(frameDuration, animationFrames);
        if (loop) {
            animation.setPlayMode(Animation.PlayMode.LOOP);
        }

        animations.put(state, animation);
        if (state.equals("default")) {
           this.defaultTexture = animation.getKeyFrames()[0];
        }
        return this;
    };

    public static RenderComponent playerRenderComponent() {
        if (useIsometric) {
            return new RenderComponent()
                    .registerAnimation(Gdx.files.internal("sprites/player/player_idle.png"), 8, 1, 1.25f, true, "default")
                    // axis-directional
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up_left.png"), 8, 1, 0.125f, true, "moving:up")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down_right.png"), 8, 1, 0.125f, true, "moving:down")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down_left.png"), 8, 1, 0.125f, true, "moving:left")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up_right.png"), 8, 1, 0.125f, true, "moving:right")
                    // diagonal
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_left.png"), 8, 1, 0.125f, true, "moving:upleft")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up.png"), 8, 1, 0.125f, true, "moving:upright")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down.png"), 8, 1, 0.125f, true, "moving:downleft")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_right.png"), 8, 1, 0.125f, true, "moving:downright")
            ;
        } else {
            return new RenderComponent()
                    .registerAnimation(Gdx.files.internal("sprites/player/player_idle.png"), 8, 1, 1.25f, true, "default")
                    // axis-directional
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up.png"), 8, 1, 0.125f, true, "moving:up")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down.png"), 8, 1, 0.125f, true, "moving:down")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_left.png"), 8, 1, 0.125f, true, "moving:left")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_right.png"), 8, 1, 0.125f, true, "moving:right")
                    // diagonal
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up_left.png"), 8, 1, 0.125f, true, "moving:upleft")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_up_right.png"), 8, 1, 0.125f, true, "moving:upright")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down_left.png"), 8, 1, 0.125f, true, "moving:downleft")
                    .registerAnimation(Gdx.files.internal("sprites/player/player_moving_down_right.png"), 8, 1, 0.125f, true, "moving:downright")
            ;
        }
    }
}
