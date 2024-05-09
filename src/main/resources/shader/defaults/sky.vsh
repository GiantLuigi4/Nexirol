#version 450
#extension GL_ARB_separate_shader_objects : enable
//#extension GL_EXT_gpu_shader4 : enable

layout(location = 0) in vec4 Position;

layout(location = 0) out vec4 fgCoord;

layout(binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

void main() {
    fgCoord = Position;
//    gl_Position = Position;
    gl_Position = projectionMatrix * mat4(mat3(modelViewMatrix)) * Position;
}
