package forestry.api.arboriculture.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.arboriculture.IArboristTracker;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ITreeSpeciesType extends ISpeciesType<ITreeSpecies, ITree> {
	@Override
	IArboristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile);

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
}
