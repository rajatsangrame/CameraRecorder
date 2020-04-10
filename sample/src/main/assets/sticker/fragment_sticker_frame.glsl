precision mediump float;
varying vec2 textureCoordinate;
varying vec2 stickerCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D stickerTexture;
uniform int enableSticker;

vec4 blendColor(vec4 frameColor, vec4 sourceColor) {
    vec4 outputColor;
    outputColor.r = frameColor.r + sourceColor.r * sourceColor.a * (1.0 - frameColor.a);
    outputColor.g = frameColor.g + sourceColor.g * sourceColor.a * (1.0 - frameColor.a);
    outputColor.b = frameColor.b + sourceColor.b * sourceColor.a * (1.0 - frameColor.a);
    outputColor.a = frameColor.a + sourceColor.a * (1.0 - frameColor.a);
    return outputColor;
}

void main() {
    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);
    if (enableSticker == 0) {
        gl_FragColor = sourceColor;
    } else {
        lowp vec4 frameColor = texture2D(stickerTexture, stickerCoordinate);
        gl_FragColor = blendColor(frameColor, sourceColor);
    }
}
