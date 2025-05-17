/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.VanillaWoodType;

/**
 * Provides easy access to Forestry and Vanilla wood items.
 * Forestry wood blocks have the same block state properties as vanilla ones.
 * Note that all doors are fireproof (even vanilla).
 *
 * @see WoodBlockKind
 * @see ForestryWoodType
 * @see VanillaWoodType
 */
// todo merge with ITreeManager in 1.21
public interface IWoodAccess {
	ItemStack getStack(IWoodType woodType, WoodBlockKind kind, boolean fireproof);

	BlockState getBlock(IWoodType woodType, WoodBlockKind kind, boolean fireproof);

	/**
	 * @since 2.6.0
	 */
	TagKey<Block> getLogBlockTag(IWoodType kind, boolean fireproof);

	/**
	 * @since 2.6.0
	 */
	TagKey<Item> getLogItemTag(IWoodType kind, boolean fireproof);

	List<IWoodType> getRegisteredWoodTypes();

	/**
	 * Call this after item registry to register the blocks/items for your wood type.
	 *
	 * @param woodType      The type of wood, ex. Oak or Teak
	 * @param woodBlockKind The kind of wood block, ex. Planks or Fence
	 * @param fireproof     Whether this is for the fireproof variant of the wood block kind (ignored in the case of non-burnable wood blocks)
	 * @param blockState    The default block state of the Planks/Fence/etc. for the given wood type
	 * @param itemStack     Supplier for the item form of Planks/Fence/etc. for the given wood type
	 */
	void register(IWoodType woodType, WoodBlockKind woodBlockKind, boolean fireproof, BlockState blockState, Supplier<Item> itemStack);

	/**
	 * Call this after item registry to register the block tags used by your wood type.
	 *
	 * @param woodType    The type of wood, ex. Oak or Teak
	 * @param fireproof   Whether these tags are for the fireproof logs or the regular logs
	 * @param logBlockTag The block tag for logs of this wood type
	 * @param logItemTag  The item tag for logs of this wood type
	 * @since 2.6.0
	 */
	void registerLogTag(IWoodType woodType, boolean fireproof, TagKey<Block> logBlockTag, TagKey<Item> logItemTag);
}
