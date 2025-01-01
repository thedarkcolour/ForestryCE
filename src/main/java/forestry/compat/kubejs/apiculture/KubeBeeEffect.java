package forestry.compat.kubejs.apiculture;

import java.util.function.UnaryOperator;

import com.mojang.datafixers.util.Function3;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;

public record KubeBeeEffect(UnaryOperator<IEffectData> validateStorage, boolean combinable,
							Function3<IGenome, IEffectData, IBeeHousing, IEffectData> doEffect,
							Function3<IGenome, IEffectData, IBeeHousing, IEffectData> doClientEffect,
							boolean dominant) implements IBeeEffect {
	@Override
	public IEffectData validateStorage(IEffectData storedData) {
		return this.validateStorage.apply(storedData);
	}

	@Override
	public boolean isCombinable() {
		return this.combinable;
	}

	@Override
	public IEffectData doEffect(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		return this.doEffect.apply(genome, storedData, housing);
	}

	@Override
	public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		return this.doClientEffect.apply(genome, storedData, housing);
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}
}
