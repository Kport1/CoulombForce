#version 410

layout(location = 0) in vec2 pos1_;
layout(location = 1) in vec2 pos2_;
layout(location = 2) in float radius_;
layout(location = 3) in float value_;

flat out vec2 pos1;
flat out vec2 pos2;
flat out float radius;
flat out float value;

void main() {
    pos1 = pos1_;
    pos2 = pos2_;
    radius = radius_;
    value = value_;
    gl_PointSize = 800;
    gl_Position = vec4(0, 0, 0, 1);
}
