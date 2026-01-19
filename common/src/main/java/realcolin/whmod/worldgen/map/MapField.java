package realcolin.whmod.worldgen.map;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.worldgen.densityfunction.MapSampler;

public enum MapField implements StringRepresentable {
    HEIGHT("height") {
        @Override
        public DensityFunction read(Terrain terrain) {
            return terrain.height();
        }
    };

    public static final Codec<MapField> CODEC = StringRepresentable.fromEnum(MapField::values);
    private final String name;

    MapField(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public abstract DensityFunction read(Terrain terrain);
}
