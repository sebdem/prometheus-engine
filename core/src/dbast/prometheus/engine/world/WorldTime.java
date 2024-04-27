package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.MathUtils;

public class WorldTime {

    protected Pixmap skyboxLighting;
    protected Pixmap environmentLighting;
    protected Pixmap sightRange;

    public int dayDuration;
    public float speedOfTime;

    public WorldTime() {
        this.speedOfTime = 8f;
        this.dayDuration  = 24;

        this.skyboxLighting = new Pixmap(Gdx.files.local("data/environment/overworld/skyline.png"));
        this.environmentLighting = new Pixmap(Gdx.files.local("data/environment/overworld/lighting.png"));
        this.sightRange = new Pixmap(Gdx.files.local("data/environment/overworld/visibility.png"));
    }


    public Color getSkyboxColor(float worldAge) {
       /* float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;
        // without lerping for now...
        int pixelColor = skyboxLighting.getPixel((int)(this.skyboxLighting.getWidth() * (timeOfDay / dayDuration)),0);
        Color target = new Color(pixelColor);
        target.a = 1f;
        return target;*/
        Color target = getForProgress(skyboxLighting, worldAge);
        target.a = 1f;
        return target;
    }


    public Color getLightingColor(float worldAge) {
        /*float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;
        // without lerping for now...
        int pixelColor = environmentLighting.getPixel((int)(this.environmentLighting.getWidth() * (timeOfDay / dayDuration)),0);
        Color target = new Color(pixelColor);*/
        Color target = getForProgress(environmentLighting, worldAge);
        target.a = 1f;
        return target;
    }

    public float getSightRange(float worldAge) {
        return (getForProgress(sightRange, worldAge).r * 100f) + 3;
    }

    public float getTimeOfDay(float worldAge) {
        return ((worldAge / 60) * speedOfTime) % dayDuration;
    }

    protected Color getForProgress(Pixmap targetPixmap, float worldAge) {
        float pixelProgress = targetPixmap.getWidth() * getDayProgress(worldAge);
        int min = MathUtils.floor(pixelProgress);
        int max = MathUtils.ceil(pixelProgress);
        if (max >= targetPixmap.getWidth()) {
            max = 0;
        }
        Color minTarget = new Color(targetPixmap.getPixel(min,0));
        Color maxTarget = new Color(targetPixmap.getPixel(max,0));

        return minTarget.lerp(maxTarget, pixelProgress - min);
    }

    public float getDayProgress(float atAge) {
        return (getTimeOfDay(atAge) / dayDuration);
    }
}
