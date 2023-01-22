#version 410 core

layout(location = 0) in vec2 pos;

uniform ivec2 windowSize;

void main() {
    gl_Position = vec4(pos, 0, 1);
}
