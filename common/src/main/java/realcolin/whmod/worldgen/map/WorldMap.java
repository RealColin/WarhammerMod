package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import realcolin.whmod.WHRegistries;

public class WorldMap {

    public static final Codec<WorldMap> DIRECT_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("image").forGetter(src -> src.imageLoc),
                    Codec.INT.fieldOf("resolution").forGetter(src -> src.resolution)
            ).apply(inst, WorldMap::new));

    public static final Codec<Holder<WorldMap>> CODEC = RegistryFileCodec.create(WHRegistries.MAP, DIRECT_CODEC);

    private final ResourceLocation imageLoc;
    private final int resolution;

    public WorldMap(ResourceLocation imageLoc, int resolution) {
        this.imageLoc = imageLoc;
        this.resolution = resolution;
    }
}
