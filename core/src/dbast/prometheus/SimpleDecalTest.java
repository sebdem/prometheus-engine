package dbast.prometheus;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class SimpleDecalTest extends InputAdapter implements ApplicationListener {
    private static final int NUM_DECALS = 3;
    DecalBatch batch;
    Array<Decal> decals = new Array<Decal>();
    PerspectiveCamera camera;
    DecalTestController controller;
    FPSLogger logger = new FPSLogger();

    public void create () {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1;
        camera.far = 300;
        camera.position.set(0, 0, 5);
        controller = new DecalTestController(camera);

        Gdx.input.setInputProcessor(controller);
        batch = new DecalBatch(new CameraGroupStrategy(camera));

        TextureRegion[] textures = {new TextureRegion(new Texture(Gdx.files.internal("entity/debug.png"))),
                new TextureRegion(new Texture(Gdx.files.internal("missing.png"))),
                new TextureRegion(new Texture(Gdx.files.internal("badlogic.jpg")))};

        Decal decal = Decal.newDecal(1, 1, textures[1]);
        decal.setPosition(0, 0, 0);
        decals.add(decal);

        decal = Decal.newDecal(1, 1, textures[0], true);
        decal.setPosition(0.5f, 0.5f, 1);
        decals.add(decal);

        decal = Decal.newDecal(1, 1, textures[0], true);
        decal.setPosition(1, 1, -1);
        decals.add(decal);

        decal = Decal.newDecal(1, 1, textures[2]);
        decal.setPosition(1.5f, 1.5f, -2);
        decals.add(decal);

        decal = Decal.newDecal(1, 1, textures[1]);
        decal.setPosition(2, 2, -1.5f);
        decals.add(decal);
    }

    @Override
    public void resize(int i, int i1) {

    }

    Vector3 dir = new Vector3();
    private boolean billboard = true;

    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        camera.update();
        for (int i = 0; i < decals.size; i++) {
            Decal decal = decals.get(i);
            if (billboard) {
                // billboarding for ortho cam :)
// dir.set(-camera.direction.x, -camera.direction.y, -camera.direction.z);
// decal.setRotation(dir, Vector3.Y);

                // billboarding for perspective cam
                decal.lookAt(camera.position, camera.up);
            }
            batch.add(decal);
        }
        batch.flush();
        logger.log();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose () {
        batch.dispose();
    }
}