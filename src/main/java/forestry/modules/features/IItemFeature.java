package forestry.modules.features;

import forestry.api.core.IItemProvider;
import forestry.api.core.IItemSubtype;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public interface IItemFeature<I extends Item> extends IModFeature, IItemProvider<I>, net.minecraft.world.level.ItemLike, Supplier<I> {
	@Override
	default Item asItem() {
		return item();
	}

	@Override
	default I get() {
		return item();
	}

	ResourceLocation id();
}
