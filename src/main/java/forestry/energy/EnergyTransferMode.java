package forestry.energy;

public enum EnergyTransferMode {
	EXTRACT, RECEIVE, BOTH, NONE;

	public boolean canExtract() {
		return this == EXTRACT || this == BOTH;
	}

	public boolean canReceive() {
		return this == RECEIVE || this == BOTH;
	}
}
