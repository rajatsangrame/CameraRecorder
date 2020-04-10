package com.daasuu.sample.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Size;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.daasuu.camerarecorder.egl.filter.GlFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_TEXTURE_COORD_ARRAY;
import static android.opengl.GLES10.glEnableClientState;
import static android.opengl.GLES20.GL_TEXTURE;
import static android.opengl.GLES32.GL_QUADS;

/**
 * Created by sudamasayuki on 2017/05/18.
 * <p>
 * Ref : https://stackoverflow.com/questions/18285373/android-opengl-es-2-texture-quadrants-rotated
 * Ref : https://github.com/google/grafika/issues/74
 * Ref : https://stackoverflow.com/questions/34575127/texture-is-flipped-and-upside-down
 * Ref : https://www.glprogramming.com/red/chapter08.html
 * Ref: https://blog.jayway.com/2010/12/30/opengl-es-tutorial-for-android-part-vi-textures/
 */

public class GlRajatDynamicFilter extends GlFilter {

    private int[] textures = new int[1];

    private Bitmap bitmap;

    protected Size inputResolution = new Size(720, 1280);

    float textureCoordinates[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f};

    public GlRajatDynamicFilter(Bitmap bitmap) {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        this.bitmap = bitmap;

    }

    private static final String VERTEX_SHADER_1 =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER_1 =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private final static String VERTEX_SHADER = "uniform mat4 uMVPMatrix;        // 变换矩阵\n" +
            "attribute vec4 aPosition;       // 图像顶点坐标\n" +
            "attribute vec4 aTextureCoord;   // 图像纹理坐标\n" +
            "\n" +
            "varying vec2 textureCoordinate; // 图像纹理坐标\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "}";

    private final static String FRAGMENT_SHADER = "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D sTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, textureCoordinate);\n" +
            "}";

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


    public void setResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Size(width, height));
        //bitmap = scaleCenterCrop(bitmap, width, height);
        createBitmap();
    }

    private void createBitmap() {

        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        float cx = bitmap.getWidth() / 2f;
        float cy = bitmap.getHeight() / 2f;
        matrix.postScale(-1, 1, cx, cy);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //bitmap = Bitmap.createScaledBitmap(bitmap, inputResolution.getWidth(), inputResolution.getHeight(), true);

    }

    @Override
    public void setup() {
        super.setup();
        mMVPMatrixHandle = getHandle("uMVPMatrix");

        GLES20.glGenTextures(1, textures, 0);
        //checkGlError("glGenTexture");
        //Generate texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //Set the reduction filter to use the color of the pixel with the closest coordinate in the texture as the color of the pixel to be drawn
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //Set the zoom filter to use the closest colors in the texture, and use the weighted average algorithm to get the color of the pixel to be drawn
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //Set the wrapping direction S and intercept the texture coordinates to [1 / 2n, 1-1 / 2n]. Will result in never merging with border
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //Set the wrapping direction T and intercept the texture coordinates to [1 / 2n, 1-1 / 2n]. Will result in never merging with border
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


    }

    private int flipScale = 1;
    private int rotation = 0;
    private float scale = 1f;
    private float x;
    private float y;
    private float swidth;
    private float sheight;

    private final float mStickerVertices[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f, -1.0f,  // 1 bottom right
            -1.0f, 1.0f,  // 2 top left
            1.0f, 1.0f,  // 3 top right
    };

    private int mMVPMatrixHandle;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private void calculateStickerVertices() {
        //贴纸宽高

        swidth = 720;
        sheight = 720;
        float stickerHeight = 720f;
        float stickerWidth = 720f;
        float stickerX = x;//0
        float stickerY = y;//0
        mStickerVertices[0] = stickerX;
        mStickerVertices[1] = stickerY;
        mStickerVertices[2] = stickerX + stickerWidth;
        mStickerVertices[3] = stickerY;
        mStickerVertices[4] = stickerX;
        mStickerVertices[5] = stickerY + stickerHeight;
        mStickerVertices[6] = stickerX + stickerWidth;
        mStickerVertices[7] = stickerY + stickerHeight;


        transformMatrix.idt();

        //左下角为坐标中心
        float centerX = stickerX + stickerWidth / 2f;
        float centerY = stickerY + stickerHeight / 2f;


        transformMatrix.translate(centerX, centerY, 0);
        transformMatrix.rotate(Vector3.Z, rotation);

        transformMatrix.scale(scale, scale, scale);
        transformMatrix.translate(-centerX, -centerY, 0);


        rotation += 1;
        //test
        if (scale >= 1.2f) {
            flipScale = -1;
        }
        if (scale <= 0.8f) {
            flipScale = 1;
        }
        scale += 0.01f * flipScale;

    }

    @Override
    public void onDraw() {
        if (bitmap == null) {
            return;
        }

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        if (!bitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }

        //calculateStickerVertices();
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, combinedMatrix.val, 0);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBlendFuncSeparate(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);


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

