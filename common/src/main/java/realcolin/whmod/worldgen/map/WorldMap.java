package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import realcolin.whmod.Constants;
import realcolin.whmod.WHRegistries;

public class WorldMap {

    public static final Codec<WorldMap> DIRECT_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("image").forGetter(src -> src.imageLoc),
                    Codec.INT.fieldOf("resolution").forGetter(src -> src.resolution),
                    Biome.CODEC.fieldOf("default_biome").forGetter(src -> src.defaultBiome)
            ).apply(inst, WorldMap::new));

    public static final Codec<Holder<WorldMap>> CODEC = RegistryFileCodec.create(WHRegistries.MAP, DIRECT_CODEC);

    private final ResourceLocation imageLoc;
    private final int resolution;
    private final Holder<Biome> defaultBiome;

    public WorldMap(ResourceLocation imageLoc, int resolution, Holder<Biome> defaultBiome) {
        this.imageLoc = imageLoc;
        this.resolution = resolution;
        this.defaultBiome = defaultBiome;

        Constants.LOG.info("Initialized a WorldMap instance");
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }
}
