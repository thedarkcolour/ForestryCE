package forestry.core.platform.tile;

import forestry.api.IForestryApi;
import forestry.api.core.genetics.ISpeciesType;
import forestry.core.platform.gui.NaturalistInventoryMenu;
import forestry.core.platform.inventory.NaturalistChestInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class NaturalistChestBlockEntity extends BaseBlockEntity {
	private static final float lidAngleVariationPerTick = 0.1F;
	public static final VoxelShape CHEST_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

	private final ResourceLocation speciesTypeId;
	@Nullable
	private ISpeciesType<?, ?> speciesType;
	public float lidAngle;
	public float prevLidAngle;
	private int numPlayersUsing;

	public NaturalistChestBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, ResourceLocation speciesTypeId) {
		super(type, pos, state);
		this.speciesTypeId = speciesTypeId;
		setInternalInventory(new NaturalistChestInventory(this));
	}

	public void increaseNumPlayersUsing() {
		if (this.numPlayersUsing == 0) {
			playLidSound(this.level, SoundEvents.CHEST_OPEN);
		}

		this.numPlayersUsing++;
		sendNetworkUpdate();
	}

	public void decreaseNumPlayersUsing() {
		this.numPlayersUsing--;
		if (this.numPlayersUsing < 0) {
			this.numPlayersUsing = 0;
		}
		if (this.numPlayersUsing == 0) {
			playLidSound(this.level, SoundEvents.CHEST_CLOSE);
		}
		sendNetworkUpdate();
	}

	@Override
	public void clientTick(Level level, BlockPos pos, BlockState state) {
		this.prevLidAngle = this.lidAngle;

		if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
			if (this.numPlayersUsing > 0) {
				this.lidAngle += lidAngleVariationPerTick;
			} else {
				this.lidAngle -= lidAngleVariationPerTick;
			}

			this.lidAngle = Math.max(Math.min(this.lidAngle, 1), 0);
		}
	}

	private void playLidSound(Level level, SoundEvent sound) {
		level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		data.writeInt(this.numPlayersUsing);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		this.numPlayersUsing = data.readInt();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		// this is unused but return a default just in case
		return new NaturalistInventoryMenu(windowId, inv, this);
	}

	public ISpeciesType<?, ?> getSpeciesType() {
		ISpeciesType<?, ?> speciesType = getSpeciesTypeSafe();
		if (speciesType == null) {
			throw new IllegalStateException("No species type was registered with ID: " + this.speciesTypeId);
		}
		return speciesType;
	}

	/**
	 * Gets the species type this chest accepts, or returns {@code null} if that type was never registered.
	 * The lepidopterist chest ships in base, but its species type belongs to the optional butterfly jar,
	 * and resolving it at construction crashed a base-only client. {@code FMLClientSetupEvent} builds one
	 * block entity per TESR item.
	 *
	 * @return The {@link ISpeciesType} this chest accepts, or {@code null} if no jar registered it
	 */
	@Nullable
	public ISpeciesType<?, ?> getSpeciesTypeSafe() {
		ISpeciesType<?, ?> speciesType = this.speciesType;
		if (speciesType == null) {
			speciesType = IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(this.speciesTypeId);
			this.speciesType = speciesType;
		}
		return speciesType;
	}

	// A chest whose species type never got registered accepts nothing, so it has nothing to show
	@Override
	protected boolean hasGui() {
		return getSpeciesTypeSafe() != null;
	}

}
