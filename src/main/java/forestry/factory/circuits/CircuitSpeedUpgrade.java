package forestry.factory.circuits;

import forestry.core.circuits.Circuit;
import forestry.core.circuits.ISpeedUpgradable;

public class CircuitSpeedUpgrade extends Circuit {
	private final double speedBoost;
	private final float powerDraw;

	public CircuitSpeedUpgrade(String id, double speedBoost, float powerDraw) {
		super(id);
		this.speedBoost = speedBoost;
		this.powerDraw = powerDraw;
	}

	@Override
	public boolean isCircuitable(Object tile) {
		return tile instanceof ISpeedUpgradable;
	}

	@Override
	public void onInsertion(int slot, Object tile) {
		if (!isCircuitable(tile)) {
			return;
		}
		if (tile instanceof ISpeedUpgradable machine) {
			machine.applySpeedUpgrade(this.speedBoost, this.powerDraw);
		}
	}

	@Override
	public void onLoad(int slot, Object tile) {
		onInsertion(slot, tile);
	}

	@Override
	public void onRemoval(int slot, Object tile) {
		if (!isCircuitable(tile)) {
			return;
		}
		if (tile instanceof ISpeedUpgradable machine) {
			machine.applySpeedUpgrade(-this.speedBoost, -this.powerDraw);
		}
	}

	@Override
	public void onTick(int slot, Object tile) {
	}
}
