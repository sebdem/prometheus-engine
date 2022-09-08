package dbast.prometheus.engine.serializing;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;

public abstract class AbstractLoader <T> {

    public abstract T build();

}
