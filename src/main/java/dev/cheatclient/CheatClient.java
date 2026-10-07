package dev.cheatclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class CheatClient implements ClientModInitializer {
    public static KeyBinding guiKey, freecamKey, espKey, susKey;

    private static KeyBinding key(String id, int code) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.cheatclient." + id, code, "category.cheatclient"));
    }

    @Override
    public void onInitializeClient() {
        guiKey = key("gui", GLFW.GLFW_KEY_RIGHT_SHIFT);
        freecamKey = key("freecam", GLFW.GLFW_KEY_H);
        espKey = key("esp", GLFW.GLFW_KEY_J);
        susKey = key("sus", GLFW.GLFW_KEY_K);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) mc.setScreen(new ModuleScreen());
            while (freecamKey.wasPressed()) Freecam.toggle();
            while (espKey.wasPressed()) Config.storageEsp = !Config.storageEsp;
            while (susKey.wasPressed()) Config.susFinder = !Config.susFinder;
            Freecam.tick();
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> SusFinder.onChunkLoad(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> SusFinder.onChunkUnload(chunk.getPos()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> {
            Freecam.disable(false);
            SusFinder.clear();
        });

        WorldRenderEvents.LAST.register(ctx -> {
            MatrixStack ms = ctx.matrixStack();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (ms == null || mc.world == null) return;
            Vec3d cam = ctx.camera().getPos();
            MatrixStack.Entry e = ms.peek();
            Render.begin();
            if (Config.storageEsp) StorageEsp.render(e, cam);
            if (Config.susFinder) SusFinder.render(e, cam);
            Render.end();
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            int y = 4;
            if (Config.storageEsp) { ctx.drawTextWithShadow(mc.textRenderer, "Storage ESP", 4, y, 0x55FF55); y += 10; }
            if (Config.freecam) { ctx.drawTextWithShadow(mc.textRenderer, "Freecam", 4, y, 0x55FFFF); y += 10; }
            if (Config.susFinder) { SusFinder.drawHud(ctx, 4, y); }
        });
    }
}
