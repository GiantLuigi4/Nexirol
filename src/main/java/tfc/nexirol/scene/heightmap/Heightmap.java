package tfc.nexirol.scene.heightmap;

import org.lwjgl.vulkan.VkExtent2D;
import tfc.renirol.frontend.enums.ImageLayout;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.masks.AccessMask;
import tfc.renirol.frontend.enums.masks.StageMask;
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

    public boolean needsUpdate(int mapX, int mapY) {
        return mapX != cx || mapY != cy;
    }

    public void updatePosition(CommandBuffer cmd, int centerX, int centerY) {
        int cDiffX = centerX - cx;
        int cDiffY = centerY - cy;
        if (cDiffX == 0 && cDiffY == 0) return;

        cmd.transition(
                img.getHandle(),
                StageMask.DRAW,
                StageMask.COLOR_ATTACHMENT_OUTPUT,
                ImageLayout.SHADER_READONLY,
                ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                AccessMask.SHADER_READ,
                AccessMask.COLOR_WRITE
        );
        shader.beginCompute(cmd, fbo, hmSize);

        int regStartX = ((hmSize.width()) + cx) % hmSize.width();
        int regEndX = ((hmSize.width()) + cx + cDiffX) % hmSize.width();
        int startSetX = 0;
        int endSetX = 0;

        if (cDiffX < 0) {
            int temp = regStartX;
            regStartX = regEndX;
            regEndX = temp;
            startSetX = cDiffX;
        } else endSetX = cDiffX;

        if (regStartX < 0) regStartX += hmSize.width();
        if (regEndX < 0) regEndX += hmSize.width();
        if (regEndX == 0) regEndX = hmSize.width();

        int regStartY = ((hmSize.height()) + cy) % hmSize.height();
        int regEndY = ((hmSize.height()) + cy + cDiffY) % hmSize.height();
        int startSetY = 0;
        int endSetY = 0;

        if (cDiffY < 0) {
            int temp = regStartY;
            regStartY = regEndY;
            regEndY = temp;
            startSetY = cDiffY;
        } else endSetY = cDiffY;

        if (regStartY < 0) regStartY += hmSize.height();
        if (regEndY < 0) regEndY += hmSize.height();
        if (regEndY == 0) regEndY = hmSize.height();

        if (cDiffX > 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + hmSize.width(),
                    cy - regStartY + startSetY,
                    regStartX,
                    regStartY + endSetY,
                    regEndX - regStartX,
                    hmSize.height()
            );
            shader.computeNoise(
                    cmd,
                    cx - regStartX + hmSize.width(),
                    cy - regStartY + hmSize.height() + startSetY,
                    regStartX,
                    0,
                    regEndX - regStartX,
                    regStartY + endSetY
            );
        } else if (cDiffX < 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + cDiffX,
                    cy - regStartY + startSetY,
                    regStartX,
                    regStartY,
                    regEndX - regStartX,
                    hmSize.height()
            );
            shader.computeNoise(
                    cmd,
                    cx - regStartX + cDiffX,
                    cy - regStartY + hmSize.height() + startSetY,
                    regStartX,
                    0,
                    regEndX - regStartX,
                    regStartY
            );
        }

        if (cDiffY > 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + startSetX,
                    cy - regStartY + hmSize.height(),
                    regStartX + endSetX,
                    regStartY,
                    hmSize.width(),
                    regEndY - regStartY
            );
            shader.computeNoise(
                    cmd,
                    cx - regStartX + hmSize.width() + startSetX,
                    cy - regStartY + hmSize.height(),
                    0,
                    regStartY,
                    regStartX + endSetX,
                    regEndY - regStartY
            );
        } else if (cDiffY < 0) {
            shader.computeNoise(
                    cmd,
                    cx - regStartX + startSetX,
                    cy - regStartY + cDiffY,
                    regStartX + endSetX,
                    regStartY,
                    hmSize.width(),
                    regEndY - regStartY
            );
            shader.computeNoise(
                    cmd,
                    cx - regStartX + hmSize.width() + startSetX,
                    cy - regStartY + cDiffY,
                    0,
                    regStartY,
                    regStartX + endSetX,
                    regEndY - regStartY
            );
        }

        shader.finishCompute(cmd);
        cmd.transition(
                img.getHandle(),
                StageMask.COLOR_ATTACHMENT_OUTPUT,
                StageMask.DRAW,
                ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                ImageLayout.SHADER_READONLY,
                AccessMask.COLOR_WRITE,
                AccessMask.SHADER_READ
        );

        cx = centerX;
        cy = centerY;
    }
}
