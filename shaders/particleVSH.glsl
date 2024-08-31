#version 410 core
layout (location = 0) in vec3 vertexPos;
layout (location = 1) in float _radius;
layout (location = 2) in float _charge;

flat out float charge;
flat out vec2 pos;
flat out float radius;

uniform ivec2 windowSize;

void main() {
    gl_PointSize = _radius * windowSize.x;
    gl_Position = vec4(vertexPos, 1.0);

    charge = _charge;
    pos = vertexPos.xy;
    radius = _radius;
}
