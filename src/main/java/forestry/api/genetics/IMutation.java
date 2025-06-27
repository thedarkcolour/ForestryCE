package forestry.api.genetics;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import forestry.api.genetics.alleles.AllelePair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.List;

/**
 * Represents a mutation between two species (in any order) into a third species.
 * A mutation should never have both parents of the same species.
 */
public interface IMutation<S extends ISpecies<?>> {
	/**
	 * @return {@link ISpeciesType} this mutation is associated with.
	 */
	ISpeciesType<S, ?> getType();

	/**
	 * @return One of the species required for this mutation.
	 */
	S getFirstParent();

	/**
	 * @return The other species required for this mutation.
	 */
	S getSecondParent();

	/**
	 * @return The species resulting from this mutation.
	 */
	S getResult();

	/**
	 * Returns the allele pairs of individuals resulting from this mutation. The ordering of the allele pairs matches
	 * the ordering of their respective chromosomes in the {@link forestry.api.genetics.alleles.IKaryotype} of species type.
	 *
	 * @return The list of alleles the resulting individual should have. Usually the default genome of result species.
	 */
	ImmutableList<AllelePair<?>> getResultAlleles();

	/**
	 * @return Unmodified base chance for mutation to occur.
	 */
	float getChance();

	/**
	 * @return Collection of localized, human-readable strings describing special mutation conditions, if any.
	 */
	List<Component> getSpecialConditions();

	/**
	 * @return List of all conditions required for this mutation to occur.
	 */
	List<IMutationCondition> getConditions();

	/**
	 * @return {@code true} if the passed species is one of the alleles participating in this mutation.
	 */
	boolean isPartner(ISpecies<?> species);

	/**
	 * @return The other parent in this mutation besides the given species.
	 */
	ISpecies<?> getPartner(ISpecies<?> species);

	/**
	 * @return true if the mutation should not be displayed in a gui that displays all mutations.
	 */
	boolean isSecret();

	ItemStack getMutationNote(ResolvableProfile researcher);
}
