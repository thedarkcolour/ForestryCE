package forestry.lepidopterology.entities;

import forestry.api.IForestryApi;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.genetics.pollen.IPollenManager;

public class AIButterflyPollinate extends AIButterflyInteract {
	public AIButterflyPollinate(EntityButterfly entity) {
		super(entity);
	}

	@Override
	protected boolean canInteract() {
		return this.rest != null && IForestryApi.INSTANCE.getPollenManager().canPollinate(this.entity.level(), this.rest, this.entity.getButterfly());
	}

	@Override
	public void tick() {
		if (canContinueToUse() && this.rest != null) {
			IPollenManager pollens = IForestryApi.INSTANCE.getPollenManager();
			IPollen<?> butterflyPollen = this.entity.getPollen();

			if (butterflyPollen == null) {
                this.entity.setPollen(pollens.getPollen(this.entity.level(), this.rest, this.entity.getButterfly()));
                this.entity.changeExhaustion(-this.entity.getExhaustion());
			} else if (butterflyPollen.tryPollinate(this.entity.level(), this.rest, this.entity.getPollen())) {
                this.entity.setPollen(null);
			}
			setHasInteracted();
            this.entity.cooldownPollination = EntityButterfly.COOLDOWNS;
		}
	}

}
