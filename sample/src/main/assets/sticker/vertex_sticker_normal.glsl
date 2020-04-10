uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec4 aTextureCoord;

varying vec2 textureCoordinate;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    textureCoordinate = aTextureCoord.xy;
}



//attribute highp vec4 aPosition;
//attribute highp vec4 aTextureCoord;
//varying highp vec2 vTextureCoord;
//void main() {
//gl_Position = aPosition;
//vTextureCoord = aTextureCoord.xy;
//}