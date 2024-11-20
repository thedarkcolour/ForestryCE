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
package forestry.apiculture.items;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.genetics.ItemGE;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.SpeciesUtil;

public class ItemBeeGE extends ItemGE implements IColoredItem {
	public ItemBeeGE(BeeLifeStage type) {
		super(type != BeeLifeStage.DRONE ? new Item.Properties().stacksTo(1) : new Item.Properties(), type);
	}

	@Override
	protected ISpeciesType<?, ?> getType() {
		return SpeciesUtil.BEE_TYPE.get();
	}

	@Override
	protected IBeeSpecies getSpecies(ItemStack stack) {
		return IIndividualHandlerItem.getSpecies(stack, SpeciesUtil.BEE_TYPE.get());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
		if (!stack.hasTag()) {
			return;
		}

		if (this.stage != BeeLifeStage.DRONE) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				if (((IBee) individual).isPristine()) {
					list.add(Component.translatable("for.bees.stock.pristine").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
				} else {
					list.add(Component.translatable("for.bees.stock.ignoble").withStyle(ChatFormatting.YELLOW));
				}
			});
		}

		super.appendHoverText(stack, level, list, flag);
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (!stack.hasTag()) {
			if (tintIndex == 1) {
				// 1 = body
				return 0xffdc16;
			} else if (tintIndex == 2) {
				// 2 = stripes
				return 0;
			} else {
				// 0 = outline
				return 0xffffff;
			}
		} else {
			IBeeSpecies species = getSpecies(stack);

			return switch (tintIndex) {
				case 2 -> species.getStripes();
				case 1 -> species.getBody();
				case 0 -> species.getOutline();
				default -> 0xffffff;
			};
		}
	}

	public final BeeLifeStage getStage() {
		return (BeeLifeStage) this.stage;
	}
}
