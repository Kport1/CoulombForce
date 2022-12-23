#version 410

out vec4 fragColor;

flat in vec2 pos1;
flat in vec2 pos2;
flat in float radius;

void main() {
    vec2 coord = (gl_FragCoord.xy / 400) - vec2(1, 1);
    float h = dot(coord - pos1, pos2 - pos1) / (length(pos2 - pos1) * length(pos2 - pos1));
    h = clamp(h, 0, 1);
    float dist = length(coord - (pos1 + h * (pos2 - pos1)));

    float alpha = mix(200, 0, dist - radius + 1);
    fragColor = vec4(1, 1, 1, alpha);
}
