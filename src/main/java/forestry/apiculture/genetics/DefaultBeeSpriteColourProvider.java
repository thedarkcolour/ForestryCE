package forestry.apiculture.genetics;

import forestry.api.apiculture.IBeeSpriteColourProvider;

public class DefaultBeeSpriteColourProvider implements IBeeSpriteColourProvider {
	private final int primaryColour;
	private final int secondaryColour;

	public DefaultBeeSpriteColourProvider(int primaryColour, int secondaryColour) {
		this.primaryColour = primaryColour;
		this.secondaryColour = secondaryColour;
	}

	@Override
	public int getSpriteColour(int renderPass) {
		if (renderPass == 0) {
			return this.primaryColour;
		}
		if (renderPass == 1) {
			return this.secondaryColour;
		}
		return 0xffffff;
	}
}
