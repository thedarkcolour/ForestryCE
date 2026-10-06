package forestry.api.arboriculture.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.arboriculture.IArboristTracker;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.plugin.IArboricultureRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ITreeSpeciesType extends ISpeciesType<ITreeSpecies, ITree> {
	@Override
	IArboristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile);

	/**
	 * @return The fruit registered with the given ID. Backs the {@code fruits} reference chromosome.
	 */
	IFruit getFruit(ResourceLocation id);

	/**
	 * @return The fruit registered with the given ID, or {@code null} if none is registered (graceful fallback variant).
	 */
	@Nullable
	IFruit getFruitSafe(ResourceLocation id);

	/**
	 * @return The tree effect registered with the given ID. Backs the {@code tree_effect} reference chromosome.
	 */
	ITreeEffect getTreeEffect(ResourceLocation id);

	/**
	 * Register a leaf random tick handler. Used for butterfly spawner.
	 */
	void registerLeafTickHandler(ILeafTickHandler handler);

	Collection<ILeafTickHandler> getLeafTickHandlers();

	@Nullable
	ITree getTree(Level level, BlockPos pos);

	ITree getTree(IGenome genome);

	@Nullable
	ITree getTree(BlockEntity tileEntity);

	boolean plantSapling(Level level, ITree tree, GameProfile owner, BlockPos pos);

	// todo move this logic to ITreeSpecies so individual species can override placement behavior
	boolean setFruitBlock(LevelAccessor level, IGenome genome, IFruit fruit, float yield, BlockPos pos);

	/**
	 * Tries to get genetic information of a species from a mundane/vanilla block, like from Oak Leaves to Apple Oak Leaves.
	 * Does not affect the block in the world, just returns an individual.
	 *
	 * @param state A vanilla block, like oak leaves.
	 * @return The individual for this vanilla block, or {@code null} if none exists for this block.
	 */
	@Nullable
	ITree getVanillaIndividual(BlockState state);

	/**
	 * Tries to get genetic information of a species from a mundane/vanilla item, like Oak Sapling to Apple Oak Sapling.
	 * Does not affect the item, just returns a default individual.
	 *
	 * @param item A mundane/vanilla item like Oak Sapling.
	 * @return The individual for this vanilla item, or {@code null} if none exists for this item.
	 */
	@Nullable
	ITree getVanillaIndividual(Item item);

	/**
	 * Creates a data provider that generates tree species JSON.
	 *
	 * @param output     The pack output to generate into
	 * @param registries The registries used to encode the species
	 * @param species    The action that registers the species to generate
	 * @return The data provider to add to the data generator
	 */
	DataProvider createSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<IArboricultureRegistration> species);
}
