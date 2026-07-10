package forestry.plugin.client;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.client.arboriculture.ForestryLeafSprites;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.arboriculture.client.BiomeLeafTint;
import forestry.arboriculture.client.FixedLeafTint;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class DefaultForestryClientRegistration implements Consumer<IClientRegistration> {
	@Override
	public void accept(IClientRegistration client) {
		registerApiculture(client);
		registerArboriculture(client);
		registerLepidopterology(client);
	}

	private static void registerApiculture(IClientRegistration client) {
		client.setAnalyzerPlugin(ForestrySpeciesTypes.BEE, new BeeAnalyzerPlugin());

		client.setDefaultBeeModel(BeeLifeStage.DRONE, ForestryConstants.forestry("item/bee_drone_default"));
		client.setDefaultBeeModel(BeeLifeStage.PRINCESS, ForestryConstants.forestry("item/bee_princess_default"));
		client.setDefaultBeeModel(BeeLifeStage.QUEEN, ForestryConstants.forestry("item/bee_queen_default"));
		client.setDefaultBeeModel(BeeLifeStage.LARVAE, ForestryConstants.forestry("item/bee_larvae_default"));
		client.setCustomBeeModel(ForestryBeeSpecies.VANILLA, BeeLifeStage.DRONE, ForestryConstants.forestry("item/bee_drone_cube"));
		client.setCustomBeeModel(ForestryBeeSpecies.VANILLA, BeeLifeStage.PRINCESS, ForestryConstants.forestry("item/bee_princess_cube"));
		client.setCustomBeeModel(ForestryBeeSpecies.VANILLA, BeeLifeStage.QUEEN, ForestryConstants.forestry("item/bee_queen_cube"));
	}

	private static void registerArboriculture(IClientRegistration client) {
		client.setAnalyzerPlugin(ForestrySpeciesTypes.TREE, new TreeAnalyzerPlugin());

		// Vanilla sapling models
		registerSapling(client, "minecraft", ForestryTreeSpecies.OAK);
		registerSapling(client, "minecraft", ForestryTreeSpecies.DARK_OAK);
		registerSapling(client, "minecraft", ForestryTreeSpecies.BIRCH);
		registerSapling(client, "minecraft", ForestryTreeSpecies.ACACIA_VANILLA);
		registerSapling(client, "minecraft", ForestryTreeSpecies.SPRUCE);
		registerSapling(client, "minecraft", ForestryTreeSpecies.JUNGLE);
		registerSapling(client, "minecraft", ForestryTreeSpecies.CHERRY_VANILLA);

		// Vanilla leaf sprites
		client.setLeafSprite(ForestryTreeSpecies.OAK, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.DARK_OAK, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.BIRCH, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.ACACIA_VANILLA, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.SPRUCE, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.JUNGLE, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.CHERRY_VANILLA, ForestryLeafSprites.CHERRY);

		// Forestry leaf sprites
		client.setLeafSprite(ForestryTreeSpecies.LIME, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.WALNUT, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.CHESTNUT, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.SOUR_CHERRY, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.LEMON, ForestryLeafSprites.AZALEA);
		client.setLeafSprite(ForestryTreeSpecies.PLUM, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.MAPLE, ForestryLeafSprites.MAPLE);
		client.setLeafSprite(ForestryTreeSpecies.LARCH, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.PINE, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.SEQUOIA, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.GIANT_SEQUOIA, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.TEAK, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.IPE, ForestryLeafSprites.IPE);
		client.setLeafSprite(ForestryTreeSpecies.KAPOK, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.EBONY, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.ZEBRANO, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.MAHOGANY, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.CAMELTHORN, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.PADAUK, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.BALSA, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.COCOBOLO, ForestryLeafSprites.MANGROVE);
		client.setLeafSprite(ForestryTreeSpecies.WENGE, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.BAOBAB, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.MAHOE, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.WILLOW, ForestryLeafSprites.WILLOW);
		client.setLeafSprite(ForestryTreeSpecies.GREENHEART, ForestryLeafSprites.MANGROVE);
		client.setLeafSprite(ForestryTreeSpecies.PAPAYA, ForestryLeafSprites.PALM);
		client.setLeafSprite(ForestryTreeSpecies.DATE, ForestryLeafSprites.PALM);
		client.setLeafSprite(ForestryTreeSpecies.POPLAR, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.ELM, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.FIR, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.COCONUT, ForestryLeafSprites.PALM);
		client.setLeafSprite(ForestryTreeSpecies.BEECH, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.FEIJOA, ForestryLeafSprites.AZALEA);
		client.setLeafSprite(ForestryTreeSpecies.DOGWOOD, ForestryLeafSprites.DOGWOOD);
		client.setLeafSprite(ForestryTreeSpecies.GINKGO, ForestryLeafSprites.GINKGO);
		client.setLeafSprite(ForestryTreeSpecies.JACARANDA, ForestryLeafSprites.JACARANDA);
		client.setLeafSprite(ForestryTreeSpecies.PEWEN, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.MACROCARPA, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.OLIVE, ForestryLeafSprites.WILLOW);
		client.setLeafSprite(ForestryTreeSpecies.ORANGE, ForestryLeafSprites.AZALEA);
		client.setLeafSprite(ForestryTreeSpecies.PEAR, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.KAURI, ForestryLeafSprites.SPRUCE);

		// Vanilla leaf tints
		client.setLeafTint(ForestryTreeSpecies.OAK, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.DARK_OAK, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.JUNGLE, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.ACACIA_VANILLA, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.CHERRY_VANILLA, FixedLeafTint.NONE);

		// Modded leaf tints
		client.setLeafTint(ForestryTreeSpecies.DOGWOOD, FixedLeafTint.NONE);
		client.setLeafTint(ForestryTreeSpecies.JACARANDA, FixedLeafTint.NONE);
		client.setLeafTint(ForestryTreeSpecies.IPE, FixedLeafTint.NONE);
	}

	private static void registerSapling(IClientRegistration registration, String modId, ResourceLocation speciesId) {
		// add "_sapling" (no longer need to remove "tree_" prefix)
		String path = speciesId.getPath() + "_sapling";

		ResourceLocation blockModel = ResourceLocation.fromNamespaceAndPath(modId, "block/" + path);
		ResourceLocation itemModel = ResourceLocation.fromNamespaceAndPath(modId, "item/" + path);
		registration.setSaplingModel(speciesId, blockModel, itemModel);
	}

	private static void registerLepidopterology(IClientRegistration client) {
		client.setAnalyzerPlugin(ForestrySpeciesTypes.BUTTERFLY, new ButterflyAnalyzerPlugin());

		// Register the default item/entity texture naming convention for every built-in butterfly species, so
		// ButterflyItemModel can bake a per-species model instead of falling back to the default (cabbage white)
		// for all of them. Sourced from the static compile-time id list, not the live/reloadable species map.
		for (ResourceLocation speciesId : ForestryButterflySpecies.ALL) {
			registerDefaultButterflyTextures(client, speciesId);
		}
	}

	private static void registerDefaultButterflyTextures(IClientRegistration client, ResourceLocation speciesId) {
		// Mirrors ButterflyClientManager#defaultTexturesFor's render-time fallback naming convention.
		String path = speciesId.getPath().replace("butterfly_", "");
		ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath(speciesId.getNamespace(), "item/butterfly/" + path);
		ResourceLocation entityTexture = ResourceLocation.fromNamespaceAndPath(speciesId.getNamespace(), "textures/entity/butterfly/" + path + ".png");
		client.setButterflySprites(speciesId, itemTexture, entityTexture);
	}
}
