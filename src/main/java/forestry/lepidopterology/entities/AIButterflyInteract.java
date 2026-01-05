package forestry.lepidopterology.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class AIButterflyInteract extends AIButterflyBase {
	@Nullable
	protected BlockPos rest;

	private boolean canInteract = false;
	private boolean hasInteracted = false;

	protected AIButterflyInteract(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.entity.getState() != EnumButterflyState.RESTING) {
			return false;
		}
		Vec3 pos = this.entity.position();
        this.rest = new BlockPos((int) pos.x, (int) Math.floor(pos.y) - 1, (int) pos.z);
		if (this.entity.level().isEmptyBlock(this.rest)) {
			return false;
		}

        this.canInteract = canInteract();

		return this.canInteract;
	}

	protected abstract boolean canInteract();

	@Override
	public boolean canContinueToUse() {
		return this.canInteract && !this.hasInteracted;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
        this.canInteract = false;
        this.hasInteracted = false;
        this.rest = null;
	}

	protected void setHasInteracted() {
        this.hasInteracted = true;
	}

}
