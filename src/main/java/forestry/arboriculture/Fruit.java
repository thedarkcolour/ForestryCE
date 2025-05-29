package forestry.arboriculture;

import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Fruit extends DummyFruit {
	private final List<IProduct> products;
	protected final int ripeningPeriod;

	public Fruit(boolean dominant, int ripeningPeriod, List<IProduct> products) {
		super(dominant);

		this.products = List.copyOf(products);
		this.ripeningPeriod = ripeningPeriod;
	}

	@Override
	public List<ItemStack> getFruits(IGenome genome, Level level, int ripeningTime) {
		RandomSource rand = level.random;

		if (ripeningTime >= this.ripeningPeriod) {
			ArrayList<ItemStack> stacks = new ArrayList<>(this.products.size());

			for (IProduct product : this.products) {
				if (product.chance() == 1.0f || product.chance() < level.random.nextFloat()) {
					stacks.add(product.createRandomStack(rand));
				}
			}

			return stacks;
		}

		return List.of();
	}

	@Override
	public List<IProduct> getProducts() {
		return this.products;
	}
}
