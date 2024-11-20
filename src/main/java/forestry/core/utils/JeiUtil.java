package forestry.core.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.common.crafting.IShapedRecipe;

import forestry.Forestry;
import forestry.api.ForestryCapabilities;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.modules.features.FeatureItem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;

public class JeiUtil {
	public static final String DESCRIPTION_KEY = "for.jei.description.";
	// From Ex Deorum: https://github.com/thedarkcolour/ExDeorum/blob/8ea02fd490dbfa106bdfe31af8b7a88f65b2abdc/src/main/java/thedarkcolour/exdeorum/compat/jei/ClientJeiUtil.java#L221
	static final DecimalFormat FORMATTER = new DecimalFormat();

	static {
		FORMATTER.setMinimumFractionDigits(0);
		FORMATTER.setMaximumFractionDigits(3);
	}

	public static void addDescription(IRecipeRegistration registry, String itemKey, FeatureItem<?>... items) {
		List<ItemStack> itemStacks = new ArrayList<>();
		for (FeatureItem<?> item : items) {
			itemStacks.add(item.stack());
		}

		registry.addIngredientInfo(itemStacks, VanillaTypes.ITEM_STACK, Component.translatable(DESCRIPTION_KEY + itemKey));
	}

	public static void addDescription(IRecipeRegistration registry, Block... blocks) {
		for (Block block : blocks) {
			Item item = block.asItem();
			if (item != Items.AIR) {
				addDescription(registry, item);
			} else {
				Forestry.LOGGER.error("No item for block {}", block);
			}
		}
	}

	public static void addDescription(IRecipeRegistration registry, FeatureItem<?>... items) {
		for (FeatureItem<?> item : items) {
			addDescription(registry, item.get());
		}
	}

	public static void addDescription(IRecipeRegistration registry, Item... items) {
		for (Item item : items) {
			addDescription(registry, item);
		}
	}

	public static void addDescription(IRecipeRegistration registry, Item item) {
		ResourceLocation registryName = ModUtil.getRegistryName(item);
		String resourcePath = registryName.getPath();
		addDescription(registry, item, resourcePath);
	}

	public static void addDescription(IRecipeRegistration registry, Item item, String itemKey) {
		ItemStack itemStack = new ItemStack(item);
		registry.addIngredientInfo(itemStack, VanillaTypes.ITEM_STACK, Component.translatable(DESCRIPTION_KEY + itemKey));
	}

	public static List<IRecipeSlotBuilder> layoutSlotGrid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int width, int height, int xOffset, int yOffset, int slotSpacing) {
		List<IRecipeSlotBuilder> craftingSlots = new ArrayList<>();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				IRecipeSlotBuilder slot = builder.addSlot(role, xOffset + x * slotSpacing, yOffset + y * slotSpacing);
				craftingSlots.add(slot);
			}
		}
		return craftingSlots;
	}

	public static void setCraftingItems(List<IRecipeSlotBuilder> craftingSlots, CraftingRecipe craftingGridRecipe, ICraftingGridHelper craftingGridHelper) {
		int width = 0;
		int height = 0;
		if (craftingGridRecipe instanceof IShapedRecipe<?> shapedRecipe) {
			width = shapedRecipe.getRecipeWidth();
			height = shapedRecipe.getRecipeHeight();
		}
		setCraftingItems(craftingSlots, craftingGridRecipe.getIngredients(), width, height, craftingGridHelper);
	}

	public static void setCraftingItems(List<IRecipeSlotBuilder> craftingSlots, List<Ingredient> ingredients, int width, int height, ICraftingGridHelper craftingGridHelper) {
		List<List<ItemStack>> itemStacks = ingredients.stream()
				.map(ingredient -> List.of(ingredient.getItems()))
				.toList();
		craftingGridHelper.setInputs(craftingSlots, VanillaTypes.ITEM_STACK, itemStacks, width, height);
	}

	public static NonNullList<ItemStack> getFirstItemStacks(IRecipeSlotsView recipeSlots) {
		List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
		NonNullList<ItemStack> result = NonNullList.create();

		for (IRecipeSlotView slot : slotViews) {
			if (slot.isEmpty()) {
				result.add(ItemStack.EMPTY);
			} else {
				slot.getDisplayedIngredient()
						.filter(ingredient -> ingredient.getType() == VanillaTypes.ITEM_STACK)
						.ifPresent(ingredient -> result.add(((ITypedIngredient<ItemStack>) ingredient).getIngredient().copy()));
			}
		}
		return result;
	}

	public static <S extends ISpecies<?>> void registerItemSubtypes(ISubtypeRegistration registry, IRegistryChromosome<S> species, ISpeciesType<S, ?> type) {
		IIngredientSubtypeInterpreter<ItemStack> interpreter = (stack, context) -> stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM)
				.map(individual -> individual.getIndividual().getGenome().getActiveValue(species).getBinomial())
				.orElse(IIngredientSubtypeInterpreter.NONE);

		for (ILifeStage stage : type.getLifeStages()) {
			registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, stage.getItemForm(), interpreter);
		}
	}

	// From JEI Bees
	public static void drawCenteredMulti(GuiGraphics graphics, Component component, float x, float y, int color) {
		Font font = Minecraft.getInstance().font;
		String[] split = component.getString().split(" ");

		for (int i = 0; i < split.length; i++) {
			String line = split[i];
			drawCentered(graphics, font, line, x, y + i * font.lineHeight, color);
		}
	}

	private static void drawCentered(GuiGraphics graphics, Font font, String line, float x, float y, int color) {
		int width = font.width(line);

		graphics.drawString(font, line, x - (width / 2f), y, color, false);
	}
	// End of JEI Bees

	// From Ex Deorum
	// Takes a decimal probability and returns a user-friendly percentage value
	public static Component formatChance(double probability) {
		return Component.translatable("for.jei.chance", formatPercentage(probability)).withStyle(ChatFormatting.GRAY);
	}

	public static String formatPercentage(double probability) {
		return FORMATTER.format(probability * 100);
	}
	// End of Ex Deorum
}
