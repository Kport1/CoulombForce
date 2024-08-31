#version 410 core

flat in float charge;
flat in vec2 pos;
flat in float radius;

out vec4 fragCol;

uniform ivec2 windowSize;

void main() {
    vec2 pointCoord = (gl_FragCoord.xy / windowSize * 2 - 1) - pos;
    pointCoord /= radius;
    float alpha = mix(8, 0, dot(pointCoord, pointCoord));

    if(charge > 0)
    fragCol = vec4(mix(vec3(0.9, 0.9, 0.9), vec3(1.0, 0.0, 0.0), charge), alpha);
    else
    fragCol = vec4(mix(vec3(0.9, 0.9, 0.9), vec3(0.0, 0.0, 1.0), -charge), alpha);
}
