package forestry.core.tiles;

import forestry.api.IForestryApi;
import forestry.api.client.ForestrySprites;
import forestry.api.core.INbtWritable;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.core.network.IStreamable;
import forestry.core.utils.ColourUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class EscritoireGameToken implements INbtWritable, IStreamable {
	private enum State {
		UNREVEALED,// face down
		PROBED,    // shown by escritoire probe action
		SELECTED,  // selected by the user as the first half of a match
		MATCHED,   // successfully matched
		FAILED;    // failed to match
		public static final State[] VALUES = values();
	}

	@Nullable
	private ISpeciesType<? extends ISpecies<?>, ?> tokenType;
	@Nullable
	private IIndividual tokenIndividual;
	private ItemStack tokenStack = ItemStack.EMPTY;

	private State state = State.UNREVEALED;

	public EscritoireGameToken(FriendlyByteBuf data) {
		readData(data);
	}

	public EscritoireGameToken(ISpecies<?> species) {
		setTokenSpecies(species);
	}

	public EscritoireGameToken(CompoundTag nbt) {
		read(nbt);
	}

	private void setTokenSpecies(ResourceLocation typeId, ResourceLocation speciesId) {
		if (this.tokenType != null && typeId == this.tokenType.id()) {
			setTokenSpecies(this.tokenType.getSpecies(speciesId));
		} else {
			ISpeciesType<?, ?> type = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(typeId);
			setTokenSpecies(type.getSpecies(speciesId));
		}
	}

	private void setTokenSpecies(ISpecies<?> species) {
		this.tokenIndividual = species.createIndividual();
		this.tokenType = species.getType();
		this.tokenStack = species.createStack(species.getType().getDefaultStage());
	}

	public ItemStack getTokenStack() {
		return this.tokenStack;
	}

	public boolean isVisible() {
		return this.state != State.UNREVEALED;
	}

	public boolean isProbed() {
		return this.state == State.PROBED;
	}

	public boolean isMatched() {
		return this.state == State.MATCHED;
	}

	public boolean isSelected() {
		return this.state == State.SELECTED;
	}

	public void setFailed() {
        this.state = State.FAILED;
	}

	public void setProbed(boolean probed) {
		if (probed) {
            this.state = State.PROBED;
		} else {
            this.state = State.UNREVEALED;
		}
	}

	public void setSelected() {
        this.state = State.SELECTED;
	}

	public void setMatched() {
        this.state = State.MATCHED;
	}

	public int getTokenColour() {
		if (this.tokenIndividual == null || !isVisible()) {
			return 0xffffff;
		}

		int iconColor = this.tokenIndividual.getSpecies().getEscritoireColor();

		if (this.state == State.MATCHED) {
			return ColourUtil.multiplyRGBComponents(iconColor, 0.7f);
		} else {
			return iconColor;
		}
	}


	public Component getTooltip() {
		return !this.tokenStack.isEmpty() ? this.tokenStack.getHoverName() : Component.translatable("for.gui.unknown");
	}

	@Nullable
	public ResourceLocation getOverlayToken() {
		return switch (this.state) {
			case FAILED -> ForestrySprites.ERROR_ERRORED;
			case SELECTED -> ForestrySprites.ERROR_UNKNOWN;
			default -> null;
		};
	}

	public boolean matches(EscritoireGameToken other) {
		return ItemStack.matches(this.tokenStack, other.getTokenStack());
	}

	@Override
	public CompoundTag write(CompoundTag nbt) {
		nbt.putInt("state", this.state.ordinal());

		if (this.tokenIndividual != null) {
			nbt.putString("tokenSpecies", this.tokenIndividual.getSpecies().id().toString());
		}
		return nbt;
	}

	private void read(CompoundTag nbt) {
		if (nbt.contains("state")) {
			int stateOrdinal = nbt.getInt("state");
			this.state = State.VALUES[stateOrdinal];
		}

		String tokenSpecies = nbt.getString("tokenSpecies");
		String tokenType = nbt.getString("tokenSpeciesType");

		if (!tokenSpecies.isEmpty() && !tokenType.isEmpty()) {
			setTokenSpecies(new ResourceLocation(tokenType), new ResourceLocation(tokenSpecies));
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		NetworkUtil.writeEnum(data, this.state);
		if (this.tokenIndividual != null && this.tokenType != null) {
			data.writeBoolean(true);
			data.writeResourceLocation(this.tokenIndividual.getSpecies().id());
			data.writeResourceLocation(this.tokenType.id());
		} else {
			data.writeBoolean(false);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
        this.state = NetworkUtil.readEnum(data, State.VALUES);
		if (data.readBoolean()) {
			ResourceLocation speciesId = data.readResourceLocation();
			ResourceLocation typeId = data.readResourceLocation();
			setTokenSpecies(typeId, speciesId);
		}
	}
}
