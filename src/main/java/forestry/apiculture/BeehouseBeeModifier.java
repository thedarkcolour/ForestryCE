package forestry.apiculture;

import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;

import javax.annotation.Nullable;

// no mutations/ignoble decay, 300% aging and flowering, 25% production
public class BeehouseBeeModifier implements IBeeModifier {
	@Override
	public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
		return 0.25f * currentSpeed;
	}

	@Override
	public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
		return 0.0f;
	}

	@Override
	public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
		return currentAging / 3f;
	}

	@Override
	public float modifyPollination(IGenome genome, float currentPollination) {
		return 3.0f * currentPollination;
	}

	@Override
	public float modifyGeneticDecay(IGenome genome, float currentDecay) {
		return 0.0f;
	}
}
