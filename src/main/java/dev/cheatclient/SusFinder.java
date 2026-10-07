package dev.cheatclient;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scans chunks as they load for blocks that world generation doesn't produce on its own:
 *  - deepslate with a non-Y axis (natural deepslate is always axis=y)
 *  - fully grown amethyst clusters (geodes make some, so use the threshold in the menu)
 */
public final class SusFinder {
    private static final int MAX_SAMPLES = 8;
    private static final float[] DEEPSLATE_COLOR = {0.2f, 1f, 1f, 1f};
    private static final float[] AMETHYST_COLOR = {1f, 0.3f, 1f, 1f};
    private static final float[] CHUNK_COLOR = {1f, 1f, 0.2f, 1f};

    public static final class Result {
        int deepslate, amethyst, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        final List<BlockPos> deepslateSamples = new ArrayList<>();
        final List<BlockPos> amethystSamples = new ArrayList<>();
    }

    private static final Map<ChunkPos, Result> results = new ConcurrentHashMap<>();

    private static boolean rotatedDeepslate(BlockState s) {
        return s.isOf(Blocks.DEEPSLATE) && s.get(PillarBlock.AXIS) != Direction.Axis.Y;
    }

    private static boolean cluster(BlockState s) {
        return s.isOf(Blocks.AMETHYST_CLUSTER);
    }

    public static void onChunkLoad(WorldChunk chunk) {
        Result r = new Result();
        ChunkSection[] sections = chunk.getSectionArray();
        int baseY = chunk.getBottomY();
        ChunkPos cp = chunk.getPos();

        for (int i = 0; i < sections.length; i++) {
            ChunkSection sec = sections[i];
            if (sec == null || sec.isEmpty()) continue;
            // hasAny checks the palette (distinct block states), so this is cheap
            boolean hasDs = sec.hasAny(SusFinder::rotatedDeepslate);
            boolean hasAm = sec.hasAny(SusFinder::cluster);
            if (!hasDs && !hasAm) continue;

            for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                BlockState s = sec.getBlockState(x, y, z);
                boolean ds = hasDs && rotatedDeepslate(s);
                boolean am = hasAm && cluster(s);
                if (!ds && !am) continue;
                BlockPos pos = new BlockPos(cp.getStartX() + x, baseY + i * 16 + y, cp.getStartZ() + z);
                r.minY = Math.min(r.minY, pos.getY());
                r.maxY = Math.max(r.maxY, pos.getY());
                if (ds) { r.deepslate++; if (r.deepslateSamples.size() < MAX_SAMPLES) r.deepslateSamples.add(pos); }
                if (am) { r.amethyst++; if (r.amethystSamples.size() < MAX_SAMPLES) r.amethystSamples.add(pos); }
            }
        }

        if (r.deepslate > 0 || r.amethyst > 0) results.put(cp, r); else results.remove(cp);
    }

    public static void onChunkUnload(ChunkPos pos) { results.remove(pos); }
    public static void clear() { results.clear(); }

    private static boolean isSus(Result r) {
        return (Config.susDeepslate && r.deepslate > 0)
                || (Config.susAmethyst && r.amethyst >= Config.amethystThreshold);
    }

    public static void render(MatrixStack.Entry e, Vec3d cam) {
        for (Map.Entry<ChunkPos, Result> en : results.entrySet()) {
            Result r = en.getValue();
            if (!isSus(r)) continue;
            ChunkPos cp = en.getKey();

            Box col = new Box(cp.getStartX(), r.minY - 1, cp.getStartZ(),
                    cp.getStartX() + 16, r.maxY + 2, cp.getStartZ() + 16)
                    .offset(-cam.x, -cam.y, -cam.z);
            Render.box(e, col, CHUNK_COLOR);

            if (Config.susDeepslate)
                for (BlockPos p : r.deepslateSamples)
                    Render.box(e, new Box(p).offset(-cam.x, -cam.y, -cam.z), DEEPSLATE_COLOR);
            if (Config.susAmethyst && r.amethyst >= Config.amethystThreshold)
                for (BlockPos p : r.amethystSamples)
                    Render.box(e, new Box(p).offset(-cam.x, -cam.y, -cam.z), AMETHYST_COLOR);
        }
    }

    /** HUD list of the closest flagged chunks. */
    public static void drawHud(DrawContext ctx, int x, int y) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        Vec3d p = mc.player.getPos();

        List<Map.Entry<ChunkPos, Result>> list = new ArrayList<>();
        for (Map.Entry<ChunkPos, Result> en : results.entrySet()) if (isSus(en.getValue())) list.add(en);
        list.sort(Comparator.comparingDouble(en -> dist2(en.getKey(), p)));

        ctx.drawTextWithShadow(mc.textRenderer, "Sus chunks: " + list.size(), x, y, 0xFFFF55);
        int shown = 0;
        for (Map.Entry<ChunkPos, Result> en : list) {
            if (shown++ >= 6) break;
            ChunkPos cp = en.getKey();
            Result r = en.getValue();
            String line = String.format("[%d, %d]  x%d z%d  rotDS:%d  ame:%d  (%dm)",
                    cp.x, cp.z, cp.getCenterX(), cp.getCenterZ(), r.deepslate, r.amethyst,
                    (int) Math.sqrt(dist2(cp, p)));
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y + shown * 10, 0xFFFFFF);
        }
    }

    private static double dist2(ChunkPos cp, Vec3d p) {
        double dx = cp.getCenterX() - p.x, dz = cp.getCenterZ() - p.z;
        return dx * dx + dz * dz;
    }

    private SusFinder() {}
}
