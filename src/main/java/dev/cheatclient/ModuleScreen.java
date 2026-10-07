package dev.cheatclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ModuleScreen extends Screen {
    private KeyBinding waiting = null;
    private int ctx_label_y = 0;

    public ModuleScreen() { super(Text.literal("Cheat Client")); }

    private void keybind(int x, int y, int w, String name, KeyBinding kb) {
        Text t = waiting == kb
                ? Text.literal(name + ": \u00a7e> press a key <")
                : Text.literal(name + ": ").append(kb.getBoundKeyLocalizedText());
        addDrawableChild(ButtonWidget.builder(t, b -> { waiting = kb; clearAndInit(); })
                .dimensions(x, y, w, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waiting != null) {
            // ESC = unbind, anything else = bind
            InputUtil.Key key = keyCode == GLFW.GLFW_KEY_ESCAPE
                    ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(keyCode, scanCode);
            waiting.setBoundKey(key);
            KeyBinding.updateKeysByCode();
            MinecraftClient.getInstance().options.write();
            waiting = null;
            clearAndInit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private void toggle(int x, int y, int w, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(x, y, w, 20).build());
    }

    @Override
    protected void init() {
        int x = 10, y = 24, w = 160, gap = 22;

        toggle(x, y, w, "Storage ESP", () -> Config.storageEsp, v -> Config.storageEsp = v); y += gap;
        toggle(x, y, w, "Freecam", () -> Config.freecam, v -> Freecam.toggle()); y += gap;
        toggle(x, y, w, "Sus Chunk Finder", () -> Config.susFinder, v -> Config.susFinder = v); y += gap;
        toggle(x, y, w, "Rotated Deepslate", () -> Config.susDeepslate, v -> Config.susDeepslate = v); y += gap;
        toggle(x, y, w, "Grown Amethyst", () -> Config.susAmethyst, v -> Config.susAmethyst = v); y += gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Amethyst min: " + Config.amethystThreshold), b -> {
            Config.amethystThreshold = Config.amethystThreshold % 10 + 1;
            b.setMessage(Text.literal("Amethyst min: " + Config.amethystThreshold));
        }).dimensions(x, y, w, 20).build()); y += gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Freecam speed: " + Config.freecamSpeed), b -> {
            Config.freecamSpeed = Config.freecamSpeed >= 3.0 ? 0.5 : Config.freecamSpeed + 0.5;
            b.setMessage(Text.literal("Freecam speed: " + Config.freecamSpeed));
        }).dimensions(x, y, w, 20).build()); y += gap + 8;

        ctx_label_y = y - 4;
        keybind(x, y, w, "Menu", CheatClient.guiKey); y += gap;
        keybind(x, y, w, "Freecam", CheatClient.freecamKey); y += gap;
        keybind(x, y, w, "Storage ESP", CheatClient.espKey); y += gap;
        keybind(x, y, w, "Sus Finder", CheatClient.susKey); y += gap;

        // storage type selection
        StorageType[] types = StorageType.values();
        int colX = 190, colW = 150, perCol = 10;
        for (int i = 0; i < types.length; i++) {
            StorageType t = types[i];
            int bx = colX + (i / perCol) * (colW + 5);
            int by = 24 + (i % perCol) * gap;
            toggle(bx, by, colW, t.label, () -> Config.storage.get(t), v -> Config.storage.put(t, v));
        }

        int by = 24 + perCol * gap + 4;
        addDrawableChild(ButtonWidget.builder(Text.literal("All storage ON"), b -> {
            for (StorageType t : types) Config.storage.put(t, true);
            clearAndInit();
        }).dimensions(colX, by, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("All storage OFF"), b -> {
            for (StorageType t : types) Config.storage.put(t, false);
            clearAndInit();
        }).dimensions(colX + 105, by, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(textRenderer, "Modules", 10, 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Storage ESP targets", 190, 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Keybinds (click, then press key; ESC unbinds)", 10, ctx_label_y - 6, 0xAAAAAA);
    }
}
