package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.graphics.g2d.Sprite;

@Deprecated
public class SpriteBoundSizeComponent extends SizeComponent{

    protected float nativeUnit;

    public SpriteBoundSizeComponent(float nativeUnit) {
        super(1f, 1f);
        this.nativeUnit = nativeUnit;
    }

    protected Sprite boundSprite() {
        return this.entity.getComponent(SpriteComponent.class).defaultSprite;
    }

    @Override
    public float getHeight() {
        return boundSprite().getRegionHeight() / nativeUnit;
    }

    @Override
    public float getWidth() {
        return boundSprite().getRegionWidth() / nativeUnit;
    }
}
