/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.arboriculture.IWoodAccess;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.modules.features.FeatureBlockGroup;

public enum WoodAccess implements IWoodAccess {
	INSTANCE;

	private final Map<WoodBlockKind, WoodMap> woodMaps = new EnumMap<>(WoodBlockKind.class);
	private final List<IWoodType> registeredWoodTypes = new ArrayList<>();

	WoodAccess() {
		for (WoodBlockKind woodBlockKind : WoodBlockKind.values()) {
			this.woodMaps.put(woodBlockKind, new WoodMap(woodBlockKind));
		}
		registerVanilla();
	}

	public <T extends Block & IWoodTyped> void registerFeatures(FeatureBlockGroup<? extends T, ? extends IWoodType> featureGroup, WoodBlockKind kind) {
		for (T block : featureGroup.getBlocks()) {
			registerWithoutVariants(block, kind);
		}
	}

	private void registerVanilla() {
		register(VanillaWoodType.OAK, WoodBlockKind.LOG, false, Blocks.OAK_LOG.defaultBlockState(), () -> Items.OAK_LOG);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.LOG, false, Blocks.SPRUCE_LOG.defaultBlockState(), () -> Items.SPRUCE_LOG);
		register(VanillaWoodType.BIRCH, WoodBlockKind.LOG, false, Blocks.BIRCH_LOG.defaultBlockState(), () -> Items.BIRCH_LOG);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.LOG, false, Blocks.JUNGLE_LOG.defaultBlockState(), () -> Items.JUNGLE_LOG);
		register(VanillaWoodType.ACACIA, WoodBlockKind.LOG, false, Blocks.ACACIA_LOG.defaultBlockState(), () -> Items.ACACIA_LOG);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.LOG, false, Blocks.DARK_OAK_LOG.defaultBlockState(), () -> Items.DARK_OAK_LOG);
		register(VanillaWoodType.CHERRY, WoodBlockKind.LOG, false, Blocks.CHERRY_LOG.defaultBlockState(), () -> Items.CHERRY_LOG);

		register(VanillaWoodType.OAK, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_OAK_LOG.defaultBlockState(), () -> Items.STRIPPED_OAK_LOG);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), () -> Items.STRIPPED_SPRUCE_LOG);
		register(VanillaWoodType.BIRCH, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState(), () -> Items.STRIPPED_BIRCH_LOG);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState(), () -> Items.STRIPPED_JUNGLE_LOG);
		register(VanillaWoodType.ACACIA, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_ACACIA_LOG.defaultBlockState(), () -> Items.STRIPPED_ACACIA_LOG);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), () -> Items.STRIPPED_DARK_OAK_LOG);
		register(VanillaWoodType.CHERRY, WoodBlockKind.STRIPPED_LOG, false, Blocks.STRIPPED_CHERRY_LOG.defaultBlockState(), () -> Items.STRIPPED_CHERRY_LOG);

		register(VanillaWoodType.OAK, WoodBlockKind.WOOD, false, Blocks.OAK_WOOD.defaultBlockState(), () -> Items.OAK_WOOD);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.WOOD, false, Blocks.SPRUCE_WOOD.defaultBlockState(), () -> Items.SPRUCE_WOOD);
		register(VanillaWoodType.BIRCH, WoodBlockKind.WOOD, false, Blocks.BIRCH_WOOD.defaultBlockState(), () -> Items.BIRCH_WOOD);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.WOOD, false, Blocks.JUNGLE_WOOD.defaultBlockState(), () -> Items.JUNGLE_WOOD);
		register(VanillaWoodType.ACACIA, WoodBlockKind.WOOD, false, Blocks.ACACIA_WOOD.defaultBlockState(), () -> Items.ACACIA_WOOD);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.WOOD, false, Blocks.DARK_OAK_WOOD.defaultBlockState(), () -> Items.DARK_OAK_WOOD);
		register(VanillaWoodType.CHERRY, WoodBlockKind.WOOD, false, Blocks.CHERRY_WOOD.defaultBlockState(), () -> Items.CHERRY_WOOD);

		register(VanillaWoodType.OAK, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), () -> Items.STRIPPED_OAK_WOOD);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), () -> Items.STRIPPED_SPRUCE_WOOD);
		register(VanillaWoodType.BIRCH, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_BIRCH_WOOD.defaultBlockState(), () -> Items.STRIPPED_BIRCH_WOOD);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_JUNGLE_WOOD.defaultBlockState(), () -> Items.STRIPPED_JUNGLE_WOOD);
		register(VanillaWoodType.ACACIA, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_ACACIA_WOOD.defaultBlockState(), () -> Items.STRIPPED_ACACIA_WOOD);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), () -> Items.STRIPPED_DARK_OAK_WOOD);
		register(VanillaWoodType.CHERRY, WoodBlockKind.STRIPPED_WOOD, false, Blocks.STRIPPED_CHERRY_WOOD.defaultBlockState(), () -> Items.STRIPPED_CHERRY_WOOD);

		register(VanillaWoodType.OAK, WoodBlockKind.PLANKS, false, Blocks.OAK_PLANKS.defaultBlockState(), () -> Items.OAK_PLANKS);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.PLANKS, false, Blocks.SPRUCE_PLANKS.defaultBlockState(), () -> Items.SPRUCE_PLANKS);
		register(VanillaWoodType.BIRCH, WoodBlockKind.PLANKS, false, Blocks.BIRCH_PLANKS.defaultBlockState(), () -> Items.BIRCH_PLANKS);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.PLANKS, false, Blocks.JUNGLE_PLANKS.defaultBlockState(), () -> Items.JUNGLE_PLANKS);
		register(VanillaWoodType.ACACIA, WoodBlockKind.PLANKS, false, Blocks.ACACIA_PLANKS.defaultBlockState(), () -> Items.ACACIA_PLANKS);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.PLANKS, false, Blocks.DARK_OAK_PLANKS.defaultBlockState(), () -> Items.DARK_OAK_PLANKS);
		register(VanillaWoodType.CHERRY, WoodBlockKind.PLANKS, false, Blocks.CHERRY_PLANKS.defaultBlockState(), () -> Items.CHERRY_PLANKS);

		register(VanillaWoodType.OAK, WoodBlockKind.SLAB, false, Blocks.OAK_SLAB.defaultBlockState(), () -> Items.OAK_SLAB);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.SLAB, false, Blocks.SPRUCE_SLAB.defaultBlockState(), () -> Items.SPRUCE_SLAB);
		register(VanillaWoodType.BIRCH, WoodBlockKind.SLAB, false, Blocks.BIRCH_SLAB.defaultBlockState(), () -> Items.BIRCH_SLAB);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.SLAB, false, Blocks.JUNGLE_SLAB.defaultBlockState(), () -> Items.JUNGLE_SLAB);
		register(VanillaWoodType.ACACIA, WoodBlockKind.SLAB, false, Blocks.ACACIA_SLAB.defaultBlockState(), () -> Items.ACACIA_SLAB);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.SLAB, false, Blocks.DARK_OAK_SLAB.defaultBlockState(), () -> Items.DARK_OAK_SLAB);
		register(VanillaWoodType.CHERRY, WoodBlockKind.SLAB, false, Blocks.CHERRY_SLAB.defaultBlockState(), () -> Items.CHERRY_SLAB);

		register(VanillaWoodType.OAK, WoodBlockKind.FENCE, false, Blocks.OAK_FENCE.defaultBlockState(), () -> Items.OAK_FENCE);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.FENCE, false, Blocks.SPRUCE_FENCE.defaultBlockState(), () -> Items.SPRUCE_FENCE);
		register(VanillaWoodType.BIRCH, WoodBlockKind.FENCE, false, Blocks.BIRCH_FENCE.defaultBlockState(), () -> Items.BIRCH_FENCE);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.FENCE, false, Blocks.JUNGLE_FENCE.defaultBlockState(), () -> Items.JUNGLE_FENCE);
		register(VanillaWoodType.ACACIA, WoodBlockKind.FENCE, false, Blocks.ACACIA_FENCE.defaultBlockState(), () -> Items.ACACIA_FENCE);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.FENCE, false, Blocks.DARK_OAK_FENCE.defaultBlockState(), () -> Items.DARK_OAK_FENCE);
		register(VanillaWoodType.CHERRY, WoodBlockKind.FENCE, false, Blocks.CHERRY_FENCE.defaultBlockState(), () -> Items.CHERRY_FENCE);

		register(VanillaWoodType.OAK, WoodBlockKind.FENCE_GATE, false, Blocks.OAK_FENCE_GATE.defaultBlockState(), () -> Items.OAK_FENCE_GATE);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.FENCE_GATE, false, Blocks.SPRUCE_FENCE_GATE.defaultBlockState(), () -> Items.SPRUCE_FENCE_GATE);
		register(VanillaWoodType.BIRCH, WoodBlockKind.FENCE_GATE, false, Blocks.BIRCH_FENCE_GATE.defaultBlockState(), () -> Items.BIRCH_FENCE_GATE);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.FENCE_GATE, false, Blocks.JUNGLE_FENCE_GATE.defaultBlockState(), () -> Items.JUNGLE_FENCE_GATE);
		register(VanillaWoodType.ACACIA, WoodBlockKind.FENCE_GATE, false, Blocks.ACACIA_FENCE_GATE.defaultBlockState(), () -> Items.ACACIA_FENCE_GATE);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.FENCE_GATE, false, Blocks.DARK_OAK_FENCE_GATE.defaultBlockState(), () -> Items.DARK_OAK_FENCE_GATE);
		register(VanillaWoodType.CHERRY, WoodBlockKind.FENCE_GATE, false, Blocks.CHERRY_FENCE_GATE.defaultBlockState(), () -> Items.CHERRY_FENCE_GATE);

		register(VanillaWoodType.OAK, WoodBlockKind.STAIRS, false, Blocks.OAK_STAIRS.defaultBlockState(), () -> Items.OAK_STAIRS);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.STAIRS, false, Blocks.SPRUCE_STAIRS.defaultBlockState(), () -> Items.SPRUCE_STAIRS);
		register(VanillaWoodType.BIRCH, WoodBlockKind.STAIRS, false, Blocks.BIRCH_STAIRS.defaultBlockState(), () -> Items.BIRCH_STAIRS);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.STAIRS, false, Blocks.JUNGLE_STAIRS.defaultBlockState(), () -> Items.JUNGLE_STAIRS);
		register(VanillaWoodType.ACACIA, WoodBlockKind.STAIRS, false, Blocks.ACACIA_STAIRS.defaultBlockState(), () -> Items.ACACIA_STAIRS);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.STAIRS, false, Blocks.DARK_OAK_STAIRS.defaultBlockState(), () -> Items.DARK_OAK_STAIRS);
		register(VanillaWoodType.CHERRY, WoodBlockKind.STAIRS, false, Blocks.CHERRY_STAIRS.defaultBlockState(), () -> Items.CHERRY_STAIRS);

		register(VanillaWoodType.OAK, WoodBlockKind.DOOR, false, Blocks.OAK_DOOR.defaultBlockState(), () -> Items.OAK_DOOR);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.DOOR, false, Blocks.SPRUCE_DOOR.defaultBlockState(), () -> Items.SPRUCE_DOOR);
		register(VanillaWoodType.BIRCH, WoodBlockKind.DOOR, false, Blocks.BIRCH_DOOR.defaultBlockState(), () -> Items.BIRCH_DOOR);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.DOOR, false, Blocks.JUNGLE_DOOR.defaultBlockState(), () -> Items.JUNGLE_DOOR);
		register(VanillaWoodType.ACACIA, WoodBlockKind.DOOR, false, Blocks.ACACIA_DOOR.defaultBlockState(), () -> Items.ACACIA_DOOR);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.DOOR, false, Blocks.DARK_OAK_DOOR.defaultBlockState(), () -> Items.DARK_OAK_DOOR);
		register(VanillaWoodType.CHERRY, WoodBlockKind.DOOR, false, Blocks.CHERRY_DOOR.defaultBlockState(), () -> Items.CHERRY_DOOR);

		register(VanillaWoodType.OAK, WoodBlockKind.TRAPDOOR, false, Blocks.OAK_TRAPDOOR.defaultBlockState(), () -> Items.OAK_TRAPDOOR);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.TRAPDOOR, false, Blocks.SPRUCE_TRAPDOOR.defaultBlockState(), () -> Items.SPRUCE_TRAPDOOR);
		register(VanillaWoodType.BIRCH, WoodBlockKind.TRAPDOOR, false, Blocks.BIRCH_TRAPDOOR.defaultBlockState(), () -> Items.BIRCH_TRAPDOOR);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.TRAPDOOR, false, Blocks.JUNGLE_TRAPDOOR.defaultBlockState(), () -> Items.JUNGLE_TRAPDOOR);
		register(VanillaWoodType.ACACIA, WoodBlockKind.TRAPDOOR, false, Blocks.ACACIA_TRAPDOOR.defaultBlockState(), () -> Items.ACACIA_TRAPDOOR);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.TRAPDOOR, false, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(), () -> Items.DARK_OAK_TRAPDOOR);
		register(VanillaWoodType.CHERRY, WoodBlockKind.TRAPDOOR, false, Blocks.CHERRY_TRAPDOOR.defaultBlockState(), () -> Items.CHERRY_TRAPDOOR);

		register(VanillaWoodType.OAK, WoodBlockKind.SIGN, false, Blocks.OAK_SIGN.defaultBlockState(), () -> Items.OAK_SIGN);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.SIGN, false, Blocks.SPRUCE_SIGN.defaultBlockState(), () -> Items.SPRUCE_SIGN);
		register(VanillaWoodType.BIRCH, WoodBlockKind.SIGN, false, Blocks.BIRCH_SIGN.defaultBlockState(), () -> Items.BIRCH_SIGN);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.SIGN, false, Blocks.JUNGLE_SIGN.defaultBlockState(), () -> Items.JUNGLE_SIGN);
		register(VanillaWoodType.ACACIA, WoodBlockKind.SIGN, false, Blocks.ACACIA_SIGN.defaultBlockState(), () -> Items.ACACIA_SIGN);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.SIGN, false, Blocks.DARK_OAK_SIGN.defaultBlockState(), () -> Items.DARK_OAK_SIGN);
		register(VanillaWoodType.CHERRY, WoodBlockKind.SIGN, false, Blocks.CHERRY_SIGN.defaultBlockState(), () -> Items.CHERRY_SIGN);

		register(VanillaWoodType.OAK, WoodBlockKind.WALL_SIGN, false, Blocks.OAK_WALL_SIGN.defaultBlockState(), () -> Items.OAK_SIGN);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.WALL_SIGN, false, Blocks.SPRUCE_WALL_SIGN.defaultBlockState(), () -> Items.SPRUCE_SIGN);
		register(VanillaWoodType.BIRCH, WoodBlockKind.WALL_SIGN, false, Blocks.BIRCH_WALL_SIGN.defaultBlockState(), () -> Items.BIRCH_SIGN);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.WALL_SIGN, false, Blocks.JUNGLE_WALL_SIGN.defaultBlockState(), () -> Items.JUNGLE_SIGN);
		register(VanillaWoodType.ACACIA, WoodBlockKind.WALL_SIGN, false, Blocks.ACACIA_WALL_SIGN.defaultBlockState(), () -> Items.ACACIA_SIGN);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.WALL_SIGN, false, Blocks.DARK_OAK_WALL_SIGN.defaultBlockState(), () -> Items.DARK_OAK_SIGN);
		register(VanillaWoodType.CHERRY, WoodBlockKind.WALL_SIGN, false, Blocks.CHERRY_WALL_SIGN.defaultBlockState(), () -> Items.CHERRY_SIGN);

		register(VanillaWoodType.OAK, WoodBlockKind.HANGING_SIGN, false, Blocks.OAK_HANGING_SIGN.defaultBlockState(), () -> Items.OAK_HANGING_SIGN);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.HANGING_SIGN, false, Blocks.SPRUCE_HANGING_SIGN.defaultBlockState(), () -> Items.SPRUCE_HANGING_SIGN);
		register(VanillaWoodType.BIRCH, WoodBlockKind.HANGING_SIGN, false, Blocks.BIRCH_HANGING_SIGN.defaultBlockState(), () -> Items.BIRCH_HANGING_SIGN);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.HANGING_SIGN, false, Blocks.JUNGLE_HANGING_SIGN.defaultBlockState(), () -> Items.JUNGLE_HANGING_SIGN);
		register(VanillaWoodType.ACACIA, WoodBlockKind.HANGING_SIGN, false, Blocks.ACACIA_HANGING_SIGN.defaultBlockState(), () -> Items.ACACIA_HANGING_SIGN);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.HANGING_SIGN, false, Blocks.DARK_OAK_HANGING_SIGN.defaultBlockState(), () -> Items.DARK_OAK_HANGING_SIGN);
		register(VanillaWoodType.CHERRY, WoodBlockKind.HANGING_SIGN, false, Blocks.CHERRY_HANGING_SIGN.defaultBlockState(), () -> Items.CHERRY_HANGING_SIGN);

		register(VanillaWoodType.OAK, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.OAK_HANGING_SIGN);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.SPRUCE_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.SPRUCE_HANGING_SIGN);
		register(VanillaWoodType.BIRCH, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.BIRCH_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.BIRCH_HANGING_SIGN);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.JUNGLE_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.JUNGLE_HANGING_SIGN);
		register(VanillaWoodType.ACACIA, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.ACACIA_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.ACACIA_HANGING_SIGN);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.DARK_OAK_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.DARK_OAK_HANGING_SIGN);
		register(VanillaWoodType.CHERRY, WoodBlockKind.WALL_HANGING_SIGN, false, Blocks.CHERRY_WALL_HANGING_SIGN.defaultBlockState(), () -> Items.CHERRY_HANGING_SIGN);

		register(VanillaWoodType.OAK, WoodBlockKind.BUTTON, false, Blocks.OAK_BUTTON.defaultBlockState(), () -> Items.OAK_BUTTON);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.BUTTON, false, Blocks.SPRUCE_BUTTON.defaultBlockState(), () -> Items.SPRUCE_BUTTON);
		register(VanillaWoodType.BIRCH, WoodBlockKind.BUTTON, false, Blocks.BIRCH_BUTTON.defaultBlockState(), () -> Items.BIRCH_BUTTON);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.BUTTON, false, Blocks.JUNGLE_BUTTON.defaultBlockState(), () -> Items.JUNGLE_BUTTON);
		register(VanillaWoodType.ACACIA, WoodBlockKind.BUTTON, false, Blocks.ACACIA_BUTTON.defaultBlockState(), () -> Items.ACACIA_BUTTON);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.BUTTON, false, Blocks.DARK_OAK_BUTTON.defaultBlockState(), () -> Items.DARK_OAK_BUTTON);
		register(VanillaWoodType.CHERRY, WoodBlockKind.BUTTON, false, Blocks.CHERRY_BUTTON.defaultBlockState(), () -> Items.CHERRY_BUTTON);

		register(VanillaWoodType.OAK, WoodBlockKind.PRESSURE_PLATE, false, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), () -> Items.OAK_PRESSURE_PLATE);
		register(VanillaWoodType.SPRUCE, WoodBlockKind.PRESSURE_PLATE, false, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState(), () -> Items.SPRUCE_PRESSURE_PLATE);
		register(VanillaWoodType.BIRCH, WoodBlockKind.PRESSURE_PLATE, false, Blocks.BIRCH_PRESSURE_PLATE.defaultBlockState(), () -> Items.BIRCH_PRESSURE_PLATE);
		register(VanillaWoodType.JUNGLE, WoodBlockKind.PRESSURE_PLATE, false, Blocks.JUNGLE_PRESSURE_PLATE.defaultBlockState(), () -> Items.JUNGLE_PRESSURE_PLATE);
		register(VanillaWoodType.ACACIA, WoodBlockKind.PRESSURE_PLATE, false, Blocks.ACACIA_PRESSURE_PLATE.defaultBlockState(), () -> Items.ACACIA_PRESSURE_PLATE);
		register(VanillaWoodType.DARK_OAK, WoodBlockKind.PRESSURE_PLATE, false, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState(), () -> Items.DARK_OAK_PRESSURE_PLATE);
		register(VanillaWoodType.CHERRY, WoodBlockKind.PRESSURE_PLATE, false, Blocks.CHERRY_PRESSURE_PLATE.defaultBlockState(), () -> Items.CHERRY_PRESSURE_PLATE);
	}

	/**
	 * Register wood blocks that have no variant property
	 */
	private <T extends Block & IWoodTyped> void registerWithoutVariants(T woodTyped, WoodBlockKind woodBlockKind) {
		boolean fireproof = woodTyped.isFireproof();
		BlockState blockState = woodTyped.defaultBlockState();
		IWoodType woodType = woodTyped.getWoodType();
		Supplier<Item> itemStack = woodTyped::asItem;
		register(woodType, woodBlockKind, fireproof, blockState, itemStack);
	}

	@Override
	public void register(IWoodType woodType, WoodBlockKind woodBlockKind, boolean fireproof, BlockState blockState, Supplier<Item> itemStack) {
		if (isNonBurning(woodBlockKind)) {
			fireproof = true;
		}
		WoodMap woodMap = woodMaps.get(woodBlockKind);
		if (!registeredWoodTypes.contains(woodType)) {
			registeredWoodTypes.add(woodType);
		}
		woodMap.getItem(fireproof).put(woodType, itemStack);
		woodMap.getBlock(fireproof).put(woodType, blockState);
	}

	@Override
	public ItemStack getStack(IWoodType woodType, WoodBlockKind woodBlockKind, boolean fireproof) {
		if (isNonBurning(woodBlockKind)) {
			fireproof = true;
		}
		WoodMap woodMap = woodMaps.get(woodBlockKind);
		Supplier<Item> itemStack = woodMap.getItem(fireproof).get(woodType);
		if (itemStack == null) {
			String errMessage = String.format("No stack found for %s %s %s", woodType, woodMap.getName(), fireproof ? "fireproof" : "non-fireproof");
			throw new IllegalStateException(errMessage);
		}
		return new ItemStack(itemStack.get());
	}

	@Override
	public BlockState getBlock(IWoodType woodType, WoodBlockKind woodBlockKind, boolean fireproof) {
		if (isNonBurning(woodBlockKind)) {
			fireproof = true;
		}
		WoodMap woodMap = woodMaps.get(woodBlockKind);
		BlockState blockState = woodMap.getBlock(fireproof).get(woodType);
		if (blockState == null) {
			String errMessage = String.format("No block found for %s %s %s", woodType, woodMap.getName(), fireproof ? "fireproof" : "non-fireproof");
			throw new IllegalStateException(errMessage);
		}
		return blockState;
	}

	private static boolean isNonBurning(WoodBlockKind kind) {
		return kind == WoodBlockKind.DOOR || kind == WoodBlockKind.TRAPDOOR || kind == WoodBlockKind.BUTTON || kind == WoodBlockKind.PRESSURE_PLATE;
	}

	@Override
	public List<IWoodType> getRegisteredWoodTypes() {
		return registeredWoodTypes;
	}

	private static class WoodMap {
		private final Map<IWoodType, Supplier<Item>> normalItems = new HashMap<>();
		private final Map<IWoodType, Supplier<Item>> fireproofItems = new HashMap<>();
		private final Map<IWoodType, BlockState> normalBlocks = new HashMap<>();
		private final Map<IWoodType, BlockState> fireproofBlocks = new HashMap<>();
		private final WoodBlockKind woodBlockKind;

		public WoodMap(WoodBlockKind woodBlockKind) {
			this.woodBlockKind = woodBlockKind;
		}

		public String getName() {
			return woodBlockKind.name();
		}

		public Map<IWoodType, Supplier<Item>> getItem(boolean fireproof) {
			return fireproof ? this.fireproofItems : this.normalItems;
		}

		public Map<IWoodType, BlockState> getBlock(boolean fireproof) {
			return fireproof ? this.fireproofBlocks : this.normalBlocks;
		}
	}
}
