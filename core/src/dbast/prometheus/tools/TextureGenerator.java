package dbast.prometheus.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;

public class TextureGenerator {

    final static Color baseColor = Color.valueOf("0000FF");


    public static void drawUVMap(int width, int height, FileHandle result) {
        Pixmap textureUVMap = new Pixmap(width,height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < textureUVMap.getHeight(); y++) {
            for (int x = 0; x < textureUVMap.getWidth(); x++) {
                textureUVMap.setColor(baseColor.cpy().add(
                        (float)x/width,
                        (float)y/height,
                        -((y+x)*0.5f)/((width+height)*0.5f),
                        0f
                ));
                textureUVMap.drawPixel(x,y);
            }
        }
        PixmapIO.writePNG(result, textureUVMap);
        textureUVMap.dispose();
    }

    public static void mapTexture(FileHandle template, FileHandle input, FileHandle output, boolean blendUncertain) {
        TextureData templateTexture = new Texture(template).getTextureData();
        TextureData inputTexture = new Texture(input).getTextureData();

        if (!templateTexture.isPrepared()) {
            templateTexture.prepare();
        }
        if (!inputTexture.isPrepared()) {
            inputTexture.prepare();
        }
        Pixmap templateMap = templateTexture.consumePixmap();
        Pixmap inputMap = inputTexture.consumePixmap();
        Pixmap resultMap = new Pixmap(templateMap.getWidth(),templateMap.getHeight(), Pixmap.Format.RGBA8888);

        for (int y = 0; y < templateMap.getHeight(); y++) {
            for (int x = 0; x < templateMap.getWidth(); x++) {
                Color templateXY = new Color(templateMap.getPixel(x, y));
                if (templateXY.a > 0) {
                    // get corresponding pixel data from input
                    Color inputColor = null;
                    Gdx.app.getApplicationLogger().log("TEMPLATE", String.format("RGB at %s/%s is:    %s, %s, %s (%s)", x, y, templateXY.r, templateXY.g, templateXY.b, templateXY.toString()));

                    // blue value can be ignored.
                    float mapX = templateXY.r * inputMap.getWidth();
                    float mapY = templateXY.g * inputMap.getHeight();

                    blendUncertain = false;
                    if (blendUncertain) {
                        int minX = (int)Math.floor(mapX);
                        int minY = (int)Math.floor(mapY);
                        int maxX = (int)Math.floor(mapX);
                        int maxY = (int)Math.floor(mapY);

                        float blendValue = 0.5f * (
                                (   (templateXY.r * inputMap.getWidth()) - minX     ) +
                                (   (templateXY.g * inputMap.getHeight()) - minY    )
                        );

                        inputColor = new Color(inputMap.getPixel(minX, minY)).lerp(new Color(inputMap.getPixel(maxX, maxY)), blendValue);

                    } else {
                        // get corresponding pixel data from input
                     //   inputColor = new Color(inputMap.getPixel(Math.round(mapX), Math.round(mapY)));#


                        inputColor = new Color(inputMap.getPixel(
                                (int) (mapX < 0.5f * inputMap.getWidth() ? Math.max(Math.floor(mapX), 0) : Math.min(Math.ceil(mapX), inputMap.getWidth() -1f)),
                                (int) (mapY < 0.5f * inputMap.getHeight() ? Math.max(Math.floor(mapY), 0) : Math.min(Math.ceil(mapY), inputMap.getHeight() -1f))
                        ));
                    }
                    Gdx.app.getApplicationLogger().log("INPUT", String.format("RGB at %s/%s is:    %s, %s, %s (%s)", mapX, mapY, inputColor.r, inputColor.g, inputColor.b, inputColor.toString()));


                    inputColor.mul(templateXY.a,templateXY.a, templateXY.a, 1f);
                    resultMap.setColor(inputColor);
                    resultMap.drawPixel(x,y);
                }

            }
        }

        PixmapIO.writePNG(output, resultMap);
        templateMap.dispose();
        inputMap.dispose();
        resultMap.dispose();
    }
}
