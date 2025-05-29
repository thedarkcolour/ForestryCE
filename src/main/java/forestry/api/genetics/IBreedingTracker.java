package forestry.api.genetics;

import forestry.api.genetics.alleles.IAllele;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

/**
 * Keeps track of who bred, discovered, and researched which species in a world.
 */
public interface IBreedingTracker {
	/**
	 * @return Amount of species discovered.
	 */
	int getSpeciesBred();

	/**
	 * Register the birth of a species. Will mark it as discovered.
	 */
	void registerBirth(ISpecies<?> species);

	/**
	 * Register the pickup of a species.
	 */
	void registerPickup(ISpecies<?> species);

	/**
	 * Marks a species as discovered. Should only be called from registerIndividual normally.
	 */
	void registerSpecies(ISpecies<?> species);

	/**
	 * Register a successful mutation. Will mark it as discovered.
	 */
	void registerMutation(IMutation<?> mutation);

	/**
	 * Queries the tracker for discovered species.
	 *
	 * @param mutation Mutation to query for.
	 * @return true if the mutation has been discovered.
	 */
	boolean isDiscovered(IMutation<?> mutation);

	/**
	 * Queries the tracker for discovered species.
	 *
	 * @param species Species to check.
	 * @return true if the species has been bred.
	 */
	boolean isDiscovered(ISpecies<?> species);

	/**
	 * @return A collection that contains the {@link IAllele#alleleId()}s of all discovered species.
	 */
	Collection<ResourceLocation> getDiscoveredSpecies();

	/**
	 * Register a successfully researched mutation.
	 * Mutations are normally researched in the Escritoire.
	 * Researched mutations may have bonuses such as occurring at a higher rate.
	 * Researched mutations count as discovered.
	 */
	void researchMutation(IMutation<?> mutation);

	/**
	 * @return true if the mutation has been researched.
	 */
	boolean isResearched(IMutation<?> mutation);

	/**
	 * Synchronizes the ENTIRE breeding tracker to the client side.
	 */
	void syncToPlayer(Player player);

	// todo replace these with "save" and "load" in 1.21
	void readFromNbt(CompoundTag nbt, HolderLookup.Provider registries);

	void writeToNbt(CompoundTag nbt);
}
