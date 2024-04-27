package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PrometheusSprite extends Sprite {
    public SpriteType type = SpriteType.DEFAULT;
    // In world offset
    public Vector3 renderIndexOffset = Vector3.Zero;

    public Texture normal;

    public PrometheusSprite(TextureRegion currentTextureRegion) {
        super(currentTextureRegion);
    }
    public PrometheusSprite(TextureRegion diffuseTexture, TextureRegion normalTexture) {
        super(diffuseTexture);
        this.setNormal(normalTexture.getTexture());
    }

    public PrometheusSprite(SpriteRenderData renderData) {
        super(renderData.diffuse);
        this.setNormal(renderData.normal);
        this.setRenderIndexOffset(renderData.renderIndexOffset);
    }
    /* // TODO improve update of render data
    public void updateRenderData(SpriteRenderData renderData) {
        this.setRegion(renderData.diffuse);
        this.setNormal(renderData.normal);
        this.setRenderIndexOffset(renderData.renderIndexOffset);
    }*/

    public void setType(SpriteType type) {
        this.type = type;
    }
    public void setRenderIndexOffset(Vector3 renderIndexOffset) {
        this.renderIndexOffset = renderIndexOffset;
    }

    public Texture getNormal() {
        return this.normal;
    }

    public void setNormal(Texture normal) {
        this.normal = normal;
    }
    public void setNormal(TextureRegion normal) {
        this.normal = normal != null ? normal.getTexture() : null;
    }

    public void draw(PrometheusSpriteBatch batch) {
        batch.draw(super.getTexture(), this.getNormal(), this.getVertices(), 0, 20);
    }

    public void draw(PrometheusSpriteBatch batch, float alphaModulation) {
        float oldAlpha = this.getColor().a;
        this.setAlpha(oldAlpha * alphaModulation);
        this.draw(batch);
        this.setAlpha(oldAlpha);
    }

    public void draw(Batch batch) {
        throw new NotImplementedException();
    }

    /*
    public Texture emissive; // TODO, because it would be funny
    public Texture heightmap; // needed?
    */

}
