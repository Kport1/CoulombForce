#version 410 core

out vec4 fragColor;

uniform vec2 pos1;
uniform vec2 pos2;
uniform float radius;
uniform float value;

uniform ivec2 windowSize;

void main() {
    vec2 coord = (gl_FragCoord.xy / windowSize * 2) - vec2(1, 1);
    float h = dot(coord - pos1, pos2 - pos1) / (length(pos2 - pos1) * length(pos2 - pos1));
    h = clamp(h, 0, 1);
    float dist = length(coord - (pos1 + h * (pos2 - pos1)));
    dist -= radius;
    dist = min(dist, length(coord - mix(pos1, pos2, value)) - radius * 2);

    float alpha = 1 - smoothstep(-0.005, 0, dist);
    fragColor = vec4(1, 1, 1, alpha);
}