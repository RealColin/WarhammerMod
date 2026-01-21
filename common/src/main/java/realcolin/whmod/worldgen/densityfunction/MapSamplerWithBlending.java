package realcolin.whmod.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import realcolin.whmod.Constants;
import realcolin.whmod.util.Pair;
import realcolin.whmod.worldgen.map.MapField;
import realcolin.whmod.worldgen.map.Terrain;
import realcolin.whmod.worldgen.map.WorldMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public double compute(FunctionContext fnc) {
        var p = new Pair(fnc.blockX(), fnc.blockZ());
        if (cache.containsKey(p))
            return cache.get(p);

        var cell = map.value().getCellAt(p.a(), p.b());
        var colors = cell.getDistTransforms().keySet();

        var nearbyRegions = new ArrayList<RegionWeight>();

        for (var c : colors) {
            var arr = cell.getDistTransforms().get(c);
            int ox = Math.floorMod(p.a(), Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);
            int oz = Math.floorMod(p.b(), Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);
            var dist = arr[ox][oz];

            var terrain = map.value().getTerrainFromColor(c);
            var range = Math.min(terrain.blendRange(), Constants.BLEND_RANGE);

            if (dist <= range) {
                var weight = calculateWeight(dist, range);
                var finalWeight = weight * terrain.blendWeight();
                nearbyRegions.add(new RegionWeight(terrain, dist, finalWeight));
            }
        }

        if (nearbyRegions.size() == 1) {
            var ter = nearbyRegions.getFirst().terrain();
            var val = functions.getOrDefault(ter, field.read(ter)).compute(fnc);
            cache.put(p, val);
            return val;
        }

        var totalWeight = nearbyRegions.stream().mapToDouble(RegionWeight::weight).sum();
        var blendedValue = 0.0;

        for (var region : nearbyRegions) {
            var ter = region.terrain();
            var func = functions.getOrDefault(ter, field.read(ter));
            var val = func.compute(fnc);
            blendedValue += val * (region.weight() / totalWeight);
        }

        cache.put(p, blendedValue);
        return blendedValue;
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

        return v.apply(new MapSamplerWithBlending(map, field, tmpFuncs));
    }

    private double calculateWeight(double distance, double range) {
        if (distance == 0) return 1.0;
        if (distance >= range) return 0.0;

        double normalized = distance / range;
        return 1.0 - smoothstep(normalized);
    }

    private double smoothstep(double x) {
        return x * x * (3.0 - 2.0 * x);
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }

    record RegionWeight(Terrain terrain, double distance, double weight) {}
}
