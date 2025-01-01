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
package forestry.apiculture.hives;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;

import forestry.api.ForestryTags;
import forestry.apiculture.blocks.BlockHiveType;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveGen;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.tiles.TileHive;
import forestry.core.tiles.TileUtil;

public class HiveDefinitionSwarmer implements IHiveDefinition {
	private final List<ItemStack> bees;

	public HiveDefinitionSwarmer(ItemStack... bees) {
		this.bees = Arrays.asList(bees);
	}

	@Override
	public IHiveGen getHiveGen() {
		return new HiveGenGround(ForestryTags.Blocks.SWARM_BEE_GROUND);
	}

	@Override
	public BlockState getBlockState() {
		return ApicultureBlocks.BEEHIVE.get(BlockHiveType.SWARM).defaultState();
	}

	@Override
	public boolean isGoodBiome(Holder<Biome> biome) {
		return true;
	}

	@Override
	public boolean isGoodHumidity(HumidityType humidity) {
		return true;
	}

	@Override
	public boolean isGoodTemperature(TemperatureType temperature) {
		return true;
	}

	@Override
	public float getGenChance() {
		return 128.0f;
	}

	@Override
	public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
		TileUtil.actOnTile(level, pos, TileHive.class, tile -> tile.setContained(bees));
	}
}
