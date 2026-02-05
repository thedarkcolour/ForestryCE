package forestry.core.circuits;

public interface IMachineUpgradable {
	void applyMachineUpgrade(double speedChange, double powerChange, double outputChange);

	void removeMachineUpgrade(double speedChange, double powerChange, double outputChange);
}
