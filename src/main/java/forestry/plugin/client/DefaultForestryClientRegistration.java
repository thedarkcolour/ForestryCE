package forestry.plugin.client;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.client.arboriculture.ForestryLeafSprites;
import forestry.api.client.plugin.IClientRegistration;
import forestry.arboriculture.client.BiomeLeafTint;
import forestry.arboriculture.client.FixedLeafTint;

public class DefaultForestryClientRegistration implements Consumer<IClientRegistration> {
	@Override
	public void accept(IClientRegistration client) {
		registerApiculture(client);
		registerArboriculture(client);
		registerLepidopterology(client);
	}

	private static void registerApiculture(IClientRegistration client) {
		client.setDefaultBeeModel(BeeLifeStage.DRONE, ForestryConstants.forestry("item/bee_drone_default"));
		client.setDefaultBeeModel(BeeLifeStage.PRINCESS, ForestryConstants.forestry("item/bee_princess_default"));
		client.setDefaultBeeModel(BeeLifeStage.QUEEN, ForestryConstants.forestry("item/bee_queen_default"));
		client.setDefaultBeeModel(BeeLifeStage.LARVAE, ForestryConstants.forestry("item/bee_larvae_default"));
	}

	private static void registerArboriculture(IClientRegistration client) {
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
		client.setLeafSprite(ForestryTreeSpecies.HILL_CHERRY, ForestryLeafSprites.BIRCH);
		client.setLeafSprite(ForestryTreeSpecies.LEMON, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.PLUM, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.MAPLE, ForestryLeafSprites.MAPLE);
		client.setLeafSprite(ForestryTreeSpecies.LARCH, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.PINE, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.SEQUOIA, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.GIANT_SEQUOIA, ForestryLeafSprites.SPRUCE);
		client.setLeafSprite(ForestryTreeSpecies.TEAK, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.IPE, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.KAPOK, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.EBONY, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.ZEBRAWOOD, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.MAHOGANY, ForestryLeafSprites.JUNGLE);
		client.setLeafSprite(ForestryTreeSpecies.DESERT_ACACIA, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.PADAUK, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.BALSA, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.COCOBOLO, ForestryLeafSprites.MANGROVE);
		client.setLeafSprite(ForestryTreeSpecies.WENGE, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.BAOBAB, ForestryLeafSprites.ACACIA);
		client.setLeafSprite(ForestryTreeSpecies.MAHOE, ForestryLeafSprites.OAK);
		client.setLeafSprite(ForestryTreeSpecies.WILLOW, ForestryLeafSprites.WILLOW);
		client.setLeafSprite(ForestryTreeSpecies.SIPIRI, ForestryLeafSprites.MANGROVE);
		client.setLeafSprite(ForestryTreeSpecies.PAPAYA, ForestryLeafSprites.PALM);
		client.setLeafSprite(ForestryTreeSpecies.DATE, ForestryLeafSprites.PALM);
		client.setLeafSprite(ForestryTreeSpecies.POPLAR, ForestryLeafSprites.BIRCH);

		// Vanilla leaf tints
		client.setLeafTint(ForestryTreeSpecies.OAK, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.DARK_OAK, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.JUNGLE, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.ACACIA_VANILLA, BiomeLeafTint.DEFAULT);
		client.setLeafTint(ForestryTreeSpecies.CHERRY_VANILLA, FixedLeafTint.NONE);
	}

	private static void registerSapling(IClientRegistration registration, String modId, ResourceLocation speciesId) {
		// remove the "tree/" prefix and add "_sapling"
		String path = speciesId.getPath().substring(5) + "_sapling";
		ResourceLocation blockModel = new ResourceLocation(modId, "block/" + path);
		ResourceLocation itemModel = new ResourceLocation(modId, "item/" + path);
		registration.setSaplingModel(speciesId, blockModel, itemModel);
	}

	private static void registerLepidopterology(IClientRegistration client) {
		// todo
	}
}
