package realcolin.whmod.worldgen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.worldgen.map.WorldMap;

import java.util.List;
import java.util.stream.Stream;

public class WHBiomeSource extends BiomeSource {

    public static final MapCodec<WHBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    WorldMap.CODEC.fieldOf("map").forGetter(src -> src.map)
            ).apply(inst, inst.stable(WHBiomeSource::new)));

    private final Holder<WorldMap> map;

    public WHBiomeSource(Holder<WorldMap> map) {
        this.map = map;
    }

    @Override
    protected @NotNull MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(map.value().getDefaultBiome());
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int i1, int i2, Climate.Sampler sampler) {
        return map.value().getDefaultBiome();
    }
}
