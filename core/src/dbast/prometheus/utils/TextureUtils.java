package dbast.prometheus.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureUtils {

    public static Texture load(String origin) {
        return new Texture(new Pixmap(Gdx.files.internal(origin)));
    }

    @Deprecated
    public static Texture tint(String origin, Color color) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(origin));
        pixmap.setBlending(Pixmap.Blending.SourceOver);

        pixmap.setColor(new Color(color.r, color.g, color.b, 0.15f));
        for (int i = 1; i < 16; i++) {
            pixmap.drawCircle(pixmap.getWidth()/2, pixmap.getHeight()/2, pixmap.getHeight()/2 - i);

        }
        Texture tinted = new Texture(pixmap);

        pixmap.dispose();
        return tinted;
    }
}
