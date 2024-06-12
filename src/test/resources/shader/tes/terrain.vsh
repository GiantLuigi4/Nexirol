#version 450

// ======= MODEL DATA =======
layout (location = 0) in vec4 Position;

// ======= OUTPUT DATA =======
layout (location = 0) out ivec2 offset;

void main() {
    int GRID = 11;
    offset = ivec2((gl_InstanceIndex % GRID), gl_InstanceIndex / GRID) - ivec2(GRID, GRID) / 2;
    gl_Position = Position.xyzw + vec4(0.5, 0, 0.5, 0);
}
