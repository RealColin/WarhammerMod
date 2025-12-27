package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import realcolin.whmod.Constants;
import realcolin.whmod.WHRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldMap {

    public static final Codec<WorldMap> DIRECT_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("image").forGetter(src -> src.imageLoc),
                    Codec.INT.fieldOf("resolution").forGetter(src -> src.resolution),
                    Biome.CODEC.fieldOf("default_biome").forGetter(src -> src.defaultBiome),
                    MapEntry.ENTRIES_CODEC.fieldOf("entries").forGetter(src -> src.entries)
            ).apply(inst, WorldMap::new));

    public static final Codec<Holder<WorldMap>> CODEC = RegistryFileCodec.create(WHRegistries.MAP, DIRECT_CODEC);

    private final ResourceLocation imageLoc;
    private final int resolution;
    private final Holder<Biome> defaultBiome;
    private final List<MapEntry> entries;

    public WorldMap(ResourceLocation imageLoc, int resolution, Holder<Biome> defaultBiome, List<MapEntry> entries) {
        this.imageLoc = imageLoc;
        this.resolution = resolution;
        this.defaultBiome = defaultBiome;
        this.entries = entries;

        Constants.LOG.info("Initialized a WorldMap instance");
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }

    public Set<Holder<Biome>> getAllBiomes() {
        var set = new HashSet<Holder<Biome>>();
        set.add(defaultBiome);

        for (var e : entries) {
            set.addAll(e.biomes().stream().map(MapEntry.BiomePair::biome).toList());
        }

        return set;
    }
}
