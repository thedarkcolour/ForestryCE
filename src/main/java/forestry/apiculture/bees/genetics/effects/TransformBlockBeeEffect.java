package forestry.apiculture.bees.genetics.effects;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.TemperatureType;
import forestry.api.core.genetics.IEffectData;
import forestry.api.core.genetics.IGenome;
import forestry.apiculture.bees.genetics.Bee;
import forestry.apiculture.bees.genetics.BlockMatcher;
import forestry.core.platform.util.VecUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * The {@code forestry:transform_block} primitive: samples {@code attempts} positions in the housing's territory and
 * rewrites every sampled block matching a transform rule.
 * <p>
 * Sampling uses {@link Bee#getParticleArea}, which is where the built-ins expressed through this primitive have always
 * sampled from. They are ground-targeting, so a symmetric box centered on the housing would silently make them far
 * more effective.
 */
public class TransformBlockBeeEffect extends ThrottledBeeEffect {
	/** The state a transform writes, given the state currently at the sampled position. */
	public sealed interface To {
		Codec<To> CODEC = Codec.either(SetProperties.CODEC, Fixed.CODEC)
			.xmap(either -> either.map(set -> (To) set, fixed -> (To) fixed),
				to -> to instanceof SetProperties set ? Either.left(set) : Either.right((Fixed) to));

		BlockState apply(BlockState current);

		/** A fixed state: {@code "to": {"Name": "minecraft:ice"}}. */
		record Fixed(BlockState state) implements To {
			public static final Codec<Fixed> CODEC = BlockState.CODEC.xmap(Fixed::new, Fixed::state);

			@Override
			public BlockState apply(BlockState current) {
				return this.state;
			}
		}

		/**
		 * A property mutation of the matched state: {@code "to": {"set": {"berries": "true"}}}. Every other property
		 * of the current state is preserved. A property the matched block does not have, or a value it cannot parse,
		 * leaves the state untouched — the caller's identity guard then skips the write rather than rewriting the
		 * block with itself.
		 */
		record SetProperties(Map<String, String> properties) implements To {
			public static final Codec<SetProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("set").forGetter(SetProperties::properties)
			).apply(instance, SetProperties::new));

			@Override
			public BlockState apply(BlockState current) {
				StateDefinition<Block, BlockState> definition = current.getBlock().getStateDefinition();
				BlockState state = current;
				for (Map.Entry<String, String> entry : this.properties.entrySet()) {
					Property<?> property = definition.getProperty(entry.getKey());
					if (property != null) {
						state = setValue(state, property, entry.getValue());
					}
				}
				return state;
			}

			private static <T extends Comparable<T>> BlockState setValue(BlockState state, Property<T> property, String value) {
				return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
			}
		}
	}

	/** A single from&rarr;to replacement rule. */
	public record Transform(BlockMatcher from, To to, boolean requiresAirAbove) {
		public static final Codec<Transform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockMatcher.CODEC.fieldOf("from").forGetter(Transform::from),
			To.CODEC.fieldOf("to").forGetter(Transform::to),
			Codec.BOOL.optionalFieldOf("requires_air_above", false).forGetter(Transform::requiresAirAbove)
		).apply(instance, Transform::new));
	}

	public static final MapCodec<TransformBlockBeeEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ThrottleSettings.codec(20, false, false).forGetter(ThrottledBeeEffect::settings),
		Transform.CODEC.listOf().fieldOf("transforms").forGetter(effect -> effect.transforms),
		Codec.intRange(1, 64).optionalFieldOf("attempts", 1).forGetter(effect -> effect.attempts),
		Codec.floatRange(0f, 1f).optionalFieldOf("chance", 0.06f).forGetter(effect -> effect.chance),
		// An upper bound only. A range would be more expressive, but GLACIAL is the only consumer and only needs a
		// ceiling; widening this to a range is a follow-up.
		TemperatureType.CODEC.optionalFieldOf("max_temperature").forGetter(effect -> effect.maxTemperature)
	).apply(instance, TransformBlockBeeEffect::new));

	private final List<Transform> transforms;
	private final int attempts;
	private final float chance;
	private final Optional<TemperatureType> maxTemperature;

	public TransformBlockBeeEffect(ThrottleSettings settings, List<Transform> transforms, int attempts, float chance, Optional<TemperatureType> maxTemperature) {
		super(settings);
		this.transforms = transforms;
		this.attempts = attempts;
		this.chance = chance;
		this.maxTemperature = maxTemperature;
	}

	@Override
	public MapCodec<TransformBlockBeeEffect> codec() {
		return MAP_CODEC;
	}

	public List<Transform> transforms() {
		return this.transforms;
	}

	public int attempts() {
		return this.attempts;
	}

	public float chance() {
		return this.chance;
	}

	public Optional<TemperatureType> maxTemperature() {
		return this.maxTemperature;
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getLevel();
		if (level.isClientSide) {
			return storedData;
		}
		// Inclusive: the activation is skipped only when the housing is strictly warmer than the bound.
		if (this.maxTemperature.isPresent() && !housing.temperature().isCoolerOrEqual(this.maxTemperature.get())) {
			return storedData;
		}
		RandomSource rand = level.random;
		// Skip the RNG draw entirely when chance is 1 so a guaranteed effect does not perturb the shared world RNG
		// state.
		if (this.chance < 1.0f && rand.nextFloat() >= this.chance) {
			return storedData;
		}

		Vec3i area = Bee.getParticleArea(genome, housing);
		BlockPos center = housing.getBlockPos().offset(VecUtil.center(area));

		for (int i = 0; i < this.attempts; i++) {
			BlockPos pos = VecUtil.getRandomPositionInArea(rand, area).offset(center);
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			BlockState current = level.getBlockState(pos);
			for (Transform transform : this.transforms) {
				if (!transform.from().matches(current)) {
					continue;
				}
				if (transform.requiresAirAbove() && !level.isEmptyBlock(pos.above())) {
					break;
				}
				BlockState next = transform.to().apply(current);
				// Identity guard. Block states are interned, so reference equality is state equality. Skipping the
				// no-op write also avoids firing spurious neighbour updates (observers, redstone) on a rewrite that
				// changes nothing.
				if (next != current) {
					level.setBlockAndUpdate(pos, next);
				}
				break;
			}
		}

		return storedData;
	}
}
