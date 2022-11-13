package dbast.prometheus.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import dbast.prometheus.tools.TextureGenerator;

public class ToolRunner extends ApplicationAdapter {

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new ToolRunner(), config);
    }

    @Override
    public void create() {
        super.create();
        generate();
        Gdx.app.exit();
    }

    public void generate() {
        TextureGenerator.drawUVMap(16, 16,Gdx.files.local("tool/uv_map16x16.png"));
        TextureGenerator.drawUVMap(8, 8,Gdx.files.local("tool/uv_map8x8.png"));
       // TextureGenerator.mapTexture(Gdx.files.local("tool/uv_iso_cube.png"), Gdx.files.local("tool/box.png"), Gdx.files.local("tool/test_mapping_result_no_blend.png"), false);
        generateModelsFor(Gdx.files.local("tool/dirt.png"));
        generateModelsFor(Gdx.files.local("tool/sand.png"));
        generateModelsFor(Gdx.files.local("tool/test_mapping.png"));
        generateModelsFor(Gdx.files.local("tool/cobble.png"));
    }

    public void generateModelsFor(FileHandle input) {
        String folder = input.nameWithoutExtension();
        FileHandle output = Gdx.files.local("tool/generated/" + folder);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso_cube.png"), input, output.child("cube.png"), true);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso_cube_inverse.png"), input, output.child("cube_inv.png"), true);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso_stair.png"), input, output.child("stair_east.png"), true);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso.png"), input, output.child("top.png"), true);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso_ramp_n.png"), input, output.child("ramp_north.png"), true);
        TextureGenerator.mapTexture(Gdx.files.local("tool/models/uv_iso_ramp_e.png"), input, output.child("ramp_east.png"), true);
    }
}
