package forestry.core.circuits;

import forestry.api.core.IItemSubtype;
import forestry.core.render.ColourProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public enum EnumCircuitBoardType implements IItemSubtype {
	BASIC(1),
	ENHANCED(2),
	REFINED(3),
	INTRICATE(4);

	private final int sockets;
	private final String name;

	EnumCircuitBoardType(int sockets) {
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.sockets = sockets;
	}

	public int getSockets() {
		return this.sockets;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@OnlyIn(Dist.CLIENT)
	public int getPrimaryColor() {
		return ColourProperties.INSTANCE.get("item.circuit." + name().toLowerCase(Locale.ENGLISH) + ".primary");
	}

	@OnlyIn(Dist.CLIENT)
	public int getSecondaryColor() {
		return ColourProperties.INSTANCE.get("item.circuit." + name().toLowerCase(Locale.ENGLISH) + ".secondary");
	}
}
