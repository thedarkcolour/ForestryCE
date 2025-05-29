package forestry.apiculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.ItemCreativeHiveFrame;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.SpeciesUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ApicultureJeiPlugin implements IModPlugin {
	static final ResourceLocation BACKGROUND = ForestryConstants.forestry("textures/gui/jei/recipes.png");
	static final List<ProductsRecipeCategory> productsCategories = new ArrayList<>();
	static final List<MutationsRecipeCategory> mutationsCategories = new ArrayList<>();

	@Override
	public ResourceLocation getPluginUid() {
		return ForestryModuleIds.APICULTURE;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
		IDrawable productsBackground = helper.createDrawable(BACKGROUND, 0, 61 + 30, 162, 61);
		IDrawable mutationsBackground = helper.createDrawable(BACKGROUND, 0, 30, 162, 61);

		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			IDrawable icon = helper.createDrawableItemStack(type.createDefaultStack());

			// products
			if (type.getDefaultSpecies() instanceof IProductProducer) {
				ProductsRecipeCategory category = new ProductsRecipeCategory(type, productsBackground, icon);
				productsCategories.add(category);
				registration.addRecipeCategories(category);
			}
			// mutations
			MutationsRecipeCategory category = new MutationsRecipeCategory(type, mutationsBackground, icon);
			mutationsCategories.add(category);
			registration.addRecipeCategories(category);
		}
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registry) {
		JeiUtil.registerItemSubtypes(registry, BeeChromosomes.SPECIES, SpeciesUtil.BEE_TYPE.get());
		// show both creative frames in JEI
		registry.registerSubtypeInterpreter(ApicultureItems.FRAME_CREATIVE.item(), (stack, context) -> String.valueOf(ItemCreativeHiveFrame.hasForceMutations()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		JeiUtil.addDescription(registry, "frames",
			ApicultureItems.FRAME_IMPREGNATED,
			ApicultureItems.FRAME_PROVEN,
			ApicultureItems.FRAME_UNTREATED
		);

		JeiUtil.addDescription(registry, "apiarist.suit",
			ApicultureItems.APIARIST_BOOTS,
			ApicultureItems.APIARIST_CHEST,
			ApicultureItems.APIARIST_HELMET,
			ApicultureItems.APIARIST_LEGS
		);

		JeiUtil.addDescription(registry, ApicultureItems.SCOOP);

		for (MutationsRecipeCategory category : mutationsCategories) {
			registry.addRecipes(category.getRecipeType(), category.speciesType.getMutations().getAllMutations().stream().map(MutationRecipe::new).toList());
		}
		for (ProductsRecipeCategory category : productsCategories) {
			registry.addRecipes(category.getRecipeType(), category.speciesType.getAllSpecies().stream()
				.filter(species -> {
					// filter out species with no products or specialties
					return (species instanceof IProductProducer producer && !producer.getProducts().isEmpty())
						|| (species instanceof ISpecialtyProducer specialtyProducer && !specialtyProducer.getSpecialties().isEmpty());
				})
				.map(species -> new ProductRecipe(species.cast()))
				.toList());
		}
		mutationsCategories.clear();
		productsCategories.clear();
	}
}
