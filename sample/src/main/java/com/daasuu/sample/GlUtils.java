package com.daasuu.sample;

/**
 * Created by Rajat Sangrame on 6/4/20.
 * http://github.com/rajatsangrame
 */
public class GlUtils {

    public static final String FRAGMENT_FRAME = "// 前景贴纸绘制\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;     // 输入图像纹理坐标\n" +
            "varying vec2 stickerCoordinate;     // 贴纸纹理坐标\n" +
            "\n" +
            "uniform sampler2D inputTexture;     // 输入图像纹理\n" +
            "uniform sampler2D stickerTexture;   // 贴纸纹理\n" +
            "uniform int enableSticker;          // 是否绘制贴纸\n" +
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
            "    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    if (enableSticker == 0) {\n" +
            "        gl_FragColor = sourceColor;\n" +
            "    } else {\n" +
            "        lowp vec4 frameColor = texture2D(stickerTexture, stickerCoordinate);\n" +
            "        gl_FragColor = blendColor(frameColor, sourceColor);\n" +
            "    }\n" +
            "}";

    public static final String VERTEX_FRAME = "attribute vec4 aPosition;           // 图像顶点坐标\n" +
            "attribute vec4 aTextureCoord;       // 图像纹理坐标\n" +
            "attribute vec4 aStickerCoord;       // 贴纸纹理坐标\n" +
            "\n" +
            "varying vec2 textureCoordinate;     // 图像纹理坐标\n" +
            "varying vec2 stickerCoordinate;     // 贴纸纹理坐标\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    gl_Position = aPosition;\n" +
            "\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "    stickerCoordinate = aStickerCoord.xy;\n" +
            "}";

    public static final String DEFAULT_FRAGMENT_FRAME = "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(inputTexture, textureCoordinate);\n" +
            "}";

    public static final String DEFAULT_VERTEX_FRAME = "uniform mat4 uMVPMatrix;        // 变换矩阵\n" +
            "attribute vec4 aPosition;       // 图像顶点坐标\n" +
            "attribute vec4 aTextureCoord;   // 图像纹理坐标\n" +
            "\n" +
            "varying vec2 textureCoordinate; // 图像纹理坐标\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "}";
}

