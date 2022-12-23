#version 410

layout(location = 0) in vec2 pos1_;
layout(location = 1) in vec2 pos2_;
layout(location = 2) in float radius_;

flat out vec2 pos1;
flat out vec2 pos2;
flat out float radius;

void main() {
    pos1 = pos1_;
    pos2 = pos2_;
    radius = radius_;
    gl_PointSize = 800;
    gl_Position = vec4(0, 0, 0, 1);
}
