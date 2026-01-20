package realcolin.whmod.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.util.Pair;
import realcolin.whmod.worldgen.map.MapField;
import realcolin.whmod.worldgen.map.Terrain;
import realcolin.whmod.worldgen.map.WorldMap;

import java.util.HashMap;

public class MapSamplerWithBlending implements DensityFunction.SimpleFunction {

    public static final MapCodec<MapSamplerWithBlending> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WorldMap.CODEC.fieldOf("map").forGetter(src -> src.map),
            MapField.CODEC.fieldOf("field").forGetter(src -> src.field)
    ).apply(instance, MapSamplerWithBlending::new));

    private final Holder<WorldMap> map;
    private final MapField field;
    private final HashMap<Terrain, DensityFunction> functions;

    private final HashMap<Pair, Double> cache = new HashMap<>();

    public MapSamplerWithBlending(Holder<WorldMap> map, MapField field) {
        this(map, field, null);
    }

    public MapSamplerWithBlending(Holder<WorldMap> map, MapField field, HashMap<Terrain, DensityFunction> functions) {
        this.map = map;
        this.field = field;
        this.functions = functions;
    }

    @Override
    public double compute(FunctionContext functionContext) {
        return 0;
    }

    @Override
    public double minValue() {
        return -5;
    }

    @Override
    public double maxValue() {
        return 5;
    }

    @Override
    public @NotNull DensityFunction mapAll(@NotNull Visitor v) {
        var terrains = map.value().getTerrains();
        var tmpFuncs = new HashMap<Terrain, DensityFunction>();

        for (var t : terrains) {
            var fn = field.read(t).mapAll(v);
            tmpFuncs.put(t, fn);
        }

        return v.apply(new MapSampler(map, field, tmpFuncs));
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return null;
    }
}
