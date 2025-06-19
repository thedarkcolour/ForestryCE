/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.multiblock;

import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.ILocationProvider;
import forestry.api.core.TemperatureType;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.errors.FakeErrorLogic;
import forestry.core.owner.FakeOwnerHandler;
import forestry.core.owner.IOwnerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public interface FakeMultiblockController extends IMultiblockControllerInternal, ILocationProvider {
	@Override
	default void attachBlock(IMultiblockComponent part) {
	}

	@Override
	default void detachBlock(IMultiblockComponent part, boolean chunkUnloading) {
	}

	@Override
	default void checkIfMachineIsWhole() {
	}

	@Override
	default void assimilate(IMultiblockControllerInternal other) {
	}

	@Override
	default void _onAssimilated(IMultiblockControllerInternal otherController) {
	}

	@Override
	default void onAssimilated(IMultiblockControllerInternal assimilator) {
	}

	@Override
	default void updateMultiblockEntity() {
	}

	@Override
	default BlockPos getReferenceCoord() {
		return BlockPos.ZERO;
	}

	@Override
	default void recalculateMinMaxCoords() {
	}

	@Override
	default void encodeUpdatePacket(CompoundTag data, HolderLookup.Provider registries) {
	}

	@Override
	default void decodeUpdatePacket(CompoundTag data, HolderLookup.Provider registries) {
	}

	@Override
	default Level getLevel() {
		return null;
	}

	@Override
	default boolean hasNoParts() {
		return true;
	}

	@Override
	default boolean shouldConsume(IMultiblockControllerInternal otherController) {
		return false;
	}

	@Override
	default String getPartsListString() {
		return "";
	}

	@Override
	default void auditParts() {
	}

	@Override
	default Set<IMultiblockComponent> checkForDisconnections() {
		return Collections.emptySet();
	}

	@Override
	default Set<IMultiblockComponent> detachAllBlocks() {
		return Collections.emptySet();
	}

	@Override
	default boolean isAssembled() {
		return false;
	}

	@Override
	default void reassemble() {
	}

	@Override
	default String getLastValidationError() {
		return null;
	}

	@Override
	default Collection<IMultiblockComponent> getComponents() {
		return Collections.emptyList();
	}

	@Override
	default void read(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
	}

	@Override
	default CompoundTag write(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
		return CompoundNBT;
	}

	@Override
	default IOwnerHandler getOwnerHandler() {
		return FakeOwnerHandler.INSTANCE;
	}

	@Override
	default TemperatureType temperature() {
		return TemperatureType.NORMAL;
	}

	@Override
	default HumidityType humidity() {
		return HumidityType.NORMAL;
	}

	@Override
	default IErrorLogic getErrorLogic() {
		return FakeErrorLogic.INSTANCE;
	}

	@Override
	default void writeGuiData(RegistryFriendlyByteBuf buffer) {
	}

	@Override
	default void readGuiData(RegistryFriendlyByteBuf buffer) {
	}
}
