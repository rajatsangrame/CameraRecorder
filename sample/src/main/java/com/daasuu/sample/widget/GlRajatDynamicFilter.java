package com.daasuu.sample.widget;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

import com.daasuu.camerarecorder.egl.filter.GlFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

    public GlRajatDynamicFilter(Bitmap bitmap) {
        //super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER_OLD);
        super(VERTEX_SHADER, FRAGMENT_SHADER_OLD);
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

    protected static final String VERTEX_SHADER =
            "attribute highp vec4 aPosition;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "attribute highp vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "gl_Position = uMVPMatrix * aPosition;\n" +
                    "vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

//    private final static String VERTEX_SHADER =
//            "#version 100\n" +
//                    "uniform mat4 uMVPMatrix; // MVP-matrix for moving and rotating texture\n" +
//                    "\n" +
//                    "attribute vec4 aPosition; // data of vertices rectangle \n" +
//                    "attribute vec2 aTextureCoord; \n" +
//                    "\n" +
//                    "varying vec2 vTextureCoord; \n" +
//                    "\n" +
//                    "void main() {\n" +
//                    "    vTextureCoord = aTextureCoord;\n" +
//                    "    gl_Position = uMVPMatrix * aPosition;\n" +
//                    "}";
//
//    protected static final String FRAGMENT_SHADER_OLD = " #version 100\n" +
//            "    varying vec2 vTextureCoord;\n" +
//            "    uniform sampler2D sTexture;\n" +
//            "\n" +
//            "    void main() {\n" +
//            "        gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
//            "    }";


    public void setResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Size(width, height));
        //bitmap = scaleCenterCrop(bitmap, width, height);
        createBitmap();

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;

        android.opengl.Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0f,
                0f, 0f, -4f, 0f, 1.0f, 0.0f); // camera
        android.opengl.Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void createBitmap() {

        Matrix m = new Matrix();
        m.postRotate(180);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

    }

    int mMVPMatrixHandle;

    @Override
    public void setup() {
        super.setup();// 1

        //mMVPMatrixHandle = getHandle("uMVPMatrix");

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        // Ensure I can draw transparent stuff that overlaps properly
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    }

    protected float[] mvpMatrix = new float[16];
    protected float[] viewMatrix = new float[16];
    protected float[] projectionMatrix = new float[16];
    protected float[] modelMatrix = new float[16];
    protected float[] modelViewMatrix = new float[16];

    public static FloatBuffer getMVPMatrixAsFloatBuffer(float[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder());
        FloatBuffer returnBuffer = byteBuffer.asFloatBuffer();
        returnBuffer.put(data).position(0);
        return returnBuffer;
    }

    float angle = 0f;

    void spotPosition() {
        android.opengl.Matrix.setIdentityM(modelMatrix, 0);
        android.opengl.Matrix.translateM(modelMatrix, 0, x, y, 0.0f); // move object
        android.opengl.Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f); // rotate object
        android.opengl.Matrix.scaleM(modelMatrix, 0, 4f, 4f, 4f);// scale object
        android.opengl.Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        angle++;
    }

    @Override
    public void onDraw() {

        if (bitmap == null) {
            return;
        }

        int offsetDepthMapTextureUniform = getHandle("oTexture"); // 3
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);

        //spotPosition();
        calculateStickerVertices();
        //combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        //GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        if (bitmap != null && !bitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        releaseBitmap();


        GLES20.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, getMVPMatrixAsFloatBuffer(mvpMatrix));

    }

    private int flipScale = 1;
    private int rotation = 0;
    private float scale = 1f;
    private float x;
    private float y;
    private float swidth;
    private float sheight;

    private final float mStickerVertices[] = {
            -2.0f, -2.0f,  // 0 bottom left
            2.0f, -2.0f,  // 1 bottom right
            -2.0f, 2.0f,  // 2 top left
            2.0f, 2.0f,  // 3 top right
    };

    private void calculateStickerVertices() {

        swidth = 720;
        sheight = 720;
        float stickerHeight = 720f;
        float stickerWidth = 720f;
        float stickerX = x;//0
        float stickerY = y;//0
        float centerX = stickerX + stickerWidth / 2f;
        float centerY = stickerY + stickerHeight / 2f;
        android.opengl.Matrix.setIdentityM(mvpMatrix, 0);
        android.opengl.Matrix.rotateM(mvpMatrix, 0, rotation, 0.0f, 0.0f, 10.0f);

        rotation++;

    }

    private void releaseBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /* Unused Code Below, Don't Scroll
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

    private int loadTexture(Bitmap bitmap) {

        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        return textureId[0];
    }

    public class Sprite {
        //Reference to Activity Context

        //Added for Textures
        private final FloatBuffer mCubeTextureCoordinates;
        private int mTextureUniformHandle;
        private int mTextureCoordinateHandle;
        private final int mTextureCoordinateDataSize = 2;
        private int mTextureDataHandle;

        private final String vertexShaderCode =
                //Test
                "attribute vec2 a_TexCoordinate;" +
                        "varying vec2 v_TexCoordinate;" +
                        //End Test
                        "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        //Test
                        "v_TexCoordinate = a_TexCoordinate;" +
                        //End Test
                        "}";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 v_Color;" +
                        //Test
                        "uniform sampler2D u_Texture;" +
                        "varying vec2 v_TexCoordinate;" +
                        //End Test
                        "void main() {" +
                        //"gl_FragColor = v_Color;" +
                        //"gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));" +
                        // Just draw the texture, don't apply a color
                        "gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
                        "}";

        private final int shaderProgram;
        private final FloatBuffer vertexBuffer;
        private final ShortBuffer drawListBuffer;
        private int mPositionHandle;
        private int mMVPMatrixHandle;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 2;

        float spriteCoords[] = {
                -0.5f, 0.5f,   // top left
                -0.5f, -0.5f,   // bottom left
                0.5f, -0.5f,   // bottom right
                0.5f, 0.5f  //top right
        };

        private short drawOrder[] = {0, 1, 2, 0, 2, 3}; //Order to draw vertices
        private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

        // Set color with red, green, blue and alpha (opacity) values

        // Image to draw as a texture

        public Sprite(final Context activityContext) {

            //Initialize Vertex Byte Buffer for Shape Coordinates / # of coordinate values * 4 bytes per float
            ByteBuffer bb = ByteBuffer.allocateDirect(spriteCoords.length * 4);
            //Use the Device's Native Byte Order
            bb.order(ByteOrder.nativeOrder());
            //Create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();
            //Add the coordinates to the FloatBuffer
            vertexBuffer.put(spriteCoords);
            //Set the Buffer to Read the first coordinate
            vertexBuffer.position(0);

            // S, T (or X, Y)
            // Texture coordinate data.
            // Because images have a Y axis pointing downward (values increase as you move down the image) while
            // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
            // What's more is that the texture coordinates are the same for every face.
            final float[] cubeTextureCoordinateData =
                    {
                            0.5f, 0.5f,
                            0.5f, -0.5f,
                            -0.5f, -0.5f,
                            -0.5f, 0.5f

                    };

            mCubeTextureCoordinates = ByteBuffer
                    .allocateDirect(cubeTextureCoordinateData.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

            //Initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(spriteCoords.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);

            shaderProgram = GLES20.glCreateProgram();


            //Texture Code
            GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");

            GLES20.glLinkProgram(shaderProgram);

            //Load the texture
            mTextureDataHandle = loadTexture(bitmap);
        }

        public void draw(float[] mvpMatrix) {
            //Add program to OpenGL ES Environment
            GLES20.glUseProgram(shaderProgram);

            //Get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

            //Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            //Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);


            //Set Texture Handles and bind Texture
            mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgram, "u_Texture");
            mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");

            //Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            //Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

            //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
            GLES20.glUniform1i(mTextureUniformHandle, 0);

            //Pass in the texture coordinate information
            mCubeTextureCoordinates.position(0);
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            //Get Handle to Shape's Transformation Matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

            //Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

            //Draw the triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            //Disable Vertex Array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

        int loadTexture(Bitmap bitmap) {
            final int[] textureHandle = new int[1];

            GLES20.glGenTextures(1, textureHandle, 0);

            if (textureHandle[0] != 0) {

                // Bind to the texture in OpenGL
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

                // Set filtering
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER,
                        GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER,
                        GLES20.GL_NEAREST);

                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

                // Recycle the bitmap, since its data has been loaded into OpenGL.
                bitmap.recycle();
            }

            if (textureHandle[0] == 0) {
                throw new RuntimeException("Error loading texture.");
            }

            return textureHandle[0];
        }
    }*/
}

