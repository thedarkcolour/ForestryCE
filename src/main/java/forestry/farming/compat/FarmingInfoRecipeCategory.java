package forestry.farming.compat;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryConstants;
import forestry.api.circuits.ICircuit;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.api.farming.Soil;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.config.Constants;
import forestry.core.features.CoreItems;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.utils.JeiUtil;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

public class FarmingInfoRecipeCategory extends ForestryRecipeCategory<FarmingInfoRecipe> {
	public static final RecipeType<FarmingInfoRecipe> TYPE = RecipeType.create(ForestryConstants.MOD_ID, "farming", FarmingInfoRecipe.class);
	private final IDrawable slotDrawable;
	private final IDrawable addition;
	private final IDrawable arrow;
	private final IDrawable icon;

	public FarmingInfoRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createBlankDrawable(144, 90), "for.jei.farming");
		this.slotDrawable = guiHelper.getSlotDrawable();
		ResourceLocation resourceLocation = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/jei/recipes.png");
		addition = guiHelper.createDrawable(resourceLocation, 44, 0, 15, 15);
		arrow = guiHelper.createDrawable(resourceLocation, 59, 0, 15, 15);
		ItemStack intricateCircuitboard = new ItemStack(CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.INTRICATE));
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, intricateCircuitboard);
	}

	@Override
	public RecipeType<FarmingInfoRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FarmingInfoRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 64, 19)
				.setBackground(this.slotDrawable, -1, -1)
				.addItemStack(recipe.tube());

		IFarmType properties = recipe.properties();

		// 2x2 of slots
		List<IRecipeSlotBuilder> soilSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.INPUT, 2, 2, 1, 55, 18);
		List<IRecipeSlotBuilder> germlingSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.INPUT, 2, 2, 55, 55, 18);
		List<IRecipeSlotBuilder> productSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.OUTPUT, 2, 2, 109, 55, 18);
		int soilSlotsSize = soilSlots.size();
		int germlingSlotsSize = germlingSlots.size();
		int productSlotsSize = productSlots.size();

		// Set backgrounds
		soilSlots.forEach(slot -> slot.setBackground(this.slotDrawable, -1, -1));
		germlingSlots.forEach(slot -> slot.setBackground(this.slotDrawable, -1, -1));
		productSlots.forEach(slot -> slot.setBackground(this.slotDrawable, -1, -1));

		MutableInt germlingSlotIndex = new MutableInt();
		MutableInt productSlotIndex = new MutableInt();
		int soilSlotIndex = 0;

		// item stacks are distributed to ezach slot using round robin
		for (IFarmable farmable : properties.getFarmables()) {
			farmable.addGermlings(germling -> germlingSlots.get(germlingSlotIndex.getAndIncrement() % germlingSlotsSize)
					.addItemStack(germling));
			farmable.addProducts(product -> productSlots.get(productSlotIndex.getAndIncrement() % productSlotsSize)
					.addItemStack(product));
		}
		for (Soil soil : properties.getSoils()) {
			soilSlots.get(soilSlotIndex++ % soilSlotsSize)
					.addItemStack(soil.resource());
		}
	}

	@Override
	public void draw(FarmingInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		addition.draw(graphics, 37, 64);
		arrow.draw(graphics, 91, 64);
		int recipeWidth = this.getBackground().getWidth();
		Font font = Minecraft.getInstance().font;
		ICircuit circuit = recipe.circuit();
		int textX = (recipeWidth - font.width(circuit.getDisplayName().getString())) / 2;
		graphics.drawString(font, circuit.getDisplayName(), textX, 3, Color.darkGray.getRGB());

		Component soilName = Component.translatable("for.jei.farming.soil");
		graphics.drawString(font, soilName, 18 - (font.width(soilName.getString())) / 2, 45, Color.darkGray.getRGB());

		Component germlingsName = Component.translatable("for.jei.farming.germlings");
		graphics.drawString(font, germlingsName, (recipeWidth - font.width(germlingsName.getString())) / 2, 45, Color.darkGray.getRGB());

		Component productsName = Component.translatable("for.jei.farming.products");
		graphics.drawString(font, productsName, 126 - (font.width(productsName.getString())) / 2, 45, Color.darkGray.getRGB());
	}
}
