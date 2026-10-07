package dev.cheatclient;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class StorageEsp {
    public static void render(MatrixStack.Entry e, Vec3d cam) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        int vd = mc.options.getClampedViewDistance();
        ChunkPos pc = mc.player.getChunkPos();

        for (int dx = -vd; dx <= vd; dx++) {
            for (int dz = -vd; dz <= vd; dz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x + dx, pc.z + dz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    StorageType t = StorageType.classify(be);
                    if (t == null || !Config.storage.get(t)) continue;
                    Box box = new Box(be.getPos()).offset(-cam.x, -cam.y, -cam.z);
                    Render.box(e, box, t.color);
                }
            }
        }

        for (Entity en : mc.world.getEntities()) {
            StorageType t = null;
            if (en instanceof ChestMinecartEntity) t = StorageType.CHEST_MINECART;
            else if (en instanceof HopperMinecartEntity) t = StorageType.HOPPER_MINECART;
            else if (en instanceof ChestBoatEntity) t = StorageType.CHEST_BOAT;
            if (t == null || !Config.storage.get(t)) continue;
            Render.box(e, en.getBoundingBox().offset(-cam.x, -cam.y, -cam.z), t.color);
        }
    }

    private StorageEsp() {}
}
