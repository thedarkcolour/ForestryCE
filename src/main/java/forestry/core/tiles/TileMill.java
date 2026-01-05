package forestry.core.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileMill extends TileBase {
	public float speed;
	public int stage = 0;
	public int charge = 0;
	public float progress;

	protected TileMill(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
        this.speed = 0.01F;
	}

	@Override
	public void clientTick(Level level, BlockPos pos, BlockState state) {
		update(level, pos, false);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		update(level, pos, true);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		data.writeInt(this.charge);
		data.writeFloat(this.speed);
		data.writeInt(this.stage);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.charge = data.readInt();
        this.speed = data.readFloat();
        this.stage = data.readInt();
	}

	private void update(Level level, BlockPos pos, boolean isSimulating) {
		// Stop gracefully if discharged.
		if (this.charge <= 0) {
			if (this.stage > 0) {
                this.progress += this.speed;
			}
			if (this.progress > 0.5) {
                this.stage = 2;
			}
			if (this.progress > 1) {
                this.progress = 0;
                this.stage = 0;
			}
			return;
		}

		// Update blades
        this.progress += this.speed;
		if (this.stage <= 0) {
            this.stage = 1;
		}

		if (this.progress > 0.5 && this.stage == 1) {
            this.stage = 2;
			if (this.charge < 7 && isSimulating) {
                this.charge++;
				sendNetworkUpdate();
			}
		}
		if (this.progress > 1) {
            this.progress = 0;
            this.stage = 0;

			// Fully charged! Do something!
			if (this.charge >= 7) {
				activate(level, pos);
			}
		}

	}

	protected abstract void activate(Level level, BlockPos pos);
}
