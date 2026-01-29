package realcolin.whmod.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.util.Pair;
import realcolin.whmod.worldgen.map.Terrain;
import realcolin.whmod.worldgen.map.WorldMap;
import realcolin.whmod.worldgen.map.MapField;

import java.util.HashMap;
import java.util.stream.Collectors;

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
        var func = functions.get(terrain);
        var val = func.compute(fnc);

        if (terrain.name().equals("mountains")) {
            if (val != 0.0) {
//                System.out.println(val);
            }

//            System.out.println("Mountains ctx: " + fnc.getClass().getName());
//            System.out.println("Mountains func: " + func);
        }

        cache.put(p, val);
        return val;
    }

//    @Override
//    public void fillArray(double[] toFill, ContextProvider ctxProvider) {
//        for (int i = 0; i < toFill.length; i++) {
//            toFill[i] = compute(ctxProvider.forIndex(i));
//        }
//    }

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
        if (functions != null) {
            var remapped = new HashMap<Terrain, DensityFunction>();
            for (var e : functions.entrySet()) {
                remapped.put(e.getKey(), e.getValue().mapAll(v));
            }

            return v.apply(new MapSampler(map, field, remapped));
        }

        var terrains = map.value().getTerrains();
        var tmpFuncs = new HashMap<Terrain, DensityFunction>();

        for (var th : terrains) {
            var t = th.value();
            var original = field.read(t);
            var fn = original.mapAll(v);

//            if (t.name().equals("mountains")) {
//                System.out.println(fn);
//            }

            tmpFuncs.put(t, fn);
        }

        var ret = v.apply(new MapSampler(map, field, tmpFuncs));
//        if (ret instanceof MapSampler s) {
//            System.out.println("Funcs:");
//            s.functions.values().forEach(System.out::println);
//        }

        return ret;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }
}
