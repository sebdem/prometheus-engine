package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class AbstractScene {

    protected BitmapFont font;
    protected Color background = Color.BLACK;

    protected Stage mainStage;
    protected Stage uiStage;

    protected Skin uiSkinDefault;
    private Label fpsCounter;

    public void create(){
        this.background = Color.valueOf("#1a1a1a");

        this.font = new BitmapFont();
        this.font.setColor(Color.valueOf("#CECECE"));
        this.uiSkinDefault = new Skin(Gdx.files.internal("ui/dark-ldpi/Holo-dark-ldpi.json"));


        //mainStage = new Stage(FitViewport.);

        this.mainStage = new Stage();
        this.uiStage = new Stage(new ScreenViewport());
        //this.mainStage.se tDebugAll(true);
        //this.uiStage.setDebugAll(true);

        Gdx.input.setInputProcessor(mainStage);
        final Stage uiStageFinal = this.uiStage;
        this.mainStage.addListener(new InputListener(){
            @Override
            public boolean keyDown (InputEvent event, int keycode) {
                Gdx.app.log("Image ClickListener", "keyDown. keycode=" + keycode);
                if (keycode == Input.Keys.A) {
                    uiStageFinal.setDebugAll(!uiStageFinal.isDebugAll());
                    Gdx.app.getInput().vibrate(3000);
                }
                return false;
            }
        });

        fpsCounter = new Label("0", this.uiSkinDefault);
        this.uiStage.addActor(fpsCounter);
    }

    public void render(int windowWidth, int windowHeight, float aspect){
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        this.fpsCounter.setText(Gdx.graphics.getFramesPerSecond() + " FPS");
        this.mainStage.draw();
        this.uiStage.draw();

       //  this.font.draw(this.uiStage.getBatch(), "" + System.currentTimeMillis() + ", " + Gdx.graphics.getFramesPerSecond() + "FPS", 0, 16);
    }

    public void update(int windowWidth, int windowHeight){
        this.mainStage.act(Gdx.graphics.getDeltaTime());
        this.uiStage.act(Gdx.graphics.getDeltaTime());
    }

    public abstract void dispose();
}
