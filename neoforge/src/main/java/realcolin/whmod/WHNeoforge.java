package realcolin.whmod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import realcolin.whmod.block.FlammableRotatedPillarBlock;
import realcolin.whmod.worldgen.biome.WHBiomeSource;
import realcolin.whmod.worldgen.densityfunction.MapSampler;
import realcolin.whmod.worldgen.densityfunction.MapSamplerWithBlending;
import realcolin.whmod.worldgen.densityfunction.Noise;
import realcolin.whmod.worldgen.densityfunction.ShiftedNoise;
import realcolin.whmod.worldgen.map.Terrain;
import realcolin.whmod.worldgen.map.WorldMap;

@Mod(Constants.MOD_ID)
public class WHNeoforge {

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, Constants.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, Constants.MOD_ID);

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.createBlocks(Constants.MOD_ID);

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(Constants.MOD_ID);

    public WHNeoforge(IEventBus eventBus) {
        Constants.LOG.info("Warhammer NeoForge loaded");
        WarhammerMod.init();

        BIOME_SOURCES.register(Constants.MAP_BIOME_SOURCE_ID, () -> WHBiomeSource.CODEC);
        BIOME_SOURCES.register(eventBus);

        DENSITY_FUNCTIONS.register("noise", () -> Noise.CODEC);
        DENSITY_FUNCTIONS.register("shifted_noise", () -> ShiftedNoise.CODEC);
        DENSITY_FUNCTIONS.register("map_sampler", () -> MapSampler.CODEC);
        DENSITY_FUNCTIONS.register("blended_map_sampler", () -> MapSamplerWithBlending.CODEC);
        DENSITY_FUNCTIONS.register(eventBus);

        DeferredHolder<Block, Block> PINE_LOG =  BLOCKS.register("pine_log", () -> new FlammableRotatedPillarBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(p_152624_ -> p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)
                        .ignitedByLava()));

        ITEMS.register("pine_log", () -> new BlockItem(PINE_LOG.get(), new Item.Properties()));

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);

        eventBus.addListener(WHNeoforge::registerData);
    }

    public static void registerData(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(WHRegistries.TERRAIN, Terrain.DIRECT_CODEC);
        event.dataPackRegistry(WHRegistries.MAP, WorldMap.DIRECT_CODEC);
    }
}