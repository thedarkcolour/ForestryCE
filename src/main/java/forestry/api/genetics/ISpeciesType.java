package forestry.api.genetics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;

/**
 * Represents a type of species/individuals. Replaces the old ISpeciesRoot.
 * Register new species types in {@link IForestryPlugin#registerGenetics}.
 *
 * @param <S> The interface type of the species this type contains.
 * @param <I> The interface type of individuals who are members of this species type.
 */
public interface ISpeciesType<S extends ISpecies<I>, I extends IIndividual> extends IBreedingTrackerHandler {
	/**
	 * @return The unique ID of this species type.
	 */
	ResourceLocation id();

	/**
	 * @return The karyotype for all members of this species type.
	 */
	IKaryotype getKaryotype();

	/**
	 * @return The life stage of the individual contained in the item, or {@code null} if the item is not valid.
	 */
	@Nullable
	ILifeStage getLifeStage(ItemStack stack);

	/**
	 * @return The mutation manager for this species type.
	 * @throws IllegalStateException If not all mutations have been registered.
	 */
	IMutationManager<S> getMutations();

	/**
	 * @return The list of all members of this species type.
	 * @throws IllegalStateException If not all species have been registered yet.
	 */
	List<S> getAllSpecies();

	/**
	 * @return The species of this type registered with the given ID.
	 * @throws RuntimeException      If no species was found with that ID.
	 * @throws IllegalStateException If not all species have been registered yet.
	 */
	S getSpecies(ResourceLocation id);

	/**
	 * @return The species of this type registered with the given ID, or {@code null} if no species was found with that ID.
	 * @throws IllegalStateException If not all species have been registered yet.
	 */
	@Nullable
	S getSpeciesSafe(ResourceLocation id);

	/**
	 * @return A random species from all registered species of this type.
	 */
	S getRandomSpecies(RandomSource rand);

	/**
	 * @return A collection of IDs for all species of this type, including secret species. Used for commands.
	 */
	ImmutableSet<ResourceLocation> getAllSpeciesIds();

	/**
	 * @return The number of species of this type, <i>including secret species.</i>
	 * @throws IllegalStateException If not all species have been registered yet.
	 */
	int getSpeciesCount();

	/**
	 * @return The default species for members of this type. For bees, this is the Forest species.
	 */
	S getDefaultSpecies();

	/**
	 * @return The life stage used to represent this species in icons.
	 */
	ILifeStage getDefaultStage();

	/**
	 * @return The translation key for this species type. Ex. "species_type.forestry.bee" -> "Bee"
	 * @since 2.2.0
	 */
	String getTranslationKey();

	/**
	 * @return The display name for this species type. Ex. "Bee" or "Butterfly"
	 * @since 2.2.0
	 */
	default MutableComponent getDisplayName() {
		return Component.translatable(getTranslationKey());
	}

	/**
	 * @return A new item stack with the default species and life stage of this species type.
	 */
	default ItemStack createDefaultStack() {
		return getDefaultSpecies().createStack(getDefaultStage());
	}

	/**
	 * @return All known life stages of this species. Used for commands.
	 */
	Collection<ILifeStage> getLifeStages();

	/**
	 * Gets the player's breeding tracker for this species type.
	 *
	 * @param level   The world instance where the breeding tracker is stored.
	 * @param profile The player whose breeding tracker should be returned.
	 * @return The breeding tracker for species of this type.
	 */
	IBreedingTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile);

	/**
	 * The type of the species that will be displayed at the given position of the mutation recipe in the gui.
	 *
	 * @param position 0 = first parent, 1 = second parent, 2 = result
	 */
	default ILifeStage getTypeForMutation(int position) {
		return getDefaultStage();
	}

	/**
	 * Plugin to add information for the handheld genetic analyzer.
	 */
	IAlyzerPlugin getAlyzerPlugin();

	ItemStack createStack(I individual, ILifeStage type);

	ItemStack createStack(ResourceLocation speciesId, ILifeStage stage);

	/**
	 * @return The codec used to serialize/deserialize individuals of this species.
	 */
	Codec<? extends I> getIndividualCodec();

	/**
	 * Used to check whether the given {@link IIndividual} is member of this class.
	 *
	 * @param individual {@link IIndividual} to check.
	 * @return true if the individual is member of this class, false otherwise.
	 */
	boolean isMember(IIndividual individual);

	@SuppressWarnings({"DataFlowIssue", "ConstantValue"})
	default boolean isMember(ItemStack stack) {
		IIndividualHandlerItem individual = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM).orElse(null);
		return individual != null && isMember(individual.getIndividual());
	}

	/**
	 * Used to determine what items can be used as research materials in the Escritoire and how effective they are for probing.
	 * For an item to be accepted in the research material slots in the Escritoire, this method must return a number greater than
	 * zero for that item. An item that returns 1 is guaranteed to reveal slots on the board.
	 *
	 * @param species The species being researched.
	 * @param stack   The item to be checked for research purposes.
	 * @return The chance that a research attempt (clicking the microscope button) will successfully reveal cells on the board.
	 */
	float getResearchSuitability(S species, ItemStack stack);

	/**
	 * Used to generate rewards for players who win the Escritoire's memory game when researching a specimen.
	 *
	 * @param species     The species that was researched.
	 * @param level       The world.
	 * @param researcher  The profile of the player who completed this research.
	 * @param individual  The specimen that was researched.
	 * @param bountyLevel The bounty level starts at 16 and goes down by 1 for every probe (usage of the microscope button).
	 *                    The lowest level is 1. Rewards players for memorizing the tiles and using good research material.
	 * @return A list of reward items granted upon researching a specimen in the Escritoire. Might be empty.
	 */
	List<ItemStack> getResearchBounty(S species, Level level, GameProfile researcher, I individual, int bountyLevel);

	/**
	 * @return The name of the breeding tracker save file for the given player.
	 */
	String getBreedingTrackerFile(@Nullable GameProfile profile);

	/**
	 * @return A new breeding tracker.
	 */
	IBreedingTracker createBreedingTracker();

	/**
	 * @return A breeding tracker with data loaded from a previous save file.
	 */
	IBreedingTracker createBreedingTracker(CompoundTag nbt);

	/**
	 * @return A new individual of a random species using the default genome of the chosen species.
	 */
	I createRandomIndividual(RandomSource rand);

	/**
	 * Used to initialize a breeding tracker with additional information.
	 *
	 * @param tracker The tracker to add information to.
	 * @param world   The world this tracker is saved to. Always the overworld dimension.
	 * @param profile The player to whom the breeding tracker belongs to.
	 */
	void initializeBreedingTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile);

	/**
	 * Used to register species and related data for this species type from an {@link IForestryPlugin}.
	 * IForestryPlugin already contains methods for each species type added by Forestry. For a modded species type,
	 * it is recommended to offer an additional interface to be implemented by IForestryPlugins in order to handle
	 * registration for the custom species type (ex. IBotanyPluginExtension for Binnie's flower species type) and then
	 * checking if the plugin implements that interface.
	 *
	 * @param plugins The list of plugins responsible for registering species and data.
	 * @return The map of every species registered to this species type, which later gets passed
	 * to {@link #onSpeciesRegistered}, and the completed mutations manager for this species type.
	 * @see IForestryPlugin#registerApiculture(IApicultureRegistration) for an example of what data is registered.
	 */
	Pair<ImmutableMap<ResourceLocation, S>, IMutationManager<S>> handleSpeciesRegistration(List<IForestryPlugin> plugins);

	/**
	 * Called when all species of this type have been registered and modified.
	 *
	 * @param allSpecies The map of every species ID to its species.
	 * @param mutations  The mutations for this species type.
	 */
	void onSpeciesRegistered(ImmutableMap<ResourceLocation, S> allSpecies, IMutationManager<S> mutations);

	/**
	 * @return This species type casted to a subclass of ISpeciesType.
	 */
	default <T extends ISpeciesType<?, ?>> T cast() {
		return (T) this;
	}
}
