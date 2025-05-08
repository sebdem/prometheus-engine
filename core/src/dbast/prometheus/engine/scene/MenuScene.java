package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleByAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import dbast.prometheus.PrometheusGame;
import dbast.prometheus.engine.graphics.Resolution;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuScene extends AbstractScene {


    public Music bgmMusic;
    public MenuScene(String key) {
        super(key);
    }

    @Override
    public MenuScene create() {
        super.create();


        // ==== [ load resources ] ============================
        Texture menuBg43 = new Texture(Gdx.files.internal("ui/menu_bg_43.png"));
        Texture menuBg169 = new Texture(Gdx.files.internal("ui/menu_bg_169.png"));
        Texture logoTexture = new Texture(Gdx.files.internal("resources/native/icons/flame128.png"));

        bgmMusic = Gdx.audio.newMusic(Gdx.files.local("resources/sounds/background.wav"));
        bgmMusic.setLooping(true);

        // ==== [ set background ] ============================
        Resolution resolution = Resolution.getCurrentGDX();
        Texture backgroundTargetTexture;
        switch (resolution) {
            case ULTRAWIDE:
            case SIXTEEN_BY_NINE:
                backgroundTargetTexture = menuBg169;
                break;
            default:
                backgroundTargetTexture = menuBg43;
        }
        Gdx.app.log("Menu Scene", String.format("Using resolution %s", resolution));
        Image backgroundImage = new Image(backgroundTargetTexture);

        backgroundImage.addAction(new Action() {
            @Override
            public boolean act(float v) {
                Texture backgroundTargetTexture;
                Resolution resolution = Resolution.getCurrentGDX();
                switch (resolution) {
                    case ULTRAWIDE:
                    case SIXTEEN_BY_NINE:
                        backgroundTargetTexture = menuBg169;
                        break;
                    default:
                        backgroundTargetTexture = menuBg43;
                }
                backgroundImage.setDrawable(new TextureRegionDrawable(new TextureRegion(backgroundTargetTexture)));
                backgroundImage.invalidate();
                backgroundImage.needsLayout();
                return false;
            }
        });

        this.gui.addActor(backgroundImage);
        //backgroundImage.setFillParent(true);
        backgroundImage.setScaling(Scaling.stretch);
        backgroundImage.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // ==== [ build menu ] ============================
        Map<String, String> navigationPoints = new LinkedHashMap<>();
        Map<String, Button> navigationButtons = new HashMap<>();
        navigationPoints.put("start_game", "Start Game");
        navigationPoints.put("enter_options", "Options");
        navigationPoints.put("close_game", "Close Game");


        VerticalGroup menuLayout = new VerticalGroup();
        menuLayout.align(Align.topLeft);
        menuLayout.columnAlign(Align.center);
        menuLayout.fill();
        menuLayout.setPosition(Gdx.graphics.getWidth() / 3f, Gdx.graphics.getHeight() / 2f);
        this.gui.addActor(menuLayout);


        HorizontalGroup menuHeader = new HorizontalGroup();
        menuHeader.align(Align.left);
        menuLayout.addActor(menuHeader);

        Image logoImage = new Image(logoTexture);
        Label logoLabel = new Label("Campfire Chronicles", gui.skin);
        logoLabel.setScale(10f);
        logoLabel.setColor(Color.ORANGE);
        menuHeader.addActor(logoImage);
        menuHeader.addActor(logoLabel);

        VerticalGroup menuList = new VerticalGroup();
        // menuList.align(Align.center);
        // menuList.columnAlign(Align.center);
        //  menuList.fill();
        menuList.grow();
        //  menuList.setPosition(Gdx.graphics.getWidth() / 3f, Gdx.graphics.getHeight() - 20f);
        menuLayout.addActor(menuList);


        navigationPoints.forEach((key, buttonLabel) -> {
            Button newButton = new Button(new Label(buttonLabel, gui.skin), gui.skin);
            newButton.setName(key);
            menuList.addActor(newButton);
            navigationButtons.put(key, newButton);
        });


        if (navigationButtons.containsKey("start_game")) {
            navigationButtons.get("start_game").addAction(new Action() {
                @Override
                public boolean act(float delta) {
                    if (((Button) this.actor).isPressed()) {
                        SceneRegistry.switchActiveScene("OVER_WORLD");
                    }
                    return false;
                }
            });
        }

        if (navigationButtons.containsKey("close_game")) {
            navigationButtons.get("close_game").addAction(new Action() {
                @Override
                public boolean act(float delta) {
                    if (((Button) this.actor).isPressed()) {
                        Gdx.app.exit();
                    }
                    return false;
                }
            });
        }

        Gdx.app.log("Menu Layout", String.format("ML heigth: %s", menuLayout.getHeight()));
        //menuLayout.setPosition(Gdx.graphics.getWidth() / 3f,);
        // menuLayout.align(Align.center);
        return this;
    }

    @Override
    public void activateScene() {
        super.activateScene();
       // bgmMusic.play();
    }
}
