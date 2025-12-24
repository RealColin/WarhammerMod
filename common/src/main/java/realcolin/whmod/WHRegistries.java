package realcolin.whmod;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import realcolin.whmod.worldgen.map.WorldMap;

public class WHRegistries {
    public static final ResourceKey<Registry<WorldMap>> MAP = ResourceKey.createRegistryKey(ResourceLocation.parse("worldgen/map"));
}
