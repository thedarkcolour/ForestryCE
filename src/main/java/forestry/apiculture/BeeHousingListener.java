package forestry.apiculture;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.genetics.pollen.IPollen;

public class BeeHousingListener implements IBeeListener {
	private final IBeeHousing beeHousing;

	public BeeHousingListener(IBeeHousing beeHousing) {
		this.beeHousing = beeHousing;
	}

	@Override
	public void wearOutEquipment(int amount) {
		for (IBeeListener beeListener : this.beeHousing.getBeeListeners()) {
			beeListener.wearOutEquipment(amount);
		}
	}

	@Override
	public void onQueenDeath() {
		for (IBeeListener beeListener : this.beeHousing.getBeeListeners()) {
			beeListener.onQueenDeath();
		}
	}

	@Override
	public boolean onPollenRetrieved(IPollen<?> pollen) {
		for (IBeeListener beeListener : this.beeHousing.getBeeListeners()) {
			if (beeListener.onPollenRetrieved(pollen)) {
				return true;
			}
		}

		return false;
	}

}
