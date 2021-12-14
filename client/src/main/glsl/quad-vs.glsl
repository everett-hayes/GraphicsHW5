#version 300 es
in vec4 vertexPosition;
in vec2 vertexTexCoord;

out vec2 tex;
out vec4 rayDir;

uniform struct {
  mat4 rayDirMatrix;
  vec3 position;
} camera;

void main(void) {
  gl_Position = vertexPosition;
  tex = vertexTexCoord;
  rayDir = vertexPosition * camera.rayDirMatrix;
}
