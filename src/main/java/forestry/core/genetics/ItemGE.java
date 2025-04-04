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
package forestry.core.genetics;

import javax.annotation.Nullable;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import forestry.Forestry;
import forestry.api.ForestryCapabilities;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.ForestryConfig;
import forestry.core.genetics.capability.SerializableIndividualHandlerItem;
import forestry.core.items.ItemForestry;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;

public abstract class ItemGE extends ItemForestry {
	protected final ILifeStage stage;

	protected ItemGE(Item.Properties properties, ILifeStage stage) {
		super(properties.setNoRepair());

		this.stage = stage;
	}

	protected abstract ISpecies<?> getSpecies(ItemStack stack);

	protected abstract ISpeciesType<?, ?> getType();

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		Tag parent;

		if (nbt != null && nbt.contains("Parent")) {
			// serializable caps returned by this method are saved under "Parent". I love undocumented Forge code!!!
			parent = nbt.get("Parent");
		} else if (stack.getTag() != null && stack.getTagElement("ForgeCaps") != null && stack.getTagElement("ForgeCaps").contains("Parent")) {
			// Individual.saveToStack saves to NBT manually to bypass the cap nbt being null without setting the field
			parent = stack.getTagElement("ForgeCaps").get("Parent");
		} else {
			parent = null;
		}

		if (parent == null) {
			return new SerializableIndividualHandlerItem(getType(), stack, getType().getDefaultSpecies().createIndividual(), this.stage);
		}

		return new SerializableIndividualHandlerItem(getType(), stack, SpeciesUtil.deserializeIndividual(getType(), parent), this.stage);
	}

	@Override
	public Component getName(ItemStack stack) {
		return stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM)
				.map(handler -> GeneticsUtil.getItemName(handler.getStage(), handler.getIndividual().getSpecies()))
				.orElseGet(() -> super.getName(stack));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (!stack.hasTag()) { // villager trade wildcard bees
			return false;
		}
		ISpecies<?> species = getSpecies(stack);
		return species.hasGlint() && ForestryConfig.CLIENT.enableGlints.get();
	}

	public static void appendGeneticsTooltip(ItemStack stack, List<Component> tooltip) {
		if (!stack.hasTag()) {
			return;
		}

		MutableBoolean analyzed = new MutableBoolean();
		IIndividualHandlerItem.ifPresent(stack, individual -> {
			if (individual.isAnalyzed()) {
				if (Screen.hasShiftDown()) {
					((ISpecies<IIndividual>) individual.getSpecies()).addTooltip(individual, tooltip);
				} else {
					tooltip.add(Component.translatable("for.gui.tooltip.tmi", "< %s >").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true)));
				}

				analyzed.setTrue();
			}
		});
		if (analyzed.isFalse()) {
			tooltip.add(Component.translatable("for.gui.unknown", "< %s >").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		appendGeneticsTooltip(stack, tooltip);
	}

	@Override
	public String getCreatorModId(ItemStack stack) {
		ISpecies<?> species = getSpecies(stack);
		return species.id().getNamespace();
	}

	public static <S extends ISpecies<I>, I extends IIndividual> void addCreativeItems(ILifeStage stage, List<ItemStack> subItems, boolean hideSecrets, ISpeciesType<S, I> type) {
		for (S species : type.getAllSpecies()) {
			// Don't show secrets unless ordered to.
			if (hideSecrets && species.isSecret() && !Forestry.DEBUG) {
				continue;
			}

			subItems.add(species.createStack(species.createIndividual(), stage));
		}
	}
}
