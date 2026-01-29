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

        return newCompute(fnc);
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
        if (functions != null) {
            var remapped = new HashMap<Terrain, DensityFunction>();
            for (var e : functions.entrySet()) {
                remapped.put(e.getKey(), e.getValue().mapAll(v));
            }

            return v.apply(new MapSamplerWithBlending(map, field, remapped));
        }

        var terrains = map.value().getTerrains();
        var tmpFuncs = new HashMap<Terrain, DensityFunction>();

        for (var th : terrains) {
            var t = th.value();
            var fn = field.read(t).mapAll(v);

            tmpFuncs.put(t, fn);
        }

        return v.apply(new MapSamplerWithBlending(this.map, this.field, tmpFuncs));
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }

    public double newCompute(FunctionContext fnc) {
        var p = new Pair(fnc.blockX(), fnc.blockZ());
        if (cache.containsKey(p))
            return cache.get(p);

        var worldMap = map.value();
        var cell = worldMap.getCellAt(p.a(), p.b());
        var currentTerrain = worldMap.getTerrainFromColor(cell.getColorAt(p.a(), p.b()));
        var colors = cell.getDistTransforms().keySet();

        var landRegions = new ArrayList<RegionWeight>();
        var waterRegions = new ArrayList<RegionWeight>();

        for (var c : colors) {
            var arr = cell.getDistTransforms().get(c);
            int ox = Math.floorMod(p.a(), Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);
            int oz = Math.floorMod(p.b(), Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);
            var dist = arr[ox][oz];

            var terrain = worldMap.getTerrainFromColor(c);
            var range = Math.min(terrain.blendRange(), Constants.BLEND_RANGE);

            if (dist <= range) {
                var weight = calculateWeight(dist, range);
                var finalWeight = weight * terrain.blendWeight();
                var regionWeight = new RegionWeight(terrain, dist, finalWeight);
                if (terrain.isWater())
                    waterRegions.add(regionWeight);
                else
                    landRegions.add(regionWeight);
            }
        }

        var landWeight = landRegions.stream().mapToDouble(RegionWeight::weight).sum();
        var waterWeight = waterRegions.stream().mapToDouble(RegionWeight::weight).sum();
        var blendedValue = 0.0;


        if (!currentTerrain.isWater()) {
            // apply weights from land regions
            for (var region : landRegions) {
                var ter = region.terrain();
                var func = functions.get(ter);

                var val = func.compute(fnc);

                blendedValue += val * (region.weight() / landWeight);
            }

            // get distance to nearest water region
            var distToWater = Constants.BLEND_RANGE;
            RegionWeight weight = null;

            for (var region : waterRegions) {
                if (region.distance() < distToWater) {
                    distToWater = region.distance();
                    weight = region;
                }
            }

            var d = Constants.BLEND_RANGE;
            if (weight != null) {
                d = weight.terrain().blendRange();
            }

            // blend height from h down to 0.0 from d=50 to d=0
            blendedValue = lerp(distToWater, d, 0.0, blendedValue, 0.0);


        } else {
            // do the same thing but vice versa
            for (var region : waterRegions) {
                var ter = region.terrain();
                var func = functions.get(ter);
                var val = func.compute(fnc);
                blendedValue += val * (region.weight() / waterWeight);
            }

            var distToLand = Constants.BLEND_RANGE;
            for (var region : landRegions) {
                distToLand = Math.min(distToLand, region.distance());
            }

            blendedValue = lerp(distToLand, Constants.BLEND_RANGE, 0.0, blendedValue, 0.0);
        }


        cache.put(p, blendedValue);
        return blendedValue;
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

    private double lerp(double val,  double A, double B, double C, double D) {
        return C + ((val - A) / (B - A)) * (D - C);
    }

    record RegionWeight(Terrain terrain, double distance, double weight) {}
}
