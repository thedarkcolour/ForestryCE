package forestry.lepidopterology.entities;

import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.TreeUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AIButterflyMate extends AIButterflyInteract {
	@Nullable
	private EntityButterfly targetMate;

	public AIButterflyMate(EntityButterfly entity) {
		super(entity);
		// flags: MOVE, JUMP (from interact)
	}

	@Override
	protected boolean canInteract() {
		if (this.entity.getButterfly().getMate() == null && this.entity.canMate()) {
			return true;
		}
		if (this.entity.cooldownEgg > 0) {
			return false;
		}

		if (this.entity.getButterfly().getMate() == null) {
			return false;
		}

		if (EntityButterfly.isMaxButterflyCluster(this.entity.position(), this.entity.level())) {
			return false;
		}

		return this.rest != null && GeneticsUtil.canNurse(this.entity.getButterfly(), this.entity.level(), this.rest);
	}

	@Override
	public void tick() {
		if (canContinueToUse()) {
			if (this.entity.getButterfly().getMate() == null && this.targetMate != null) {
				if (this.entity.cooldownMate <= 0 && this.entity.distanceTo(this.targetMate) < 9.0D) {
                    this.entity.getButterfly().setMate(this.targetMate.getButterfly().getGenome());
                    this.targetMate.getButterfly().setMate(this.entity.getButterfly().getGenome());
                    this.entity.cooldownMate = EntityButterfly.COOLDOWNS;
				}
			} else if (this.rest != null) {
				IButterflyNursery nursery = TreeUtil.getOrCreateNursery(this.entity.level(), this.rest, false);
				if (nursery != null) {
					if (nursery.canNurse(this.entity.getButterfly())) {
						nursery.setCaterpillar(this.entity.getButterfly().spawnCaterpillar(nursery));
						//Log.finest("A butterfly '%s' laid an egg at %s/%s/%s.", entity.getButterfly().getIdent(), rest.posX, rest.posY, rest.posZ);
						if (this.entity.getRandom().nextFloat() < 1.0f / this.entity.getButterfly().getGenome().getActiveValue(ButterflyChromosomes.FERTILITY)) {
                            this.entity.setHealth(0);
						}
					}
				}
				setHasInteracted();
                this.entity.cooldownEgg = EntityButterfly.COOLDOWNS;
			}
		}
	}

	@Override
	public boolean canUse() {
		if (!super.canUse()) {
			return false;
		}
		if (this.entity.getButterfly().getMate() == null) {
			if (!this.entity.canMate()) {
				return false;
			} else {
                this.targetMate = getNearbyMate();
				return this.targetMate != null;
			}
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (!super.canContinueToUse()) {
			return false;
		}
		if (this.entity.getButterfly().getMate() == null) {
			return this.targetMate != null && this.targetMate.isAlive() && this.targetMate.canMate();
		}
		return true;
	}

	@Override
	public void stop() {
		super.stop();

        this.targetMate = null;
	}

	@Nullable
	private EntityButterfly getNearbyMate() {
		float f = 8.0F;
		List<EntityButterfly> nextButterflys = this.entity.level().getEntitiesOfClass(EntityButterfly.class, this.entity.getBoundingBox().expandTowards(f, f, f));
		double d0 = Double.MAX_VALUE;
		EntityButterfly nextButterfly = null;

		for (EntityButterfly butterfly : nextButterflys) {
			if (this.entity.canMateWith(butterfly) && this.entity.distanceTo(butterfly) < d0) {
				nextButterfly = butterfly;
				d0 = this.entity.distanceTo(butterfly);
			}
		}

		return nextButterfly;
	}
}
