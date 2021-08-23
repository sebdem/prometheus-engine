package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import dbast.prometheus.engine.base.IKeyed;
import dbast.prometheus.engine.gui.GUI;

public abstract class AbstractScene implements IKeyed {

    protected String key;

    public String getKey() { return this.key; }

    protected Color background;
    protected GUI gui;
    private Label fpsCounter;

    public AbstractScene(String key) {
        this.key = key;
        this.background = Color.BLACK;
    }

    public AbstractScene create(){
        this.gui = new GUI();

        fpsCounter = new Label("0 FPS", gui.skin);
        fpsCounter.setColor(Color.GREEN);

        this.gui.addActor(fpsCounter);
        Label sceneName = new Label(getKey(), gui.skin);
        sceneName.setPosition(Gdx.graphics.getWidth() - sceneName.getWidth(),Gdx.graphics.getHeight() - sceneName.getHeight());
        this.gui.addActor(sceneName);
        return this;
    }

    public void render(int windowWidth, int windowHeight, float aspect) {
        preRender(windowWidth, windowHeight, aspect);
        mainRender(windowWidth, windowHeight, aspect);
        afterRender(windowWidth, windowHeight, aspect);
    }

    public void preRender(int windowWidth, int windowHeight, float aspect) {
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
    }
    public void mainRender(int windowWidth, int windowHeight, float aspect) {
        fpsCounter.setText(Gdx.graphics.getFramesPerSecond() + " FPS");
        this.gui.draw();
    }

    public void afterRender(int windowWidth, int windowHeight, float aspect) {

    }

    /**
     * Load all larger assets here, when the game switches to this scene
     */
    public void activateScene() { }

    public void update(float deltaTime){
        this.gui.act(deltaTime);
    }

    public void dispose() {
        this.gui.dispose();
    }
}
