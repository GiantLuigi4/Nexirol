// https://stackoverflow.com/a/4275343
float rand(const vec2 co) {return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);}
float randI(const ivec2 co) {return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);}
