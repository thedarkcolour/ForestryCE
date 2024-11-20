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
package forestry.lepidopterology.items;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.Forestry;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.genetics.ItemGE;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.EntityUtil;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.TreeUtil;
import forestry.lepidopterology.entities.EntityButterfly;
import forestry.lepidopterology.features.LepidopterologyEntities;

public class ItemButterflyGE extends ItemGE implements IColoredItem {
	public static final String NBT_AGE = "Age";
	public static final int MAX_AGE = 3;

	public ItemButterflyGE(ButterflyLifeStage stage) {
		super(new Properties(), stage);
	}

	@Override
	protected IButterflySpecies getSpecies(ItemStack stack) {
		return IIndividualHandlerItem.getSpecies(stack, SpeciesUtil.BUTTERFLY_TYPE.get());
	}

	@Override
	protected ISpeciesType<?, ?> getType() {
		return SpeciesUtil.BUTTERFLY_TYPE.get();
	}

	// todo fix
	public void addCreativeItems(List<ItemStack> subItems, boolean hideSecrets) {
		if (this.stage == ButterflyLifeStage.COCOON) {
			for (int age = 0; age < MAX_AGE; age++) {
				for (IButterflySpecies species : SpeciesUtil.getAllButterflySpecies()) {
					// Don't show secret butterflies unless ordered to.
					if (hideSecrets && species.isSecret() && !Forestry.DEBUG) {
						continue;
					}

					ItemStack butterfly = species.createStack(this.stage);

					ItemButterflyGE.setAge(butterfly, age);

					subItems.add(butterfly);
				}
			}
		} else {
			for (IButterflySpecies species : SpeciesUtil.BUTTERFLY_TYPE.get().getAllSpecies()) {
				// Don't show secret butterflies unless ordered to.
				if (hideSecrets && species.isSecret() && !Forestry.DEBUG) {
					continue;
				}

				subItems.add(species.createStack(this.stage));
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (this.stage != ButterflyLifeStage.BUTTERFLY) {
			return false;
		}
		Level level = entityItem.level();
		if (level.isClientSide || entityItem.tickCount < 80) {
			return false;
		}
		if (level.random.nextInt(24) != 0) {
			return false;
		}

		IButterfly butterfly = (IButterfly) IIndividualHandlerItem.getIndividual(entityItem.getItem());
		if (butterfly == null) {
			return false;
		}

		if (!butterfly.canTakeFlight(level, entityItem.getX(), entityItem.getY(), entityItem.getZ())) {
			return false;
		}

		EntityUtil.spawnEntity(entityItem.level(), EntityButterfly.create(LepidopterologyEntities.BUTTERFLY.entityType(), entityItem.level(), butterfly, entityItem.blockPosition()), entityItem.getX(), entityItem.getY(), entityItem.getZ());
		if (!entityItem.getItem().isEmpty()) {
			entityItem.getItem().shrink(1);
		} else {
			entityItem.remove(Entity.RemovalReason.DISCARDED);
		}
		return true;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		if (level.isClientSide) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(context.getHand());

		IButterfly flutter = (IButterfly) IIndividualHandlerItem.getIndividual(stack);

		BlockState blockState = level.getBlockState(pos);
		if (this.stage == ButterflyLifeStage.COCOON) {
			pos = SpeciesUtil.BUTTERFLY_TYPE.get().plantCocoon(level, pos, flutter, getAge(stack), true);
			if (pos != null) {
				BlockUtil.sendPlaceSound(level, pos, blockState);

				if (!player.isCreative()) {
					stack.shrink(1);
				}
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.PASS;
			}
		} else if (this.stage == ButterflyLifeStage.CATERPILLAR) {
			IButterflyNursery nursery = TreeUtil.getOrCreateNursery(level, pos, true);
			if (nursery != null) {
				if (!nursery.canNurse(flutter)) {
					return InteractionResult.PASS;
				}

				nursery.setCaterpillar(flutter);

				BlockUtil.sendDestroyEffects(level, pos, blockState);

				if (!player.isCreative()) {
					stack.shrink(1);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		} else {
			return InteractionResult.PASS;
		}
	}

	public static void setAge(ItemStack cocoon, int age) {
		if (cocoon.isEmpty()) {
			return;
		}
		if (SpeciesUtil.BUTTERFLY_TYPE.get().getLifeStage(cocoon) != ButterflyLifeStage.COCOON) {
			return;
		}
		CompoundTag tagCompound = cocoon.getTag();
		if (tagCompound == null) {
			cocoon.setTag(tagCompound = new CompoundTag());
		}
		tagCompound.putInt(NBT_AGE, age);
	}

	public static int getAge(ItemStack cocoon) {
		if (cocoon.isEmpty()) {
			return 0;
		}
		if (SpeciesUtil.BUTTERFLY_TYPE.get().getLifeStage(cocoon) != ButterflyLifeStage.COCOON) {
			return 0;
		}
		CompoundTag tagCompound = cocoon.getTag();
		if (tagCompound == null) {
			return 0;
		}
		return Mth.clamp(tagCompound.getInt(NBT_AGE), 0, 2);
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1 && stack.hasTag()) {
			IButterflySpecies species = getSpecies(stack);
			return species.getSerumColor();
		}
		return 0xffffff;
	}
}
