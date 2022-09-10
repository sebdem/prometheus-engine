package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector;

// TODO too much for an enum. Move to seperate object. Maybe make Color transitions based on a texture...
public enum WorldTime {
    NIGHT2(0, 5, Color.valueOf("003355"), Color.valueOf("003355"), 10f),
    SUNRISE(5, 7, Color.valueOf("EE9900"), Color.valueOf("FFDD99"), 30F),
    NOON(7,18, Color.valueOf("0077FF"), Color.WHITE.cpy(), 100f),
    SUNSET(18, 20,  Color.valueOf("AA5700"), Color.WHITE.cpy().mul(0.9f,0.8f, 0.7f, 1), 30F),
    NIGHT1(20, 24, Color.valueOf("003355"), Color.valueOf("003355"), 10f);
    public float from;
    public float to;
    // render properties
    public Color skyboxColor;
    public Color lightingColor;
    public float sightRange;

    WorldTime(float from, float to, Color skyboxColor, Color lightingColor, float sightRange) {
        this.from = from;
        this.to = to;
        this.skyboxColor = skyboxColor;
        this.lightingColor = lightingColor;
        this.sightRange = sightRange;
    }
    public Color getSkyboxColor(float worldAge) {
        float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;

        float timeMiddle = from + ((to - from) * 0.5f);
        WorldTime adjescentTime;

        if (timeOfDay > timeMiddle) {
            adjescentTime = next();
            Color target = this.skyboxColor.cpy().lerp(adjescentTime.skyboxColor.cpy(),  getTimeTransition(timeOfDay));
            target.a = 1f;
            return target;
        } else if (timeOfDay < timeMiddle) {
            adjescentTime = previous();
            Color target = adjescentTime.skyboxColor.cpy().lerp(this.skyboxColor.cpy(), getTimeTransition(timeOfDay));
            target.a = 1f;
            return target;
        } else {
            return this.skyboxColor;
        }
    }

    public Color getLightingColor(float worldAge) {
        float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;
        float timeMiddle = from + ((to - from) * 0.5f);
        WorldTime adjescentTime;

        if (timeOfDay > timeMiddle) {
            adjescentTime = next();
            Color target = this.lightingColor.cpy().lerp(adjescentTime.lightingColor.cpy(),  getTimeTransition(timeOfDay));
            target.a = 1f;
            return target;
        } else if (timeOfDay < timeMiddle) {
            adjescentTime = previous();
            Color target = adjescentTime.lightingColor.cpy().lerp(this.lightingColor.cpy(), getTimeTransition(timeOfDay));
            target.a = 1f;
            return target;
        } else {
            return this.skyboxColor;
        }
    }


    public float getSightRange(float worldAge) {
        float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;

        float timeMiddle = from + ((to - from) * 0.5f);
        WorldTime adjescentTime;
        float minTime = 0f;
        float maxTime = 1f;
        if (timeOfDay > timeMiddle) {
            adjescentTime = this.next();
            return this.sightRange + (getTimeTransition(timeOfDay) * (adjescentTime.sightRange - this.sightRange));
        } else if (timeOfDay < timeMiddle) {
            adjescentTime = this.previous();
            return adjescentTime.sightRange + (getTimeTransition(timeOfDay) * (this.sightRange - adjescentTime.sightRange));
        } else {
            return this.sightRange;
        }
    }

    protected float getTimeTransition(float timeOfDay) {
        float timeMiddle = from + ((to - from) * 0.5f);
        WorldTime adjescentTime;
        float minTime = 0f;
        float maxTime = 1f;
        if (timeOfDay > timeMiddle) {
            adjescentTime = this.next();
            minTime = timeMiddle;
            maxTime = adjescentTime.from + ((adjescentTime.to - adjescentTime.from) * 0.5f);
        } else if (timeOfDay < timeMiddle) {
            adjescentTime = this.previous();
            minTime = adjescentTime.from + ((adjescentTime.to - adjescentTime.from) * 0.5f);
            maxTime = timeMiddle;
        } else {
            return 0;
        }
        return (timeOfDay - minTime) / (maxTime - minTime);
    }

    public WorldTime next() {
        WorldTime nextTime;
        switch (this) {
            case NIGHT2: nextTime = SUNRISE; break;
            case SUNRISE: nextTime = NOON; break;
            case NOON: nextTime = SUNSET; break;
            case SUNSET: nextTime = NIGHT1; break;
            case NIGHT1: nextTime = NIGHT2; break;
            default: nextTime = this;
        };
        return nextTime;
    }

    public WorldTime previous() {
        WorldTime nextTime;
        switch (this) {
            case NIGHT2: nextTime = NIGHT1; break;
            case SUNRISE: nextTime = NIGHT2; break;
            case NOON: nextTime = SUNRISE; break;
            case SUNSET: nextTime = NOON; break;
            case NIGHT1: nextTime = SUNSET; break;
            default: nextTime = this;
        };
        return nextTime;
    }

    final static int dayDuration = 24;
    final static float speedOfTime = 1f;

    public static WorldTime get(long realTime) {
        return NIGHT2;
    }
    public static WorldTime get(float worldAge) {
        float timeOfDay = ((worldAge / 60) * speedOfTime) % dayDuration;
        if (timeOfDay > SUNRISE.from && timeOfDay <= SUNRISE.to) {
            return SUNRISE;
        }
        if (timeOfDay > NOON.from && timeOfDay <= NOON.to) {
            return NOON;
        }
        if (timeOfDay > SUNSET.from && timeOfDay <= SUNSET.to) {
            return SUNSET;
        }
        if (timeOfDay > NIGHT2.from && timeOfDay <= NIGHT2.to) {
            return NIGHT2;
        }
        if (timeOfDay > NIGHT1.from && timeOfDay <= NIGHT1.to) {
            return NIGHT1;
        }
        return NOON;
    }


}
