package forestry.api.plugin;

import java.awt.Color;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;

/**
 * Entry point for apiculture related registration.
 * Obtain an instance by overriding {@link IForestryPlugin#registerApiculture}.
 */
public interface IApicultureRegistration {
	/**
	 * @deprecated Use variant that accepts TextColor
	 */
	@Deprecated(forRemoval = true)
	default IBeeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, Color outline) {
		return registerSpecies(id, genus, species, dominant, TextColor.fromRgb(outline.getRGB()));
	}

	/**
	 * Register a new bee species.
	 *
	 * @param id       The unique ID for this species.
	 * @param genus    The scientific name of the genus containing this species. See {@link forestry.api.genetics.ForestryTaxa}.
	 * @param species  The scientific name of this species without the genus. (Ex. mellifera, for <i>Apis mellifera</i>)
	 * @param dominant Whether this species appears as a dominant allele in a genome.
	 * @param outline  The color used for tinting the bee's outline. IntelliJ should show a nice color preview.
	 * @throws IllegalStateException If a species has already been registered with the given ID.
	 * @since 2.3.3 Now accepts TextColor instead of java.awt.Color
	 */
	IBeeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor outline);

	/**
	 * Modify a species that was already registered.
	 * You can change all the same things as in {@link #registerSpecies} except for the genus and species names.
	 *
	 * @param id     The ID of the species to modify.
	 * @param action A function that receives the builder of the species to modify.
	 */
	void modifySpecies(ResourceLocation id, Consumer<IBeeSpeciesBuilder> action);

	/**
	 * Adds a bee species to the possible bees found in apiaries in the Apiarist villager houses.
	 * There are two pools: the common pool, which is rolled 75% of the time, and the rare pool, which is rolled 25% of the time.
	 *
	 * @param speciesId The ID of the species to add.
	 * @param rare      If true, this bee goes into the "rare" village bee pool, which is chosen 25% of the time instead of the "common" pool.
	 * @param alleles   Map of non-default alleles. Example is the rare Tolerant Flyer variant of the Forest species.
	 */
	void addVillageBee(ResourceLocation speciesId, boolean rare, Map<IChromosome<?>, IAllele> alleles);

	/**
	 * Shorthand for adding a village bee that uses the default genome.
	 *
	 * @param speciesId The ID of the species to add.
	 * @param rare      Whether this species is added to the rare pool or the common pool.
	 */
	default void addVillageBee(ResourceLocation speciesId, boolean rare) {
		addVillageBee(speciesId, rare, Map.of());
	}

	/**
	 * Register a wild hive for world generation.
	 *
	 * @param id         The unique ID for this hive.
	 * @param definition The hive definition to register.
	 */
	IHiveBuilder registerHive(ResourceLocation id, IHiveDefinition definition);

	/**
	 * Modifies an already registered hive.
	 *
	 * @param id     The ID of the hive to modify.
	 * @param action The modification to apply to the hive builder.
	 */
	void modifyHive(ResourceLocation id, Consumer<IHiveBuilder> action);

	/**
	 * Register a flower type, a category of blocks that a bee can work with.
	 *
	 * @param id   The unique ID for this flower type.
	 * @param type The flower type to register.
	 */
	void registerFlowerType(ResourceLocation id, IFlowerType type);

	void registerBeeEffect(ResourceLocation id, IBeeEffect effect);

	/**
	 * Register an activity type, which defines activity periods for a bee.
	 *
	 * @param id   The unique ID for this activity type.
	 * @param type The activity type to register.
	 */
	void registerActivityType(ResourceLocation id, IActivityType type);

	/**
	 * Registers an ingredient to be used in the Alveary Swarmer block, which creates swarm hives around the alveary
	 * containing ignoble princesses with copies of the queen's genome.
	 *
	 * @param swarmItem   The item to be used.
	 * @param swarmChance The chance a swarm hive is created. For Royal Jelly, this is {@code 0.01} or 1%.
	 */
	void registerSwarmerMaterial(Item swarmItem, float swarmChance);
}
