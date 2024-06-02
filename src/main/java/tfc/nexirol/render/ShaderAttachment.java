package tfc.nexirol.render;

import org.lwjgl.util.shaderc.Shaderc;
import tfc.nexirol.render.glsl.PreProcessor;
import tfc.renirol.frontend.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.shader.Shader;
import tfc.renirol.util.ShaderCompiler;

import java.util.Arrays;

// TODO: move to reni
public class ShaderAttachment {
    final Shader shader;

    public ShaderAttachment(
            PreProcessor processor, ShaderCompiler compiler,
            ReniLogicalDevice logical,
            ShaderStageFlags shaderStageFlags,
            String data, String file, String entry
    ) {
        data = PreProcessor.toText(processor.transform(PreProcessor.toLines(Arrays.asList(data.split("\n")))));
        shader = new Shader(
                compiler, logical, data,
                switch (shaderStageFlags) {
                    case VERTEX -> Shaderc.shaderc_glsl_vertex_shader;
                    case FRAGMENT -> Shaderc.shaderc_glsl_fragment_shader;
                    case COMPUTE -> Shaderc.shaderc_glsl_compute_shader;
                    case GEOMETRY -> Shaderc.shaderc_glsl_default_geometry_shader;
                    case TESSELATION_CONTROL -> Shaderc.shaderc_glsl_tess_control_shader;
                    case TESSELATION_EVALUATION -> Shaderc.shaderc_glsl_tess_evaluation_shader;
                    default -> throw new RuntimeException("NYI");
                },
                shaderStageFlags.bits,
                file, entry
        );
        if (!shader.compiledSuccessfully()) {
            System.out.println(data);
        }
    }

    public void destroy() {
        shader.destroy();
    }
}
