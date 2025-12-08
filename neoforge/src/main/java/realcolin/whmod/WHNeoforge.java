package realcolin.whmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class WHNeoforge {

    public WHNeoforge(IEventBus eventBus) {
        Constants.LOG.info("Warhammer NeoForge loaded");
        WarhammerMod.init();
    }
}