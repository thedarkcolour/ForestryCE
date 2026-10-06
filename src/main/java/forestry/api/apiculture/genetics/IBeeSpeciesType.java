package forestry.api.apiculture.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.plugin.IApicultureRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

// todo reimplement beekeeping mode
public interface IBeeSpeciesType extends ISpeciesType<IBeeSpecies, IBee> {
	/**
	 * @return {@link IApiaristTracker} associated with the passed world.
	 */
	@Override
	IApiaristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile);



	/**
	 * @return The bee effect registered with the given ID. Backs the {@code bee_effect} reference chromosome.
	 */
	IBeeEffect getBeeEffect(ResourceLocation id);

	/**
	 * @return The activity type registered with the given ID. Backs the {@code activity} reference chromosome.
	 */
	IActivityType getActivityType(ResourceLocation id);

	/**
	 * @return The bee jubilance registered with the given ID. Backs the {@code jubilance} reference chromosome.
	 */
	IBeeJubilance getJubilance(ResourceLocation id);

	/**
	 * @return The bee jubilance registered with the given ID, or {@code null} if none is registered (graceful fallback variant).
	 */
	@Nullable
	IBeeJubilance getJubilanceSafe(ResourceLocation id);

	/**
	 * @return true if passed item is a drone. Equal to getLifeStage(ItemStack stack) == EnumBeeType.DRONE
	 */
	boolean isDrone(ItemStack stack);

	/**
	 * @return true if passed item is mated (i.e. a queen)
	 */
	boolean isMated(ItemStack stack);

	/**
	 * Creates a data provider that generates bee species JSON.
	 *
	 * Each jubilance ID used by a species must be registered, either by a plugin or by the action.
	 * Otherwise, the data provider throws when it runs.
	 *
	 * @param output     The pack output to generate into
	 * @param registries The registries used to encode the species
	 * @param species    The action that registers the species to generate
	 * @return The data provider to add to the data generator
	 */
	DataProvider createSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<IApicultureRegistration> species);
}
