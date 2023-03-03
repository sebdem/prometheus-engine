package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * This baby is supposed to render {@link PrometheusSprite}s. The difference to regular SpriteBatch/Sprites/Textures is
 * that PrometheusSprites can contain additional render data (height maping, light information, height maps etc.) as
 * opposed to being a simple 2D texture.
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class PrometheusSpriteBatch extends SpriteBatch {
    /** @deprecated */
    @Deprecated
    public static Mesh.VertexDataType defaultVertexDataType;
    private Mesh mesh;
    final float[] vertices;
    int idx;
    Texture lastTexture;
    Texture lastNormal;
    public static Texture defaultNormal = new Texture(Gdx.files.internal("world/terrain/iso/fallback_map.png"));
    float invTexWidth;
    float invTexHeight;
    boolean drawing;
    private final Matrix4 transformMatrix;
    private final Matrix4 projectionMatrix;
    private final Matrix4 combinedMatrix;
    private boolean blendingDisabled;
    private int blendSrcFunc;
    private int blendDstFunc;
    private int blendSrcFuncAlpha;
    private int blendDstFuncAlpha;
    private final ShaderProgram shader;
    private ShaderProgram customShader;
    private boolean ownsShader;
    private final Color color;
    float colorPacked;
    public int renderCalls;
    public int totalRenderCalls;
    public int maxSpritesInBatch;

    public PrometheusSpriteBatch() {
        this(1000, (ShaderProgram)null);
    }

    public PrometheusSpriteBatch(int size) {
        this(size, (ShaderProgram)null);
    }

    public PrometheusSpriteBatch(int size, ShaderProgram defaultShader) {
        this.idx = 0;
        this.lastTexture = null;
        this.lastNormal = defaultNormal;
        this.invTexWidth = 0.0F;
        this.invTexHeight = 0.0F;
        this.drawing = false;
        this.transformMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.blendingDisabled = false;
        this.blendSrcFunc = 770;
        this.blendDstFunc = 771;
        this.blendSrcFuncAlpha = 770;
        this.blendDstFuncAlpha = 771;
        this.customShader = null;
        this.color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
        this.colorPacked = Color.WHITE_FLOAT_BITS;
        this.renderCalls = 0;
        this.totalRenderCalls = 0;
        this.maxSpritesInBatch = 0;
        if (size > 8191) {
            throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);
        } else {
            Mesh.VertexDataType vertexDataType = Gdx.gl30 != null ? VertexDataType.VertexBufferObjectWithVAO : defaultVertexDataType;
            this.mesh = new Mesh(vertexDataType, false, size * 4, size * 6, new VertexAttribute[]{new VertexAttribute(1, 2, "a_position"), new VertexAttribute(4, 4, "a_color"), new VertexAttribute(16, 2, "a_texCoord0")});
            this.projectionMatrix.setToOrtho2D(0.0F, 0.0F, (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
            this.vertices = new float[size * 20];
            int len = size * 6;
            short[] indices = new short[len];
            short j = 0;

            for(int i = 0; i < len; j = (short)(j + 4)) {
                indices[i] = j;
                indices[i + 1] = (short)(j + 1);
                indices[i + 2] = (short)(j + 2);
                indices[i + 3] = (short)(j + 2);
                indices[i + 4] = (short)(j + 3);
                indices[i + 5] = j;
                i += 6;
            }

            this.mesh.setIndices(indices);
            if (defaultShader == null) {
                this.shader = createDefaultShader();
                this.ownsShader = true;
            } else {
                this.shader = defaultShader;
            }

        }
    }

    public static ShaderProgram createDefaultShader() {
        String vertexShader = "attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_color = a_color;\n   v_color.a = v_color.a * (255.0/254.0);\n   v_texCoords = a_texCoord0;\n   gl_Position =  u_projTrans * a_position;\n}\n";
        String fragmentShader = "#ifdef GL_ES\n#define LOWP lowp\nprecision mediump float;\n#else\n#define LOWP \n#endif\nvarying LOWP vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\n  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n}";
        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        } else {
            return shader;
        }
    }

    public void begin() {
        if (this.drawing) {
            throw new IllegalStateException("SpriteBatch.end must be called before begin.");
        } else {
            this.renderCalls = 0;
            Gdx.gl.glDepthMask(false);
            if (this.customShader != null) {
                this.customShader.begin();
            } else {
                this.shader.begin();
            }

            this.setupMatrices();
            this.drawing = true;
        }
    }

    public void end() {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before end.");
        } else {
            if (this.idx > 0) {
                this.flush();
            }

            this.lastTexture = null;
            this.lastNormal = defaultNormal;
            this.drawing = false;
            GL20 gl = Gdx.gl;
            gl.glDepthMask(true);
            if (this.isBlendingEnabled()) {
                gl.glDisable(3042);
            }

            if (this.customShader != null) {
                this.customShader.end();
            } else {
                this.shader.end();
            }

        }
    }

    public void setColor(Color tint) {
        this.color.set(tint);
        this.colorPacked = tint.toFloatBits();
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        this.colorPacked = this.color.toFloatBits();
    }

    public Color getColor() {
        return this.color;
    }

    public void setPackedColor(float packedColor) {
        Color.abgr8888ToColor(this.color, packedColor);
        this.colorPacked = packedColor;
    }

    public float getPackedColor() {
        return this.colorPacked;
    }

    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            float fx2 = width - originX;
            float fy2 = height - originY;
            if (scaleX != 1.0F || scaleY != 1.0F) {
                fx *= scaleX;
                fy *= scaleY;
                fx2 *= scaleX;
                fy2 *= scaleY;
            }

            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            float u;
            float v;
            if (rotation != 0.0F) {
                u = MathUtils.cosDeg(rotation);
                v = MathUtils.sinDeg(rotation);
                x1 = u * fx - v * fy;
                y1 = v * fx + u * fy;
                x2 = u * fx - v * fy2;
                y2 = v * fx + u * fy2;
                x3 = u * fx2 - v * fy2;
                y3 = v * fx2 + u * fy2;
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = fx;
                y1 = fy;
                x2 = fx;
                y2 = fy2;
                x3 = fx2;
                y3 = fy2;
                x4 = fx2;
                y4 = fy;
            }

            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            u = (float)srcX * this.invTexWidth;
            v = (float)(srcY + srcHeight) * this.invTexHeight;
            float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
            float v2 = (float)srcY * this.invTexHeight;
            float color;
            if (flipX) {
                color = u;
                u = u2;
                u2 = color;
            }

            if (flipY) {
                color = v;
                v = v2;
                v2 = color;
            }

            color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x2;
            vertices[idx + 6] = y2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = x3;
            vertices[idx + 11] = y3;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = x4;
            vertices[idx + 16] = y4;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float u = (float)srcX * this.invTexWidth;
            float v = (float)(srcY + srcHeight) * this.invTexHeight;
            float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
            float v2 = (float)srcY * this.invTexHeight;
            float fx2 = x + width;
            float fy2 = y + height;
            float color;
            if (flipX) {
                color = u;
                u = u2;
                u2 = color;
            }

            if (flipY) {
                color = v;
                v = v2;
                v2 = color;
            }

            color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float u = (float)srcX * this.invTexWidth;
            float v = (float)(srcY + srcHeight) * this.invTexHeight;
            float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
            float v2 = (float)srcY * this.invTexHeight;
            float fx2 = x + (float)srcWidth;
            float fy2 = y + (float)srcHeight;
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float fx2 = x + width;
            float fy2 = y + height;
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(Texture texture, float x, float y) {
        this.draw(texture, x, y, (float)texture.getWidth(), (float)texture.getHeight());
    }

    public void draw(Texture texture, float x, float y, float width, float height) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float fx2 = x + width;
            float fy2 = y + height;
            float u = 0.0F;
            float v = 1.0F;
            float u2 = 1.0F;
            float v2 = 0.0F;
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = 0.0F;
            vertices[idx + 4] = 1.0F;
            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = 0.0F;
            vertices[idx + 9] = 0.0F;
            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = 1.0F;
            vertices[idx + 14] = 0.0F;
            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = 1.0F;
            vertices[idx + 19] = 1.0F;
            this.idx = idx + 20;
        }
    }

    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            int verticesLength = this.vertices.length;
            int remainingVertices = verticesLength;
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else {
                remainingVertices = verticesLength - this.idx;
                if (remainingVertices == 0) {
                    this.flush();
                    remainingVertices = verticesLength;
                }
            }

            int copyCount = Math.min(remainingVertices, count);
            System.arraycopy(spriteVertices, offset, this.vertices, this.idx, copyCount);
            this.idx += copyCount;

            for(count -= copyCount; count > 0; count -= copyCount) {
                offset += copyCount;
                this.flush();
                copyCount = Math.min(verticesLength, count);
                System.arraycopy(spriteVertices, offset, this.vertices, 0, copyCount);
                this.idx += copyCount;
            }

        }
    }

    public void draw(TextureRegion region, float x, float y) {
        this.draw(region, x, y, (float)region.getRegionWidth(), (float)region.getRegionHeight());
    }

    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            Texture texture = region.getTexture();
            
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float fx2 = x + width;
            float fy2 = y + height;
            float u = region.getU();
            float v = region.getV2();
            float u2 = region.getU2();
            float v2 = region.getV();
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            Texture texture = region.getTexture();
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            float fx2 = width - originX;
            float fy2 = height - originY;
            if (scaleX != 1.0F || scaleY != 1.0F) {
                fx *= scaleX;
                fy *= scaleY;
                fx2 *= scaleX;
                fy2 *= scaleY;
            }

            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            float u;
            float v;
            if (rotation != 0.0F) {
                u = MathUtils.cosDeg(rotation);
                v = MathUtils.sinDeg(rotation);
                x1 = u * fx - v * fy;
                y1 = v * fx + u * fy;
                x2 = u * fx - v * fy2;
                y2 = v * fx + u * fy2;
                x3 = u * fx2 - v * fy2;
                y3 = v * fx2 + u * fy2;
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = fx;
                y1 = fy;
                x2 = fx;
                y2 = fy2;
                x3 = fx2;
                y3 = fy2;
                x4 = fx2;
                y4 = fy;
            }

            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            u = region.getU();
            v = region.getV2();
            float u2 = region.getU2();
            float v2 = region.getV();
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x2;
            vertices[idx + 6] = y2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = x3;
            vertices[idx + 11] = y3;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = x4;
            vertices[idx + 16] = y4;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            Texture texture = region.getTexture();
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            float fx2 = width - originX;
            float fy2 = height - originY;
            if (scaleX != 1.0F || scaleY != 1.0F) {
                fx *= scaleX;
                fy *= scaleY;
                fx2 *= scaleX;
                fy2 *= scaleY;
            }

            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            float u1;
            float v1;
            if (rotation != 0.0F) {
                u1 = MathUtils.cosDeg(rotation);
                v1 = MathUtils.sinDeg(rotation);
                x1 = u1 * fx - v1 * fy;
                y1 = v1 * fx + u1 * fy;
                x2 = u1 * fx - v1 * fy2;
                y2 = v1 * fx + u1 * fy2;
                x3 = u1 * fx2 - v1 * fy2;
                y3 = v1 * fx2 + u1 * fy2;
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = fx;
                y1 = fy;
                x2 = fx;
                y2 = fy2;
                x3 = fx2;
                y3 = fy2;
                x4 = fx2;
                y4 = fy;
            }

            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            float u2;
            float v2;
            float u3;
            float v3;
            float u4;
            float v4;
            if (clockwise) {
                u1 = region.getU2();
                v1 = region.getV2();
                u2 = region.getU();
                v2 = region.getV2();
                u3 = region.getU();
                v3 = region.getV();
                u4 = region.getU2();
                v4 = region.getV();
            } else {
                u1 = region.getU();
                v1 = region.getV();
                u2 = region.getU2();
                v2 = region.getV();
                u3 = region.getU2();
                v3 = region.getV2();
                u4 = region.getU();
                v4 = region.getV2();
            }

            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u1;
            vertices[idx + 4] = v1;
            vertices[idx + 5] = x2;
            vertices[idx + 6] = y2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u2;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = x3;
            vertices[idx + 11] = y3;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u3;
            vertices[idx + 14] = v3;
            vertices[idx + 15] = x4;
            vertices[idx + 16] = y4;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u4;
            vertices[idx + 19] = v4;
            this.idx = idx + 20;
        }
    }

    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (!this.drawing) {
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        } else {
            float[] vertices = this.vertices;
            Texture texture = region.getTexture();
            if (texture != this.lastTexture) {
                this.switchTexture(texture);
            } else if (this.idx == vertices.length) {
                this.flush();
            }

            float x1 = transform.m02;
            float y1 = transform.m12;
            float x2 = transform.m01 * height + transform.m02;
            float y2 = transform.m11 * height + transform.m12;
            float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
            float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
            float x4 = transform.m00 * width + transform.m02;
            float y4 = transform.m10 * width + transform.m12;
            float u = region.getU();
            float v = region.getV2();
            float u2 = region.getU2();
            float v2 = region.getV();
            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = x2;
            vertices[idx + 6] = y2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;
            vertices[idx + 10] = x3;
            vertices[idx + 11] = y3;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;
            vertices[idx + 15] = x4;
            vertices[idx + 16] = y4;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    public void flush() {
        if (this.idx != 0) {
            ++this.renderCalls;
            ++this.totalRenderCalls;
            int spritesInBatch = this.idx / 20;
            if (spritesInBatch > this.maxSpritesInBatch) {
                this.maxSpritesInBatch = spritesInBatch;
            }

            int count = spritesInBatch * 6;

            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            this.lastTexture.bind();

            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
            this.lastNormal.bind();

            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

            Mesh mesh = this.mesh;
            mesh.setVertices(this.vertices, 0, this.idx);
            mesh.getIndicesBuffer().position(0);
            mesh.getIndicesBuffer().limit(count);
            if (this.blendingDisabled) {
                Gdx.gl.glDisable(3042);
            } else {
                Gdx.gl.glEnable(3042);
                if (this.blendSrcFunc != -1) {
                    Gdx.gl.glBlendFuncSeparate(this.blendSrcFunc, this.blendDstFunc, this.blendSrcFuncAlpha, this.blendDstFuncAlpha);
                }
            }

            mesh.render(this.customShader != null ? this.customShader : this.shader, 4, 0, count);
            this.idx = 0;
        }
    }

    public void disableBlending() {
        if (!this.blendingDisabled) {
            this.flush();
            this.blendingDisabled = true;
        }
    }

    public void enableBlending() {
        if (this.blendingDisabled) {
            this.flush();
            this.blendingDisabled = false;
        }
    }

    public void setBlendFunction(int srcFunc, int dstFunc) {
        this.setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (this.blendSrcFunc != srcFuncColor || this.blendDstFunc != dstFuncColor || this.blendSrcFuncAlpha != srcFuncAlpha || this.blendDstFuncAlpha != dstFuncAlpha) {
            this.flush();
            this.blendSrcFunc = srcFuncColor;
            this.blendDstFunc = dstFuncColor;
            this.blendSrcFuncAlpha = srcFuncAlpha;
            this.blendDstFuncAlpha = dstFuncAlpha;
        }
    }

    public int getBlendSrcFunc() {
        return this.blendSrcFunc;
    }

    public int getBlendDstFunc() {
        return this.blendDstFunc;
    }

    public int getBlendSrcFuncAlpha() {
        return this.blendSrcFuncAlpha;
    }

    public int getBlendDstFuncAlpha() {
        return this.blendDstFuncAlpha;
    }

    public void dispose() {
        this.mesh.dispose();
        if (this.ownsShader && this.shader != null) {
            this.shader.dispose();
        }

    }

    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4 getTransformMatrix() {
        return this.transformMatrix;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (this.drawing) {
            this.flush();
        }

        this.projectionMatrix.set(projection);
        if (this.drawing) {
            this.setupMatrices();
        }

    }

    public void setTransformMatrix(Matrix4 transform) {
        if (this.drawing) {
            this.flush();
        }

        this.transformMatrix.set(transform);
        if (this.drawing) {
            this.setupMatrices();
        }

    }

    private void setupMatrices() {
        this.combinedMatrix.set(this.projectionMatrix).mul(this.transformMatrix);
        if (this.customShader != null) {
            this.customShader.setUniformMatrix("u_projTrans", this.combinedMatrix);
            this.customShader.setUniformi("u_texture", 0);
            this.customShader.setUniformi("u_normal", 1);
        } else {
            this.shader.setUniformMatrix("u_projTrans", this.combinedMatrix);
            this.shader.setUniformi("u_texture", 0);
            this.shader.setUniformi("u_normal", 1);
        }

    }

    public void setNormal(Texture nTexture) {
        if (nTexture != null) {
            this.lastNormal = nTexture;
        } else {
            this.lastNormal = defaultNormal;
        }
    }

    protected void switchTexture(Texture texture) {
        this.flush();
        this.lastTexture = texture;
        this.invTexWidth = 1.0F / (float)texture.getWidth();
        this.invTexHeight = 1.0F / (float)texture.getHeight();
    }

    public void setShader(ShaderProgram shader) {
        if (this.drawing) {
            this.flush();
            if (this.customShader != null) {
                this.customShader.end();
            } else {
                this.shader.end();
            }
        }

        this.customShader = shader;
        if (this.drawing) {
            if (this.customShader != null) {
                this.customShader.begin();
            } else {
                this.shader.begin();
            }

            this.setupMatrices();
        }

    }

    public ShaderProgram getShader() {
        return this.customShader == null ? this.shader : this.customShader;
    }

    public boolean isBlendingEnabled() {
        return !this.blendingDisabled;
    }

    public boolean isDrawing() {
        return this.drawing;
    }

    static {
        defaultVertexDataType = VertexDataType.VertexArray;
    }
}
