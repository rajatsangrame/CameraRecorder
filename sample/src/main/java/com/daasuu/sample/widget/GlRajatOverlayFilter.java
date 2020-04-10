package com.daasuu.sample.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

import com.daasuu.camerarecorder.egl.filter.GlFilter;

/**
 * Created by sudamasayuki on 2017/05/18.
 * <p>
 * Ref : https://stackoverflow.com/questions/18285373/android-opengl-es-2-texture-quadrants-rotated
 * Ref : https://github.com/google/grafika/issues/74
 * Ref : https://stackoverflow.com/questions/34575127/texture-is-flipped-and-upside-down
 * Ref : https://www.glprogramming.com/red/chapter08.html
 * Ref: https://blog.jayway.com/2010/12/30/opengl-es-tutorial-for-android-part-vi-textures/
 */

public class GlRajatOverlayFilter extends GlFilter {

    private int[] textures = new int[1];

    private Bitmap bitmap;

    protected Size inputResolution = new Size(720, 1280);

    public GlRajatOverlayFilter(Bitmap bitmap) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER_OLD);
        //super(VERTEX_SHADER, FRAGMENT_SHADER);
        this.bitmap = bitmap;

    }

    private final static String FRAGMENT_SHADER_OLD =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "uniform lowp sampler2D oTexture;\n" +
                    "void main() {\n" +
                    "   lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                    "   lowp vec4 textureColor2 = texture2D(oTexture, vTextureCoord);\n" +
                    "   \n" +
                    "   gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);\n" +
                    "}\n";

    private final static String VERTEX_SHADER = "attribute vec4 aPosition;        \n" +
            "attribute vec4 aTextureCoord;    \n" +
            "attribute vec4 aStickerCoord;    \n" +
            "\n" +
            "varying vec2 textureCoordinate;  \n" +
            "varying vec2 stickerCoordinate;  \n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    gl_Position = aPosition;\n" +
            "\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "    stickerCoordinate = aStickerCoord.xy;\n" +
            "}";

    private final static String FRAGMENT_SHADER = "precision mediump float;\n" +
            "varying vec2 textureCoordinate;    \n" +
            "varying vec2 stickerCoordinate;    \n" +
            "\n" +
            "uniform sampler2D sTexture;    \n" +
            "uniform sampler2D oTexture;  \n" +
            "uniform int enableSticker;         \n" +
            "\n" +
            "// 混合\n" +
            "vec4 blendColor(vec4 frameColor, vec4 sourceColor) {\n" +
            "    vec4 outputColor;\n" +
            "    outputColor.r = frameColor.r + sourceColor.r * sourceColor.a * (1.0 - frameColor.a);\n" +
            "    outputColor.g = frameColor.g + sourceColor.g * sourceColor.a * (1.0 - frameColor.a);\n" +
            "    outputColor.b = frameColor.b + sourceColor.b * sourceColor.a * (1.0 - frameColor.a);\n" +
            "    outputColor.a = frameColor.a + sourceColor.a * (1.0 - frameColor.a);\n" +
            "    return outputColor;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    lowp vec4 sourceColor = texture2D(sTexture, textureCoordinate);\n" +
            "    if (enableSticker == 99) {\n" +
            "        gl_FragColor = sourceColor;\n" +
            "    } else {\n" +
            "        lowp vec4 frameColor = texture2D(oTexture, stickerCoordinate);\n" +
            "        gl_FragColor = blendColor(frameColor, sourceColor);\n" +
            "    }\n" +
            "}";

    public void setResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Size(width, height));
        bitmap = scaleCenterCrop(bitmap, width, height);
        //createBitmap();
    }

    private void createBitmap() {

        Matrix m = new Matrix();
        m.postRotate(180);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        //bitmap = Bitmap.createScaledBitmap(bitmap, inputResolution.getWidth(), inputResolution.getHeight(), true);

    }

    @Override
    public void setup() {
        super.setup();// 1
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

    }

    @Override
    public void onDraw() {

        if (bitmap == null) {
            return;
        }

        int offsetDepthMapTextureUniform = getHandle("oTexture"); // 3

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        if (bitmap != null && !bitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }

        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        releaseBitmap();
    }

    public static int createTexture(Bitmap bitmap) {
        int[] texture = new int[1];
        if (bitmap != null && !bitmap.isRecycled()) {
            //Generate texture
            GLES20.glGenTextures(1, texture, 0);
            //checkGlError("glGenTexture");
            //Generate texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //Set the reduction filter to use the color of the pixel with the closest coordinate in the texture as the color of the pixel to be drawn
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //Set the zoom filter to use the closest colors in the texture, and use the weighted average algorithm to get the color of the pixel to be drawn
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //Set the wrapping direction S and intercept the texture coordinates to [1 / 2n, 1-1 / 2n]. Will result in never merging with border
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //Set the wrapping direction T and intercept the texture coordinates to [1 / 2n, 1-1 / 2n]. Will result in never merging with border
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //Generate a 2D texture based on the parameters specified above
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture[0];
        }
        return 0;
    }


    private void releaseBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private Bitmap scaleCenterCrop(Bitmap source, int newWidth, int newHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top
                + scaledHeight);
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
                source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        float cx = source.getWidth() / 2f;
        float cy = source.getHeight() / 2f;
        matrix.postScale(-1, 1, cx, cy);
        dest = Bitmap.createBitmap(dest, 0, 0, dest.getWidth(), dest.getHeight(), matrix, true);
        return dest;
    }
}

