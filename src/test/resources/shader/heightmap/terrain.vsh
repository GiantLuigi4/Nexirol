#version 450

// ======= MODEL DATA =======
layout (location = 0) in vec4 Position;

// ======= OUTPUT DATA =======
layout (location = 0) out ivec2 offset;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * Position.xyzw;
}
