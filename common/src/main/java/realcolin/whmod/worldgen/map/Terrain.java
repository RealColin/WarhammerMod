package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import realcolin.whmod.WHRegistries;

public record Terrain(DensityFunction height) {
    public static final Codec<Terrain> DIRECT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(Terrain::height)
            ).apply(instance, Terrain::new));

    public static final Codec<Holder<Terrain>> CODEC = RegistryFileCodec.create(WHRegistries.TERRAIN, DIRECT_CODEC);
}
