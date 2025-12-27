package realcolin.whmod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import realcolin.whmod.worldgen.biome.WHBiomeSource;
import realcolin.whmod.worldgen.map.WorldMap;

@Mod(Constants.MOD_ID)
public class WHNeoforge {

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, Constants.MOD_ID);

    public WHNeoforge(IEventBus eventBus) {
        Constants.LOG.info("Warhammer NeoForge loaded");
        WarhammerMod.init();

        BIOME_SOURCES.register(Constants.MAP_BIOME_SOURCE_ID, () -> WHBiomeSource.CODEC);
        BIOME_SOURCES.register(eventBus);

        eventBus.addListener(WHNeoforge::registerData);
    }

    public static void registerData(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(WHRegistries.MAP, WorldMap.DIRECT_CODEC);
    }
}