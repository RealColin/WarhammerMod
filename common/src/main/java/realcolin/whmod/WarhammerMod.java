package realcolin.whmod;

import realcolin.whmod.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class WarhammerMod {

    public static void init() {
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
            Constants.LOG.info("Warhammer Mod common initialization");
        }
    }
}