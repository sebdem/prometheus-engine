
package dbast.prometheus;

import com.badlogic.gdx.ApplicationListener;
        import com.badlogic.gdx.Gdx;
        import com.badlogic.gdx.graphics.Color;
        import com.badlogic.gdx.graphics.GL20;
        import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
        import com.badlogic.gdx.graphics.g3d.Environment;
        import com.badlogic.gdx.graphics.g3d.Model;
        import com.badlogic.gdx.graphics.g3d.ModelBatch;
        import com.badlogic.gdx.graphics.g3d.ModelInstance;
        import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
        import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
        import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dbast.prometheus.utils.TextureUtils;

/**
 * See: http://blog.xoppa.com/basic-3d-using-libgdx-2/
 * @author Xoppa
 */
public class TestThreeDim implements ApplicationListener {
    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public Model model;
    public ModelInstance instance;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 0.4f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(TextureAttribute.createDiffuse(TextureUtils.load("debug.png")));
        material.set(TextureAttribute.createEmissive(TextureUtils.load("debug_spec.png")));
        material.set(TextureAttribute.createNormal(TextureUtils.load("debug_norm.png")));
        model = modelBuilder.createBox(5f, 5f, 5f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        instance = new ModelInstance(model);

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}