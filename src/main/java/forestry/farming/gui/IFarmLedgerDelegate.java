package forestry.farming.gui;

import forestry.api.climate.IClimateProvider;

public interface IFarmLedgerDelegate extends IClimateProvider {
	float getHydrationModifier();

	float getHydrationTempModifier();

	float getHydrationHumidModifier();

	float getHydrationRainfallModifier();

	double getDrought();
}
