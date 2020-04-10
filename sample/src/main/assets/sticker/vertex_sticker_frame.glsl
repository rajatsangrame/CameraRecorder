attribute vec4 aPosition;
attribute vec4 aTextureCoord;
attribute vec4 aStickerCoord;

varying vec2 textureCoordinate;
varying vec2 stickerCoordinate;

void main() {

    gl_Position = aPosition;

    textureCoordinate = aTextureCoord.xy;
    stickerCoordinate = aStickerCoord.xy;
}