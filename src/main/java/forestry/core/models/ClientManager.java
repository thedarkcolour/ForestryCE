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
package forestry.core.models;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.event.ModelEvent;

import forestry.core.blocks.IColoredBlock;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.ModUtil;
import forestry.core.utils.ResourceUtil;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureGroup;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureTable;

public enum ClientManager {
	INSTANCE;

	public static final ItemColor FORESTRY_ITEM_COLOR = (stack, tintIndex) -> {
		Item item = stack.getItem();
		if (item instanceof IColoredItem coloredItem) {
			return coloredItem.getColorFromItemStack(stack, tintIndex);
		}
		return 0xffffff;
	};
	public static final BlockColor FORESTRY_BLOCK_COLOR = (state, level, pos, tintIndex) -> {
		Block block = state.getBlock();
		if (level != null && pos != null && block instanceof IColoredBlock coloredBlock) {
			return coloredBlock.colorMultiplier(state, level, pos, tintIndex);
		}
		return 0xffffff;
	};

	/* CUSTOM MODELS*/
	private final List<BlockModelEntry> customBlockModels = new ArrayList<>();
	private final List<ModelEntry> customModels = new ArrayList<>();
	/* DEFAULT ITEM AND BLOCK MODEL STATES*/
	@Nullable
	private ModelState defaultBlockState;

	public ModelState getDefaultBlockState() {
		if (defaultBlockState == null) {
			defaultBlockState = ResourceUtil.loadTransform(new ResourceLocation("block/block"));
		}
		return defaultBlockState;
	}

	public void registerModel(BakedModel model, Object feature) {
		if (feature instanceof FeatureGroup<?, ?, ?> group) {
			group.getFeatures().forEach(f -> registerModel(model, f));
		} else if (feature instanceof FeatureTable<?, ?, ?, ?> group) {
			group.getFeatures().forEach(f -> registerModel(model, f));
		} else if (feature instanceof FeatureBlock<?, ?> block) {
			registerModel(model, block.block(), block.item());
		} else if (feature instanceof FeatureItem<?> item) {
			registerModel(model, item.item());
		}
	}

	public void registerModel(BakedModel model, Block block, @Nullable BlockItem item) {
		registerModel(model, block, item, block.getStateDefinition().getPossibleStates());
	}

	public void registerModel(BakedModel model, Block block, @Nullable BlockItem item, Collection<BlockState> states) {
		customBlockModels.add(new BlockModelEntry(model, block, item, states));
	}

	public void registerModel(BakedModel model, Item item) {
		customModels.add(new ModelEntry(new ModelResourceLocation(ModUtil.getRegistryName(item), "inventory"), model));
	}

	public void onBakeModels(ModelEvent.ModifyBakingResult event) {
		//register custom models
		Map<ResourceLocation, BakedModel> registry = event.getModels();
		for (final BlockModelEntry entry : customBlockModels) {
			for (BlockState state : entry.states) {
				registry.put(BlockModelShaper.stateToModelLocation(state), entry.model);
			}
			if (entry.item != null) {
				ResourceLocation registryName = ModUtil.getRegistryName(entry.item);
				if (registryName == null) {
					continue;
				}
				registry.put(new ModelResourceLocation(registryName, "inventory"), entry.model);
			}
		}

		for (final ModelEntry entry : customModels) {
			registry.put(entry.modelLocation, entry.model);
		}
	}

	private record BlockModelEntry(BakedModel model, Block block, @Nullable BlockItem item,
								   Collection<BlockState> states) {
	}

	private record ModelEntry(ModelResourceLocation modelLocation, BakedModel model) {
	}
}
