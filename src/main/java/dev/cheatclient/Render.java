package dev.cheatclient;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

/** Through-wall wireframe boxes, batched into one draw call per frame. */
public final class Render {
    private static BufferBuilder buf;
    private static int lines;

    public static void begin() {
        buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        lines = 0;
    }

    public static void end() {
        if (buf == null) return;
        if (lines > 0) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2f);
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            BufferRenderer.drawWithGlobalProgram(buf.end());
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
        buf = null;
    }

    /** Box must already be relative to the camera position. */
    public static void box(MatrixStack.Entry e, Box b, float[] c) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        // bottom
        line(e, x1, y1, z1, x2, y1, z1, c); line(e, x2, y1, z1, x2, y1, z2, c);
        line(e, x2, y1, z2, x1, y1, z2, c); line(e, x1, y1, z2, x1, y1, z1, c);
        // top
        line(e, x1, y2, z1, x2, y2, z1, c); line(e, x2, y2, z1, x2, y2, z2, c);
        line(e, x2, y2, z2, x1, y2, z2, c); line(e, x1, y2, z2, x1, y2, z1, c);
        // verticals
        line(e, x1, y1, z1, x1, y2, z1, c); line(e, x2, y1, z1, x2, y2, z1, c);
        line(e, x2, y1, z2, x2, y2, z2, c); line(e, x1, y1, z2, x1, y2, z2, c);
    }

    private static void line(MatrixStack.Entry e, float x1, float y1, float z1,
                             float x2, float y2, float z2, float[] c) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        dx /= len; dy /= len; dz /= len;
        buf.vertex(e, x1, y1, z1).color(c[0], c[1], c[2], c[3]).normal(e, dx, dy, dz);
        buf.vertex(e, x2, y2, z2).color(c[0], c[1], c[2], c[3]).normal(e, dx, dy, dz);
        lines++;
    }

    private Render() {}
}
