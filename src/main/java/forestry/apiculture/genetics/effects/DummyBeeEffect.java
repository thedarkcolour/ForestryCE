package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.genetics.IBeeEffect;

// A bee effect that does nothing. Used in the default "none" as well as for the Leporine bee's Easter effect.
public class DummyBeeEffect implements IBeeEffect {
	private final boolean dominant;

	public DummyBeeEffect(boolean dominant) {
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}
}
