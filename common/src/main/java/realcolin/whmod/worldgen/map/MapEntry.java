package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

@SuppressWarnings("ClassEscapesDefinedScope")
public record MapEntry(String region, Integer color, Holder<Terrain> terrain, List<BiomePair> biomes) {

    public static final Codec<List<BiomePair>> BIOMES_CODEC =
            RecordCodecBuilder.<BiomePair>create(instance -> instance.group(
                    Biome.CODEC.fieldOf("biome").forGetter(BiomePair::biome),
                    Codec.INT.fieldOf("weight").forGetter(BiomePair::weight)
            ).apply(instance, BiomePair::new)).listOf();

    public static final Codec<List<MapEntry>> ENTRIES_CODEC =
            RecordCodecBuilder.<MapEntry>create(instance -> instance.group(
                    Codec.STRING.fieldOf("region").forGetter(MapEntry::region),
                    Codec.INT.fieldOf("color").forGetter(MapEntry::color),
                    Terrain.CODEC.fieldOf("terrain").forGetter(MapEntry::terrain),
                    BIOMES_CODEC.fieldOf("biomes").forGetter(MapEntry::biomes)
            ).apply(instance, MapEntry::new)).listOf();

    record BiomePair(Holder<Biome> biome, Integer weight) {}
}
