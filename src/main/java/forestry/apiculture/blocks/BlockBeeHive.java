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
package forestry.apiculture.blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import forestry.api.ForestryTags;
import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.apiculture.hives.IHiveTile;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.tiles.TileHive;
import forestry.core.tiles.TileUtil;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

// Hives where wild bees live
public class BlockBeeHive extends Block implements EntityBlock {
	private final ResourceLocation speciesId;

	public BlockBeeHive(BlockHiveType type) {
		this(type.getSpeciesId());
	}

	public BlockBeeHive(ResourceLocation speciesId) {
		super(Properties.of().sound(SoundType.WOOD).lightLevel(state -> 7).strength(2.5f));
		this.speciesId = speciesId;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileHive(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> actualType) {
		return actualType != ApicultureTiles.HIVE.tileType() ? null : (level1, pos, state1, t) -> ((TileHive) t).tick(level1);
	}

	@Override
	public void attack(BlockState state, Level world, BlockPos pos, Player player) {
		TileUtil.actOnTile(world, pos, IHiveTile.class, tile -> tile.onAttack(world, pos, player));
	}

	@Override
	public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		super.playerWillDestroy(world, pos, state, player);
		boolean canHarvest = canHarvestBlock(state, world, pos, player);
		TileUtil.actOnTile(world, pos, IHiveTile.class, tile -> tile.onBroken(world, pos, player, canHarvest));
	}

	@Nullable
	private ObjectArrayList<IHiveDrop> getDropsForHive() {
		List<IHiveDrop> drops = IForestryApi.INSTANCE.getHiveManager().getDrops(this.speciesId);

		if (drops.isEmpty()) {
			return null;
		} else {
			return new ObjectArrayList<>(drops);
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		BlockPos pos = BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN));
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);

		if (tool.is(ForestryTags.Items.SCOOPS)) {
			int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
			return getDrops(builder.getLevel(), pos, fortune);
		}
		return List.of();
	}

	private List<ItemStack> getDrops(ServerLevel level, BlockPos pos, int fortune) {
		List<ItemStack> drops = new ArrayList<>();
		RandomSource random = level.getRandom();

		ObjectArrayList<IHiveDrop> hiveDrops = getDropsForHive();
		if (hiveDrops == null) {
			return drops;
		} else {
			Util.shuffle(hiveDrops, level.random);
		}

		// Grab a princess (10 tries)
		int tries = 0;
		boolean hasPrincess = false;
		while (tries <= 10 && !hasPrincess) {
			tries++;

			for (IHiveDrop drop : hiveDrops) {
				if (random.nextDouble() < drop.getChance(level, pos, fortune)) {
					IBee bee = drop.createIndividual(level, pos);
					if (random.nextFloat() < drop.getIgnobleChance(level, pos, fortune)) {
						bee.setPristine(false);
					}

					ItemStack princess = bee.createStack(BeeLifeStage.PRINCESS);
					drops.add(princess);
					hasPrincess = true;
					break;
				}
			}
		}

		// Grab drones
		for (IHiveDrop drop : hiveDrops) {
			if (random.nextDouble() < drop.getChance(level, pos, fortune)) {
				IBee bee = drop.createIndividual(level, pos);
				ItemStack drone = bee.createStack(BeeLifeStage.DRONE);
				drops.add(drone);
				break;
			}
		}

		// Grab anything else on offer
		for (IHiveDrop drop : hiveDrops) {
			if (random.nextDouble() < drop.getChance(level, pos, fortune)) {
				drops.addAll(drop.getExtraItems(level, pos, fortune));
				break;
			}
		}
		return drops;
	}

	public ResourceLocation getSpeciesId() {
		return this.speciesId;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 5;
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return true;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 5;
	}
}
