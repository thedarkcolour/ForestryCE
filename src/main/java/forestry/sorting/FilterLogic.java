package forestry.sorting;

import forestry.api.IForestryApi;
import forestry.api.core.ILocationProvider;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.IAlleleManager;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.genetics.filter.FilterData;
import forestry.api.genetics.filter.IFilterLogic;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.utils.SpeciesUtil;
import forestry.sorting.network.packets.PacketFilterChangeGenome;
import forestry.sorting.network.packets.PacketFilterChangeRule;
import forestry.sorting.network.packets.PacketGuiFilterUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class FilterLogic implements IFilterLogic {
	private final ILocationProvider locatable;
	private final INetworkHandler networkHandler;
	private IFilterRuleType[] filterRules = new IFilterRuleType[6];
	private AlleleFilter[][] genomeFilter = new AlleleFilter[6][3];

	public FilterLogic(ILocationProvider locatable, INetworkHandler networkHandler) {
		this.locatable = locatable;
		this.networkHandler = networkHandler;

		for (int i = 0; i < this.filterRules.length; i++) {
			this.filterRules[i] = IForestryApi.INSTANCE.getFilterManager().getDefaultRule();
		}
	}

	@Override
	public INetworkHandler getNetworkHandler() {
		return this.networkHandler;
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		for (int i = 0; i < this.filterRules.length; i++) {
			data.putString("TypeFilter" + i, this.filterRules[i].getId());
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				AlleleFilter filter = this.genomeFilter[i][j];
				if (filter == null) {
					continue;
				}
				if (filter.activeSpecies != null) {
					data.putString("GenomeFilterS" + i + "-" + j + "-" + 0, filter.activeSpecies.id().toString());
				}
				if (filter.inactiveSpecies != null) {
					data.putString("GenomeFilterS" + i + "-" + j + "-" + 1, filter.inactiveSpecies.id().toString());
				}
			}
		}
		return data;
	}

	@Override
	public void read(CompoundTag data) {
		for (int i = 0; i < this.filterRules.length; i++) {
			this.filterRules[i] = IForestryApi.INSTANCE.getFilterManager().getRuleOrDefault(data.getString("TypeFilter" + i));
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				AlleleFilter filter = new AlleleFilter();
				if (data.contains("GenomeFilterS" + i + "-" + j + "-" + 0)) {
					filter.activeSpecies = SpeciesUtil.getAnySpecies(new ResourceLocation(data.getString("GenomeFilterS" + i + "-" + j + "-" + 0)));
				}
				if (data.contains("GenomeFilterS" + i + "-" + j + "-" + 1)) {
					filter.inactiveSpecies = SpeciesUtil.getAnySpecies(new ResourceLocation(data.getString("GenomeFilterS" + i + "-" + j + "-" + 1)));
				}
				this.genomeFilter[i][j] = filter;
			}
		}
	}

	@Override
	public void writeGuiData(FriendlyByteBuf buffer) {
		writeFilterRules(buffer, this.filterRules);
		writeGenomeFilters(buffer, this.genomeFilter);
	}

	@Override
	public void readGuiData(FriendlyByteBuf buffer) {
        this.filterRules = readFilterRules(buffer);
        this.genomeFilter = readGenomeFilters(buffer);
	}

	public static void writeFilterRules(FriendlyByteBuf buffer, IFilterRuleType[] filterRules) {
		for (IFilterRuleType filterRule : filterRules) {
			buffer.writeShort(IForestryApi.INSTANCE.getFilterManager().getId(filterRule));
		}
	}

	public static void writeGenomeFilters(FriendlyByteBuf buffer, AlleleFilter[][] genomeFilter) {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				AlleleFilter filter = genomeFilter[i][j];
				if (filter == null) {
					buffer.writeBoolean(false);
					buffer.writeBoolean(false);
					continue;
				}
				if (filter.activeSpecies != null) {
					buffer.writeBoolean(true);
					buffer.writeResourceLocation(filter.activeSpecies.id());
				} else {
					buffer.writeBoolean(false);
				}
				if (filter.inactiveSpecies != null) {
					buffer.writeBoolean(true);
					buffer.writeResourceLocation(filter.inactiveSpecies.id());
				} else {
					buffer.writeBoolean(false);
				}
			}
		}
	}

	public static IFilterRuleType[] readFilterRules(FriendlyByteBuf buffer) {
		IFilterRuleType[] filterRules = new IFilterRuleType[6];
		for (int i = 0; i < 6; i++) {
			filterRules[i] = IForestryApi.INSTANCE.getFilterManager().getRule(buffer.readShort());
		}

		return filterRules;
	}

	public static AlleleFilter[][] readGenomeFilters(FriendlyByteBuf buffer) {
		AlleleFilter[][] genomeFilters = new AlleleFilter[6][32023];
		IAlleleManager alleles = IForestryApi.INSTANCE.getAlleleManager();

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				AlleleFilter filter = new AlleleFilter();
				if (buffer.readBoolean()) {
					filter.activeSpecies = SpeciesUtil.getAnySpecies(buffer.readResourceLocation());
				}
				if (buffer.readBoolean()) {
					filter.inactiveSpecies = SpeciesUtil.getAnySpecies(buffer.readResourceLocation());
				}
				genomeFilters[i][j] = filter;
			}
		}

		return genomeFilters;
	}

	@Override
	public boolean isValid(ItemStack stack, Direction facing) {
		return IIndividualHandlerItem.filter(stack, (individual, stage) -> isValid(facing, stack, new FilterData(individual, stage)));
	}

	public boolean isValid(Direction facing, ItemStack stack, FilterData filterData) {
		IFilterRuleType rule = getRule(facing);
		if (rule == DefaultFilterRuleType.CLOSED) {
			return false;
		}
		if (rule == DefaultFilterRuleType.ITEM) {
			return true;
		}
		ResourceLocation requiredRoot = rule.getSpeciesTypeId();
		if (requiredRoot != null && (!filterData.type().id().equals(requiredRoot))) {
			return false;
		}
		if (rule == DefaultFilterRuleType.ANYTHING || rule.isValid(stack, filterData)) {
			IIndividual individual = filterData.individual();
			var active = individual.getSpecies();
			var inactive = individual.getInactiveSpecies();
			return isValidAllelePair(facing, active, inactive);
		}
		return false;
	}

	public boolean isValidAllelePair(Direction orientation, ISpecies<?> active, ISpecies<?> inactive) {
		AlleleFilter[] directionFilters = this.genomeFilter[orientation.ordinal()];

		if (directionFilters == null) {
			return true;
		}

		boolean foundFilter = false;
		for (int i = 0; i < 3; i++) {
			AlleleFilter filter = directionFilters[i];
			if (filter != null && !filter.isEmpty()) {
				foundFilter = true;
				if (!filter.isEmpty() && filter.isValid(active, inactive)) {
					return true;
				}
			}
		}
		return !foundFilter;
	}

	public IFilterRuleType getRule(Direction facing) {
		return this.filterRules[facing.ordinal()];
	}

	public boolean setRule(Direction facing, IFilterRuleType rule) {
		if (this.filterRules[facing.ordinal()] != rule) {
            this.filterRules[facing.ordinal()] = rule;
			return true;
		}
		return false;
	}

	@Nullable
	public AlleleFilter getGenomeFilter(Direction facing, int index) {
		return this.genomeFilter[facing.ordinal()][index];
	}

	@Nullable
	public ISpecies<?> getGenomeFilter(Direction facing, int index, boolean active) {
		AlleleFilter filter = getGenomeFilter(facing, index);
		if (filter == null) {
			return null;
		}
		return active ? filter.activeSpecies : filter.inactiveSpecies;
	}

	public boolean setGenomeFilter(Direction facing, int index, boolean active, @Nullable ISpecies<?> allele) {
		AlleleFilter filter = this.genomeFilter[facing.ordinal()][index];
		if (filter == null) {
			filter = this.genomeFilter[facing.ordinal()][index] = new AlleleFilter();
		}
		boolean set;
		if (active) {
			set = filter.activeSpecies != allele;
			filter.activeSpecies = allele;
		} else {
			set = filter.inactiveSpecies != allele;
			filter.inactiveSpecies = allele;
		}
		return set;
	}

	@Override
	public void sendToServer(Direction facing, int index, boolean active, @Nullable ISpecies<?> allele) {
        IForestryPacketServer packet = new PacketFilterChangeGenome(this.locatable.getBlockPos(), facing, (short) index, active, allele);
        PacketDistributor.sendToServer(packet);
    }

	@Override
	public void sendToServer(Direction facing, IFilterRuleType rule) {
        IForestryPacketServer packet = new PacketFilterChangeRule(this.locatable.getBlockPos(), facing, rule);
        PacketDistributor.sendToServer(packet);
    }

	public PacketGuiFilterUpdate createGuiUpdatePacket(BlockPos pos) {
		return new PacketGuiFilterUpdate(pos, this.filterRules, this.genomeFilter);
	}

	public void readGuiUpdatePacket(PacketGuiFilterUpdate msg) {
		this.filterRules = msg.filterRules();
		this.genomeFilter = msg.genomeFilter();
	}
}
