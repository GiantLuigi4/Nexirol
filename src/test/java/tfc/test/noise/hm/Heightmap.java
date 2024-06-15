package tfc.test.noise.hm;

import org.lwjgl.vulkan.VkExtent2D;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.SwapchainUsage;
import tfc.renirol.frontend.enums.modes.image.FilterMode;
import tfc.renirol.frontend.enums.modes.image.MipmapMode;
import tfc.renirol.frontend.enums.modes.image.WrapMode;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.framebuffer.Framebuffer;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.image.Image;
import tfc.renirol.frontend.rendering.resource.image.texture.TextureSampler;
import tfc.renirol.itf.ReniDestructable;

public class Heightmap implements ReniDestructable {
    final HeightmapShader shader;
    final VkExtent2D hmSize;
    final Framebuffer fbo;
    final Image img;
    final Image imgTmp;
    final RenderPassInfo pass;

    int cx, cy;

    public int getCx() {
        return cx;
    }

    public int getCy() {
        return cy;
    }

    public Heightmap(
            HeightmapShader shader,
            int width, int height,
            Image img, Framebuffer fbo
    ) {
        this(
                shader,
                width, height,
                img, fbo, fbo.genericPass(shader.device, Operation.PERFORM, Operation.PERFORM)
        );
    }

    public Heightmap(
            HeightmapShader shader,
            int width, int height,
            Image img, Framebuffer fbo, RenderPassInfo pass
    ) {
        this.shader = shader;
        this.hmSize = VkExtent2D.calloc();
        this.hmSize.set(width, height);
        this.img = img;
        this.fbo = fbo;
        this.pass = pass;
        imgTmp = new Image(shader.device).setUsage(SwapchainUsage.COLOR);
        imgTmp.create(
                img.getExtents().width(), img.getExtents().height(),
                img.getFormat()
        );
    }

    public TextureSampler createSampler(
            WrapMode xWrap, WrapMode yWrap,
            FilterMode min, FilterMode mag,
            MipmapMode mips,
            boolean useAnisotropy, float anisotropy,
            float lodBias, float minLod, float maxLod
    ) {
        return img.createSampler(
                xWrap, yWrap,
                min, mag,
                mips,
                useAnisotropy, anisotropy,
                lodBias, minLod, maxLod
        );
    }

    @Override
    public void destroy() {
        hmSize.free();
    }

    public void prepare(CommandBuffer cmd, int centerX, int centerY) {
        cx = centerX;
        cy = centerY;
        shader.beginCompute(cmd, fbo, hmSize);
        shader.computeNoise(
                cmd,
                centerX, centerY,
                0, 0,
                hmSize.width(), hmSize.height()
        );
        shader.finishCompute(cmd);
    }

    public void updatePosition(CommandBuffer cmd, int centerX, int centerY) {
        int cDiffX = centerX - cx;
        int cDiffY = centerY - cy;
        if (cDiffX == 0 && cDiffY == 0) return;

        shader.beginCompute(cmd, fbo, hmSize);

        int regStartX = ((hmSize.width()) + cx) % hmSize.width();
        int regEndX = ((hmSize.width()) + cx + cDiffX) % hmSize.width();

        if (cDiffX < 0) {
            int temp = regStartX;
            regStartX = regEndX;
            regEndX = temp;
        }

        if (regStartX < 0) regStartX += hmSize.width();
        if (regEndX < 0) regEndX += hmSize.width();
        if (regEndX == 0) regEndX = hmSize.width();

        int regStartY = ((hmSize.height()) + cy) % hmSize.height();
        int regEndY = ((hmSize.height()) + cy + cDiffY) % hmSize.height();

        if (cDiffY < 0) {
            int temp = regStartY;
            regStartY = regEndY;
            regEndY = temp;
        }

        if (regStartY < 0) regStartY += hmSize.height();
        if (regEndY < 0) regEndY += hmSize.height();
        if (regEndY == 0) regEndY = hmSize.height();

        if (cDiffX > 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + hmSize.width(), 0,
                    regStartX, 0,
                    regEndX - regStartX, hmSize.height()
            );
        } else if (cDiffX < 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + cDiffX, 0,
                    regStartX, 0,
                    regEndX - regStartX, hmSize.height()
            );
        }

        shader.finishCompute(cmd);

        cx = centerX;
        cy = centerY;
    }
}
