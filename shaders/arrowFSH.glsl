#version 410 core

out vec4 fragColor;

uniform vec2 pos1;
uniform vec2 pos2;
uniform float radius;

uniform ivec2 windowSize;

float triangleSDF(vec2 p, float r){
    const float k = sqrt(3.0);
    p.x = abs(p.x) - r;
    p.y = p.y + r/k;
    if( p.x+k*p.y > 0.0 ) p=vec2(p.x-k*p.y, -k*p.x-p.y) / 2.0;
    p.x = p.x - clamp( p.x, -2.0*r, 0.0 );
    return -length(p) * sign(p.y);
}

vec2 rotate(vec2 p, float r){
    float sine = sin(r);
    float cosine = cos(r);
    return vec2(cosine * p.x + sine * p.y, cosine * p.y - sine * p.x);
}

void main() {
    if(pos1 == pos2) discard;
    if(isnan(pos1.x) || isnan(pos1.y) || isnan(pos2.x) || isnan(pos2.y)) discard;
    if(isinf(pos1.x) || isinf(pos1.y) || isinf(pos2.x) || isinf(pos2.y)) discard;

    vec2 coord = (gl_FragCoord.xy / windowSize * 2) - vec2(1, 1);
    float h = dot(coord - pos1, pos2 - pos1) / (length(pos2 - pos1) * length(pos2 - pos1));
    h = clamp(h, 0, 1);
    float dist = length(coord - (pos1 + h * (pos2 - pos1)));
    dist -= radius;

    float a = atan(pos2.y - pos1.y, pos2.x - pos1.x) + 3.1415 * 1.5;
    dist = min(dist, triangleSDF(rotate(coord - pos2, a) * vec2(1, 0.7), radius * 2));

    float alpha = 1 - smoothstep(-0.005, 0, dist);
    fragColor = vec4(1, 1, 1, alpha * 0.5);
}
