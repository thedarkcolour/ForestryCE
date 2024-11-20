package forestry.factory.recipes.jei;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import forestry.api.fuels.FuelManager;
import forestry.api.fuels.RainSubstrate;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.ClientsideCode;
import forestry.core.features.FluidsItems;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.widgets.TankWidget;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.ModUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.blocks.BlockFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.gui.GuiCarpenter;
import forestry.factory.gui.GuiCentrifuge;
import forestry.factory.gui.GuiFabricator;
import forestry.factory.gui.GuiFermenter;
import forestry.factory.gui.GuiMoistener;
import forestry.factory.gui.GuiSqueezer;
import forestry.factory.gui.GuiStill;
import forestry.factory.recipes.jei.carpenter.CarpenterRecipeCategory;
import forestry.factory.recipes.jei.carpenter.CarpenterRecipeTransferHandler;
import forestry.factory.recipes.jei.centrifuge.CentrifugeRecipeCategory;
import forestry.factory.recipes.jei.fabricator.FabricatorRecipeCategory;
import forestry.factory.recipes.jei.fabricator.FabricatorRecipeTransferHandler;
import forestry.factory.recipes.jei.fermenter.FermenterRecipeCategory;
import forestry.factory.recipes.jei.moistener.MoistenerRecipeCategory;
import forestry.factory.recipes.jei.rainmaker.RainmakerRecipeCategory;
import forestry.factory.recipes.jei.squeezer.SqueezerRecipeCategory;
import forestry.factory.recipes.jei.still.StillRecipeCategory;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;

@JeiPlugin
public class FactoryJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return ForestryModuleIds.FACTORY;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(
				new CarpenterRecipeCategory(guiHelper),
				new CentrifugeRecipeCategory(guiHelper),
				new FabricatorRecipeCategory(guiHelper),
				new FermenterRecipeCategory(guiHelper),
				new MoistenerRecipeCategory(guiHelper),
				new RainmakerRecipeCategory(guiHelper),
				new SqueezerRecipeCategory(guiHelper),
				new StillRecipeCategory(guiHelper)
		);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		RecipeManager manager = ClientsideCode.getRecipeManager();

		registry.addRecipes(ForestryRecipeType.CARPENTER, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.CARPENTER).toList());
		registry.addRecipes(ForestryRecipeType.CENTRIFUGE, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.CENTRIFUGE).toList());
		registry.addRecipes(ForestryRecipeType.FABRICATOR, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.FABRICATOR).toList());
		registry.addRecipes(ForestryRecipeType.FERMENTER, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.FERMENTER).toList());
		registry.addRecipes(ForestryRecipeType.MOISTENER, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.MOISTENER).toList());
		registry.addRecipes(ForestryRecipeType.RAINMAKER, FuelManager.rainSubstrate.values().stream()
				.sorted(Comparator.comparing(RainSubstrate::duration))
				.toList());
		registry.addRecipes(ForestryRecipeType.SQUEEZER, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.SQUEEZER).toList());
		registry.addRecipes(ForestryRecipeType.STILL, RecipeUtils.getRecipes(manager, FactoryRecipeTypes.STILL).toList());

		BlockFactoryPlain rainTank = FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.RAINTANK).block();
		JeiUtil.addDescription(registry, rainTank);
		JeiUtil.addDescription(registry, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.BOTTLER).block());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
		registry.addRecipeTransferHandler(new CarpenterRecipeTransferHandler(), ForestryRecipeType.CARPENTER);
		registry.addRecipeTransferHandler(new FabricatorRecipeTransferHandler(), ForestryRecipeType.FABRICATOR);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER).block()), ForestryRecipeType.CARPENTER);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE).block()), ForestryRecipeType.CENTRIFUGE);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).block()), ForestryRecipeType.FABRICATOR);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER).block()), ForestryRecipeType.FERMENTER);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER).block()), ForestryRecipeType.MOISTENER);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.RAINTANK).block()), ForestryRecipeType.RAINMAKER);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.SQUEEZER).block()), ForestryRecipeType.SQUEEZER);
		registry.addRecipeCatalyst(new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.STILL).block()), ForestryRecipeType.STILL);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registry) {
		registry.addGenericGuiContainerHandler(GuiForestry.class, new ForestryAdvancedGuiHandler(registry.getJeiHelpers().getIngredientManager()));

		registry.addRecipeClickArea(GuiCarpenter.class, 98, 48, 21, 26, ForestryRecipeType.CARPENTER);

		registry.addRecipeClickArea(GuiCentrifuge.class, 38, 22, 38, 14, ForestryRecipeType.CENTRIFUGE);
		registry.addRecipeClickArea(GuiCentrifuge.class, 38, 54, 38, 14, ForestryRecipeType.CENTRIFUGE);

		registry.addRecipeClickArea(GuiFabricator.class, 121, 53, 18, 18, ForestryRecipeType.FABRICATOR);

		registry.addRecipeClickArea(GuiFermenter.class, 72, 40, 32, 18, ForestryRecipeType.FERMENTER);

		registry.addRecipeClickArea(GuiMoistener.class, 123, 35, 19, 21, ForestryRecipeType.MOISTENER);

		registry.addRecipeClickArea(GuiSqueezer.class, 76, 41, 43, 16, ForestryRecipeType.SQUEEZER);

		registry.addRecipeClickArea(GuiStill.class, 73, 17, 33, 57, ForestryRecipeType.STILL);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
		IIngredientSubtypeInterpreter<ItemStack> subtypeInterpreter = (itemStack, context) -> {
			LazyOptional<IFluidHandlerItem> fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
			return fluidHandler.map(handler -> handler.getFluidInTank(0))
					.map(fluid -> ModUtil.getRegistryName(fluid.getFluid()))
					.map(ResourceLocation::toString)
					.orElse(IIngredientSubtypeInterpreter.NONE);
		};

		for (Item container : FluidsItems.CONTAINERS.itemArray()) {
			subtypeRegistry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, container, subtypeInterpreter);
		}
	}


	static class ForestryAdvancedGuiHandler implements IGuiContainerHandler<GuiForestry<?>> {
		private final IIngredientManager manager;

		ForestryAdvancedGuiHandler(IIngredientManager manager) {
			this.manager = manager;
		}

		@Override
		public List<Rect2i> getGuiExtraAreas(GuiForestry<?> guiContainer) {
			return guiContainer.getExtraGuiAreas();
		}

		@Override
		public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(GuiForestry<?> guiContainer, double mouseX, double mouseY) {
			TankWidget widget = guiContainer.getTankAtPosition(mouseX, mouseY);

			if (widget != null && widget.getTank() != null) {
				return manager.createTypedIngredient(ForgeTypes.FLUID_STACK, widget.getTank().getFluid()).map(ingredient -> new IClickableIngredient<FluidStack>() {
					@Override
					public ITypedIngredient<FluidStack> getTypedIngredient() {
						return ingredient;
					}

					@Override
					public Rect2i getArea() {
						return new Rect2i(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
					}
				});
			} else {
				return Optional.empty();
			}
		}
	}
}
