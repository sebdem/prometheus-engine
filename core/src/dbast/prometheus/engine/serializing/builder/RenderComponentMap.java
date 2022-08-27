package dbast.prometheus.engine.serializing.builder;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.entity.components.RenderComponent;

import java.util.LinkedHashMap;

public class RenderComponentMap extends LinkedHashMap<String, RenderComponentData> {

    public RenderComponent build() {
        RenderComponent component = new RenderComponent();
        this.forEach((state,data) -> {
            component.registerAnimation(
                    Gdx.files.local(data.path),
                    data.columns,
                    data.rows,
                    data.frameDuration,
                    data.loop,
                    state
            );
        });
        return component;
    }
}
