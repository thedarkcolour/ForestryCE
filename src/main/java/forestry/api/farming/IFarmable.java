/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.farming;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IFarmable describes a farmable resource, including its sapling/premature and mature versions, its saplings/seeds,
 * and the harvested resources resulting from harvesting and collecting from windfall.
 */
public interface IFarmable {
	/**
	 * @return true if the block at the given location is a "sapling" for this type, i.e. a non-harvestable immature version of the crop.
	 */
	boolean isSaplingAt(Level level, BlockPos pos, BlockState state);

	/**
	 * Used by farms to get crops of this farmable if the block at the position matches.
	 *
	 * @param level The world.
	 * @param pos   The position to check for a mature crop. Can be different than the base position.
	 * @param state The block state at the position.
	 * @return {@link ICrop} if the block at the given location is a harvestable and mature crop, null otherwise.
	 */
	@Nullable
	ICrop getCropAt(Level level, BlockPos pos, BlockState state);

	/**
	 * Used by the farm to check if an item can be used to plant this farmable.
	 *
	 * @param stack A potential item to be input into the farm.
	 *
	 * @return {@code true} if the item is a valid seed/sapling/etc. for this farmable.
	 */
	boolean isGermling(ItemStack stack);

	/**
	 * Used by JEI to display the seeds/saplings/etc. of this farmable.
	 *
	 * @param accumulator Accepts new item stacks for germlings.
	 */
	default void addGermlings(Consumer<ItemStack> accumulator) {
	}

	/**
	 * Used by JEI to display the products of this farmable.
	 *
	 * @param accumulator Accepts new item stacks for products.
	 */
	default void addProducts(Consumer<ItemStack> accumulator) {
	}

	/**
	 * Used by the farm to pickup nearby products that might have fallen from decayed leaves.
	 * For example, tree farms typically only harvest the trunks, which means that as the leaves decay, items will
	 * spawn that need to be collected by the farm as products of the tree.
	 *
	 * @return {@code true} if the item should be collected as windfall.
	 */
	boolean isWindfall(ItemStack stack);

	/**
	 * Plants a sapling by manipulating the world. The {@link IFarmLogic} should have verified the given location as valid. Called by the {@link IFarmHousing}
	 * which handles resources.
	 *
	 * @return {@code true} if a sapling was planted, false otherwise.
	 */
	boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos);
}
