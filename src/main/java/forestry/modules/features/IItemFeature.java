package forestry.modules.features;

import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import forestry.api.core.IItemProvider;

public interface IItemFeature<I extends Item> extends IModFeature, IItemProvider<I>, net.minecraft.world.level.ItemLike, Supplier<I> {
	@Override
	default Item asItem() {
		return item();
	}

	@Override
	default I get() {
		return item();
	}

	Holder<Item> holder();
}
