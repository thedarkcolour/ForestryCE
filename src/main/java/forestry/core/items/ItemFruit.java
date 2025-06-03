package forestry.core.items;

import forestry.api.core.IItemSubtype;
import net.minecraft.world.item.Item;

import java.util.Locale;

public class ItemFruit extends ItemForestryFood {

	public enum EnumFruit implements IItemSubtype {
		CHERRY(1, 0.5f, 16),
		WALNUT(1, 0.75f, 16),
		CHESTNUT(1, 1.5f, 16),
		LEMON,
		PLUM,
		DATES(1, 0.75f, 16),
		PAPAYA(4, 0.8f, 32),

		PEAR,
		ORANGE,
		COCONUT(2, 1.6f, 64), //Not sure what the best stat is here. Low hunger, high saturation and use time seems fair???
		OLIVE(1, 1f, 16),
		FEIJOA(2, 0.6f, 16);

		private final String name;

		private final int heal; //the number of half-shanks to heal
		private final float saturation;
		private final int useTime;

		EnumFruit() {
			this.name = name().toLowerCase(Locale.ENGLISH);
			//The default stats is the same as the vanilla apple.
			this.heal = 4;
			this.saturation = 0.3f;
			this.useTime = 32;
		}

		//Constructor for overriding default values
		EnumFruit(int h, float s, int u){
			this.name = name().toLowerCase(Locale.ENGLISH);
			this.heal = h;
			this.saturation = s;
			this.useTime = u;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	private final EnumFruit type;
	public ItemFruit(EnumFruit type) {
		super(type.heal, type.saturation, type.useTime);
		this.type = type;
	}

	public EnumFruit getType() {
		return this.type;
	}


	@Override
	public boolean canBeDepleted() {
		return false;
	}
}
