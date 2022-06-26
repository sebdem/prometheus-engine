package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;

import java.util.Arrays;
import java.util.List;

public class PlayerInputSystem extends ComponentSystem {

    @Override
    public void execute(float updateDelta, List<Entity> entities) {
        InputProcessor inputProcessor = Gdx.input.getInputProcessor();
        float velocityX = 0;
        float velocityY = 0;

        float inc = 4f;
        float sprint = 2f;

        if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocityY += inc;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocityY -= inc;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += inc;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= inc;
        }

        //
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            velocityX *= sprint;
            velocityY *= sprint;
        }

        // reduce diagonal speed. one does not simply walk ~1.5 times their regular speed when walking diagonally
        if (velocityX != 0 & velocityY != 0) {
            velocityX /= 1.41421356237;
            velocityY /= 1.41421356237;
        }

        for(Entity entity : entities) {
            //Gdx.app.getApplicationLogger().log("PIS", String.format("Entity %s update for PIS System at %s", entity.getId(), updateDelta));
            PositionComponent position = entity.getComponent(PositionComponent.class);
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
            velocity.setVelocity_x(velocityX);
            velocity.setVelocity_y(velocityY);
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, InputControllerComponent.class);
    }
}
