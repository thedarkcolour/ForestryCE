package forestry.apiculture.genetics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import forestry.api.core.IProduct;
import forestry.api.core.ProductType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.List;

// used by secret Patriotic bee species
public record FireworkProduct(float chance) implements IProduct {
	private static final DyeColor[] COLORS = {DyeColor.RED, DyeColor.WHITE, DyeColor.BLUE};
	private static final FireworkExplosion.Shape[] SHAPES = FireworkExplosion.Shape.values();

	public static final MapCodec<FireworkProduct> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(FireworkProduct::chance)
	).apply(instance, FireworkProduct::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FireworkProduct> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, FireworkProduct::chance,
		FireworkProduct::new
	);
	public static final ProductType<FireworkProduct> TYPE = new ProductType<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public ProductType<?> type() {
		return TYPE;
	}

	@Override
	public Item item() {
		return Items.FIREWORK_ROCKET;
	}

	@Override
	public ItemStack createStack() {
		return new ItemStack(Items.FIREWORK_ROCKET);
	}

	@Override
	public ItemStack createRandomStack(RandomSource random) {
		ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);

		// 1.21 stores fireworks via the FIREWORKS data component instead of NBT tags.
		DyeColor color = COLORS[random.nextInt(COLORS.length)];
		FireworkExplosion.Shape shape = SHAPES[random.nextInt(SHAPES.length)];
		FireworkExplosion explosion = new FireworkExplosion(
			shape,
			IntList.of(color.getFireworkColor()),
			IntList.of(),
			false,
			random.nextBoolean()
		);

		firework.set(DataComponents.FIREWORKS, new Fireworks(2, List.of(explosion)));
		return firework;
	}
}
