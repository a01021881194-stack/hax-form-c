package dev.cheatclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Freecam: swaps the camera entity for a detached fake player and blanks the real
 * player's input so your character stands still. Mouse look still rotates the real
 * player's head (the server will see that).
 */
public final class Freecam {
    private static OtherClientPlayerEntity cam;

    public static void toggle() {
        if (Config.freecam) disable(true); else enable();
    }

    private static void enable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        cam = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
        cam.refreshPositionAndAngles(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                mc.player.getYaw(), mc.player.getPitch());
        cam.noClip = true;
        mc.setCameraEntity(cam);
        mc.player.input = new Input(); // base Input does nothing -> player stands still
        Config.freecam = true;
    }

    public static void disable(boolean restoreInput) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (restoreInput && mc.player != null) {
            mc.setCameraEntity(mc.player);
            mc.player.input = new KeyboardInput(mc.options);
        }
        cam = null;
        Config.freecam = false;
    }

    public static void tick() {
        if (!Config.freecam || cam == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) { disable(false); return; }
        if (mc.currentScreen != null) return; // don't fly around while in a menu

        GameOptions o = mc.options;
        float yaw = mc.player.getYaw(), pitch = mc.player.getPitch();
        Vec3d look = Vec3d.fromPolar(pitch, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);

        Vec3d move = Vec3d.ZERO;
        if (o.forwardKey.isPressed()) move = move.add(look);
        if (o.backKey.isPressed()) move = move.subtract(look);
        if (o.rightKey.isPressed()) move = move.add(right);
        if (o.leftKey.isPressed()) move = move.subtract(right);
        if (o.jumpKey.isPressed()) move = move.add(0, 1, 0);
        if (o.sneakKey.isPressed()) move = move.subtract(0, 1, 0);
        if (move.lengthSquared() > 0) {
            double speed = Config.freecamSpeed * (o.sprintKey.isPressed() ? 2.5 : 1.0);
            move = move.normalize().multiply(speed);
        }

        // keep prev* in sync so rendering interpolates smoothly between ticks
        cam.prevX = cam.lastRenderX = cam.getX();
        cam.prevY = cam.lastRenderY = cam.getY();
        cam.prevZ = cam.lastRenderZ = cam.getZ();
        cam.prevYaw = cam.getYaw();
        cam.prevPitch = cam.getPitch();
        cam.setPosition(cam.getX() + move.x, cam.getY() + move.y, cam.getZ() + move.z);
        cam.setYaw(yaw);
        cam.setPitch(pitch);
    }

    private Freecam() {}
}
