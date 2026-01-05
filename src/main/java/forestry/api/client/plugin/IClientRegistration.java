package forestry.api.client.plugin;

import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.genetics.ILifeStage;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles client registration for Forestry plugins.
 * To access, override {@link forestry.api.plugin.IForestryPlugin#registerClient}. Here's an example of doing so
 * safely without loading client-only code on the server:
 * <pre>
 * {@code
 * public class MyForestryPlugin implements IForestryPlugin {
 *     @Override
 *     public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
 *         registrar.accept(new MyForestryClientRegistration());
 *     }
 * }
 * }
 * </pre>
 * <pre>
 * {@code
 * public class MyForestryClientRegistration implements Consumer<IClientRegistration> {
 *     @Override
 *     public void accept(IClientRegistration client) {
 *         client.setLeafSprite(MySpecies.SUPER_OAK, ForestryLeafSprites.WILLOW);
 *         // ... and more
 *     }
 * }
 * }
 * </pre>
 */
public interface IClientRegistration {
	/**
	 * Registers a default model for bees of a given life stage. Used by base Forestry to configure default bee models.
	 * If your plugin adds a new {@link ILifeStage} to the bee species type, you must call this method so that default
	 * models are available for other addons that utilize bee models.
	 *
	 * @param stage         The life stage to set the default model for.
	 * @param modelLocation The location of the model to use.
	 */
	void setDefaultBeeModel(ILifeStage stage, ResourceLocation modelLocation);

	/**
	 * Registers a custom model for the given bee species when at the given life stage.
	 *
	 * @param speciesId The ID of the bee species to register a custom model for.
	 * @param stage     The block model for the sapling.
	 * @param model     The location of the item model for the bee species when at the given life stage.
	 */
	void setCustomBeeModel(ResourceLocation speciesId, ILifeStage stage, ResourceLocation model);

	/**
	 * Registers the block and item models for a tree species's saplings. If you do not call this, Forestry will
	 * use {@code "<namespace>:block/sapling/<id without tree_>"} and {@code "<namespace>:item/sapling/<id without tree_>"} as defaults.
	 *
	 * @param speciesId  The ID of the tree species to register models for.
	 * @param blockModel The block model for the sapling.
	 * @param itemModel  The item model for the sapling.
	 */
	void setSaplingModel(ResourceLocation speciesId, ResourceLocation blockModel, ResourceLocation itemModel);

	/**
	 * Sets the leaf sprite of this species. Required for all tree species.
	 *
	 * @param speciesId The ID of the tree species to register models for.
	 * @param sprite    The sprite used for leaf rendering. Can be reused for multiple species.
	 */
	void setLeafSprite(ResourceLocation speciesId, ILeafSprite sprite);

	/**
	 * Sets the custom leaf tint object of this species. If none is registered, then a default tint based on the
	 * escritoire color assigned to the tree species will be used instead.
	 *
	 * @param speciesId The ID of the tree species to set the tint for.
	 * @param tint      The tint. Can be reused for multiple species.
	 */
	void setLeafTint(ResourceLocation speciesId, ILeafTint tint);

	/**
	 * Sets the sprites used by Forestry butterflies for item/entity rendering. If none is set, Forestry will
	 * use {@code "<namespace>:item/butterfly/<id without butterfly_>"} and {@code "<namespace>:textures/entity/butterfly/<id without butterfly_>.png"} as defaults.
	 * The texture maps are different between the item and entity models, so the returned textures should be different too.
	 *
	 * @param speciesId     The ID of the species to set the sprite for.
	 * @param itemTexture   The path to the item texture.
	 * @param entityTexture The path to the entity texture.
	 */
	void setButterflySprites(ResourceLocation speciesId, ResourceLocation itemTexture, ResourceLocation entityTexture);

	void setAnalyzerPlugin(ResourceLocation speciesTypeId, IAnalyzerPlugin<?, ?> plugin);
}
