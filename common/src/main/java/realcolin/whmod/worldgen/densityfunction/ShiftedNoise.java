package realcolin.whmod.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jetbrains.annotations.NotNull;

public record ShiftedNoise(NoiseHolder noise,
                           double xScale,
                           double yScale,
                           double zScale,
                           DensityFunction shiftX,
                           DensityFunction shiftY,
                           DensityFunction shiftZ) implements DensityFunction {

    public static final MapCodec<ShiftedNoise> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NoiseHolder.CODEC.fieldOf("noise").forGetter(ShiftedNoise::noise),
            Codec.DOUBLE.fieldOf("x_scale").forGetter(ShiftedNoise::xScale),
            Codec.DOUBLE.fieldOf("y_scale").forGetter(ShiftedNoise::yScale),
            Codec.DOUBLE.fieldOf("z_scale").forGetter(ShiftedNoise::zScale),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ShiftedNoise::shiftX),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(ShiftedNoise::shiftY),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ShiftedNoise::shiftZ)
    ).apply(instance, ShiftedNoise::new));

    @Override
    public double compute(FunctionContext functionContext) {
        double d0 = (double)functionContext.blockX() * this.xScale + this.shiftX.compute(functionContext);
        double d1 = (double)functionContext.blockY() * this.yScale + this.shiftY.compute(functionContext);
        double d2 = (double)functionContext.blockZ() * this.zScale + this.shiftZ.compute(functionContext);
        return this.noise.getValue(d0, d1, d2);
    }

    @Override
    public void fillArray(double @NotNull [] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ShiftedNoise(visitor.visitNoise(this.noise), this.xScale, this.yScale, this.zScale, this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return -maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }
}
