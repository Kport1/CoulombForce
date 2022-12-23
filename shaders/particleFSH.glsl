#version 410

flat in float charge;

out vec4 fragCol;

void main() {
    vec2 pointCoord = gl_PointCoord.st * 2 - 1;
    float alpha = mix(8, 0, dot(pointCoord, pointCoord));

    if(charge > 0)
    fragCol = vec4(mix(vec3(0.9, 0.9, 0.9), vec3(1.0, 0.0, 0.0), charge), alpha);
    else
    fragCol = vec4(mix(vec3(0.9, 0.9, 0.9), vec3(0.0, 0.0, 1.0), -charge), alpha);
}
