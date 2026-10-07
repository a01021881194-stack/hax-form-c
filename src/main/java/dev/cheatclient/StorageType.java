package dev.cheatclient;

import net.minecraft.block.entity.*;
import java.util.function.Predicate;

/** Order matters: more specific classes must come before their parents. */
public enum StorageType {
    TRAPPED_CHEST("Trapped Chest", 1f, 0.2f, 0.2f, be -> be instanceof TrappedChestBlockEntity),
    CHEST("Chest", 1f, 0.6f, 0.1f, be -> be instanceof ChestBlockEntity),
    ENDER_CHEST("Ender Chest", 0.6f, 0.2f, 1f, be -> be instanceof EnderChestBlockEntity),
    BARREL("Barrel", 0.65f, 0.45f, 0.2f, be -> be instanceof BarrelBlockEntity),
    SHULKER("Shulker Box", 1f, 0.3f, 1f, be -> be instanceof ShulkerBoxBlockEntity),
    HOPPER("Hopper", 0.6f, 0.6f, 0.6f, be -> be instanceof HopperBlockEntity),
    DROPPER("Dropper", 0.8f, 0.8f, 0.3f, be -> be instanceof DropperBlockEntity),
    DISPENSER("Dispenser", 0.9f, 0.9f, 0.5f, be -> be instanceof DispenserBlockEntity),
    FURNACE("Furnace", 0.7f, 0.7f, 0.7f, be -> be instanceof FurnaceBlockEntity),
    BLAST_FURNACE("Blast Furnace", 0.5f, 0.5f, 0.6f, be -> be instanceof BlastFurnaceBlockEntity),
    SMOKER("Smoker", 0.5f, 0.4f, 0.3f, be -> be instanceof SmokerBlockEntity),
    BREWING_STAND("Brewing Stand", 0.3f, 0.9f, 0.5f, be -> be instanceof BrewingStandBlockEntity),
    CHISELED_BOOKSHELF("Chiseled Bookshelf", 0.8f, 0.6f, 0.4f, be -> be instanceof ChiseledBookshelfBlockEntity),
    DECORATED_POT("Decorated Pot", 0.8f, 0.4f, 0.3f, be -> be instanceof DecoratedPotBlockEntity),
    CRAFTER("Crafter", 0.4f, 0.8f, 0.8f, be -> be instanceof CrafterBlockEntity),
    JUKEBOX("Jukebox", 0.3f, 0.7f, 1f, be -> be instanceof JukeboxBlockEntity),
    LECTERN("Lectern", 0.9f, 0.8f, 0.5f, be -> be instanceof LecternBlockEntity),
    // entities (no block entity predicate)
    CHEST_MINECART("Chest Minecart", 1f, 0.8f, 0.2f, null),
    HOPPER_MINECART("Hopper Minecart", 0.7f, 0.7f, 0.7f, null),
    CHEST_BOAT("Chest Boat", 0.4f, 0.8f, 0.2f, null);

    public final String label;
    public final float[] color;
    public final Predicate<BlockEntity> test;

    StorageType(String label, float r, float g, float b, Predicate<BlockEntity> test) {
        this.label = label;
        this.color = new float[]{r, g, b, 1f};
        this.test = test;
    }

    public static StorageType classify(BlockEntity be) {
        for (StorageType t : values()) {
            if (t.test != null && t.test.test(be)) return t;
        }
        return null;
    }
}
