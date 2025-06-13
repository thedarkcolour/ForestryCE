package forestry.modules.features;

import forestry.api.core.IBlockProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Collections;

public interface IBlockFeature<B extends Block, I extends BlockItem> extends IItemFeature<I>, IBlockProvider<B, I> {
	@Override
	default Collection<B> collect() {
		return Collections.singleton(block());
	}

	@SuppressWarnings("unchecked")
	default <T extends Block> T cast() {
		return (T) block();
	}

	BlockState defaultState();

	<V extends Comparable<V>> BlockState setValue(Property<V> property, V value);

	@Override
	Holder<Item> holder();
}
