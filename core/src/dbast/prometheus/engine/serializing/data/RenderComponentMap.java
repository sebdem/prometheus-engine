package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.components.RenderComponent;

import java.util.LinkedHashMap;

public class RenderComponentMap extends LinkedHashMap<String, RenderComponentData> {

    public RenderComponent build() {
        RenderComponent component = new RenderComponent();
        this.forEach((state,data) -> {
            component.registerAnimation(
                    Gdx.files.local(data.path),
                    (data.normal != null) ? Gdx.files.local(data.normal) : null,
                    new Vector3(data.offset[0], data.offset[1], data.offset[2]),
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
