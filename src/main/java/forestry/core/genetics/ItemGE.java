package forestry.core.genetics;

import forestry.Forestry;
import forestry.api.ForestryCapabilities;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.ForestryConfig;
import forestry.core.features.CoreDataComponents;
import forestry.core.items.ItemForestry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public abstract class ItemGE extends ItemForestry {
	protected final ILifeStage stage;

	protected ItemGE(Item.Properties properties, ILifeStage stage) {
		super(properties.setNoRepair().component(CoreDataComponents.INDIVIDUAL));

		this.stage = stage;
	}

	protected abstract ISpecies<?> getSpecies(ItemStack stack);

	protected abstract ISpeciesType<?, ?> getType();

	@Override
	public Component getName(ItemStack stack) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM);
		if (handler == null) {
			return super.getName(stack);
		}
		return handler.getIndividual().getSpecies().getItemDisplayName(handler.getStage());
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		ISpecies<?> species = getSpecies(stack);
		return species.hasGlint() && ForestryConfig.CLIENT.enableGlints.get();
	}

	public static void appendGeneticsTooltip(ItemStack stack, List<Component> tooltip) {
		boolean analyzed = false;
		IIndividual individual = IIndividualHandlerItem.getIndividual(stack);

		if (individual != null) {
			if (individual.isAnalyzed()) {
				if (Screen.hasShiftDown()) {
					individual.getSpecies().addTooltip(individual.cast(), tooltip);
				} else {
					tooltip.add(Component.translatable("for.gui.tooltip.tmi", "< %s >").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true)));
				}

				analyzed = true;
			}
		}
		if (!analyzed) {
			tooltip.add(Component.translatable("for.gui.unknown", "< %s >").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
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
