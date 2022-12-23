#version 410
layout (location = 0) in vec3 vertexPos;
layout (location = 1) in float radius;
layout (location = 2) in float _charge;

flat out float charge;

void main() {
    charge = _charge;
    gl_PointSize = radius * 800;
    gl_Position = vec4(vertexPos, 1.0);
}
