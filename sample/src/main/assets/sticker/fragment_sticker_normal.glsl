precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

void main() {
    gl_FragColor = texture2D(inputTexture, textureCoordinate);
}

//precision mediump float;
//varying highp vec2 vTextureCoord;
//uniform lowp sampler2D sTexture;
//void main() {
//gl_FragColor = texture2D(sTexture, vTextureCoord);
//}