package dbast.prometheus.engine.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GUI extends Stage {

    public Skin skin;

    public GUI() {
        super(new ScreenViewport()); //ScreenViewport
        this.skin = new Skin(Gdx.files.internal("ui/dark-hdpi/Holo-dark-hdpi.json"));
    }
}
