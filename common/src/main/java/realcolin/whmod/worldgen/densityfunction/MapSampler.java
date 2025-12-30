package realcolin.whmod.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.util.Pair;
import realcolin.whmod.worldgen.map.Terrain;
import realcolin.whmod.worldgen.map.WorldMap;

import java.util.HashMap;

@SuppressWarnings("ClassEscapesDefinedScope")
public class MapSampler implements DensityFunction.SimpleFunction {
    public static final MapCodec<MapSampler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WorldMap.CODEC.fieldOf("map").forGetter(src -> src.map),
            MapField.CODEC.fieldOf("field").forGetter(src -> src.field)
    ).apply(instance, MapSampler::new));

    private final Holder<WorldMap> map;
    private final MapField field;
    private final HashMap<Terrain, DensityFunction> functions;

    private final HashMap<Pair, Double> cache = new HashMap<>();


    public MapSampler(Holder<WorldMap> map, MapField field) {
        this(map, field, null);
    }

    public MapSampler(Holder<WorldMap> map, MapField field, HashMap<Terrain, DensityFunction> functions) {
        this.map = map;
        this.field = field;
        this.functions = functions;
    }

    @Override
    public double compute(@NotNull FunctionContext fnc) {
        var p = new Pair(fnc.blockX(), fnc.blockZ());
        if (cache.containsKey(p))
            return cache.get(p);

        var terrain = map.value().getTerrain(p.a(), p.b());

        if (functions.containsKey(terrain)) {
            var val = functions.get(terrain).compute(fnc);
            cache.put(p, val);
            return val;
        }

        // TODO reminder why i need this
        var val = field.read(terrain).compute(fnc);
        cache.put(p, val);
        return val;
    }

    @Override
    public double minValue() {
        return -5.0;
    }

    @Override
    public double maxValue() {
        return 5.0;
    }

    @Override
    public DensityFunction mapAll(Visitor v) {
        var terrains = map.value().getTerrains();
        var tmpFuncs = new HashMap<Terrain, DensityFunction>();

        for (var t : terrains) {
            var fn = field.read(t).mapAll(v);
            tmpFuncs.put(t, fn);
        }

        return v.apply(new MapSampler(map, field, tmpFuncs));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }

    enum MapField implements StringRepresentable {
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

        abstract DensityFunction read(Terrain terrain);
    }
}
