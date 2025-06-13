package forestry.apiculture.tiles;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.apiculture.ApiaryBeeListener;
import forestry.apiculture.ApiaryBeeModifier;
import forestry.apiculture.IApiary;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.gui.BeeHousingMenu;
import forestry.apiculture.gui.GuiBeeHousing;
import forestry.apiculture.inventory.IApiaryInventory;
import forestry.apiculture.inventory.InventoryApiary;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class TileApiary extends TileBeeHousingBase implements IApiary {
	private final IBeeModifier beeModifier = new ApiaryBeeModifier();
	private final IBeeListener beeListener = new ApiaryBeeListener(this);
	private final InventoryApiary inventory = new InventoryApiary();

	public TileApiary(BlockPos pos, BlockState state) {
		super(ApicultureTiles.APIARY.tileType(), pos, state, "apiary");
		setInternalInventory(this.inventory);
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return this.inventory;
	}

	@Override
	public IApiaryInventory getApiaryInventory() {
		return this.inventory;
	}

	@Override
	public Collection<IBeeModifier> getBeeModifiers() {
		ArrayList<IBeeModifier> beeModifiers = new ArrayList<>();

		beeModifiers.add(this.beeModifier);
		this.inventory.forEachFrame((hiveFrame, stack) -> beeModifiers.add(hiveFrame.getBeeModifier(stack)));

		return beeModifiers;
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return Collections.singleton(this.beeListener);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BeeHousingMenu(windowId, player.getInventory(), this, true, GuiBeeHousing.Icon.APIARY);
	}

	@Override
	public boolean interactNoItem(Level level, Player player, BlockPos pos) {
		player.openMenu(this, buffer -> {
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(true);
			NetworkUtil.writeEnum(buffer, GuiBeeHousing.Icon.APIARY);
		});
		return false;
	}
}
