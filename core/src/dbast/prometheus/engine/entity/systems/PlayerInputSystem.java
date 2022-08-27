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
        float velocityZ = 0;

        float baseSpeed = 0.125f;
        float sprint = 2f;

        if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocityY += baseSpeed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocityY -= baseSpeed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += baseSpeed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= baseSpeed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            velocityZ += baseSpeed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            velocityZ -= baseSpeed;
        }

        //
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            velocityX *= sprint;
            velocityY *= sprint;
            velocityZ *= sprint;
        }

       /* if (velocityX != 0 & velocityY != 0) {
            velocityX /= 1.41421356237;
            velocityY /= 1.41421356237;
        }
*/
        for(Entity entity : entities) {
            //Gdx.app.getApplicationLogger().log("PIS", String.format("Entity %s update for PIS System at %s", entity.getId(), updateDelta));
            PositionComponent position = entity.getComponent(PositionComponent.class);
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

            velocityX /= updateDelta;
            velocityY /= updateDelta;
            velocityZ /= updateDelta;

            velocity.setVelocity_x(velocityX);
            velocity.setVelocity_y(velocityY);
            velocity.setVelocity_z(velocityZ);
            // reduce diagonal speed. one does not simply walk ~1.5 times their regular speed when walking diagonally
            //velocity.normalize();
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, InputControllerComponent.class);
    }
}
