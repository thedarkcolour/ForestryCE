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
package forestry.arboriculture.tiles;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import forestry.api.IForestryApi;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.client.IForestryClientApi;
import forestry.api.climate.IBiomeProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.ISpectacleBlock;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IFruitBearer;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.arboriculture.features.ArboricultureTiles;
import forestry.arboriculture.network.IRipeningPacketReceiver;
import forestry.arboriculture.network.PacketRipeningUpdate;
import forestry.core.ClientsideCode;
import forestry.core.network.packets.PacketTileStream;
import forestry.core.utils.ColourUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;

public class TileLeaves extends TileTreeContainer implements IFruitBearer, IButterflyNursery, IRipeningPacketReceiver, IBiomeProvider, ISpectacleBlock {
	private static final String NBT_RIPENING = "RT";
	private static final String NBT_DAMAGE = "ENC";
	private static final String NBT_FRUIT_LEAF = "FL";
	private static final String NBT_MATURATION = "CATMAT";
	private static final String NBT_CATERPILLAR = "CATER";

	public static final ModelProperty<ITreeSpecies> PROPERTY_SPECIES = new ModelProperty<>();
	public static final ModelProperty<Boolean> PROPERTY_POLLINATED = new ModelProperty<>();
	public static final ModelProperty<ResourceLocation> PROPERTY_FRUIT_TEXTURE = new ModelProperty<>();

	private int colourFruits;

	@Nullable
	private ResourceLocation fruitSprite;
	@Nullable
	private ITreeSpecies species;
	@Nullable
	private IButterfly caterpillar;

	private boolean isFruitLeaf;
	private boolean isPollinatedState;
	private int ripeningTime;
	private short ripeningPeriod = Short.MAX_VALUE - 1;

	private int maturationTime;
	private int damage;

	private IEffectData[] effectData = new IEffectData[2];

	public TileLeaves(BlockPos pos, BlockState state) {
		super(ArboricultureTiles.LEAVES.tileType(), pos, state);
	}

	/* SAVING & LOADING */
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);

		this.ripeningTime = nbt.getInt(NBT_RIPENING);
		this.damage = nbt.getInt(NBT_DAMAGE);
		this.isFruitLeaf = nbt.getBoolean(NBT_FRUIT_LEAF);
		boolean checkFruit = !nbt.contains(NBT_FRUIT_LEAF);

		Tag caterpillarNbt = nbt.get(NBT_CATERPILLAR);
		if (caterpillarNbt != null) {
			this.maturationTime = nbt.getInt(NBT_MATURATION);
			this.caterpillar = SpeciesUtil.deserializeIndividual(SpeciesUtil.BUTTERFLY_TYPE.get(), caterpillarNbt);
		}

		ITree tree = getTree();
		if (tree != null) {
			setTree(tree);

			if (checkFruit) {
				setFruit(tree, false);
			} else if (this.isFruitLeaf) {
				setFruit(tree, true);
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);

		nbt.putInt(NBT_RIPENING, this.ripeningTime);
		nbt.putInt(NBT_DAMAGE, damage);
		nbt.putBoolean(NBT_FRUIT_LEAF, isFruitLeaf);

		if (caterpillar != null) {
			nbt.putInt(NBT_MATURATION, maturationTime);

			Tag caterpillarNbt = SpeciesUtil.serializeIndividual(caterpillar);
			if (caterpillarNbt != null) {
				nbt.put(NBT_CATERPILLAR, caterpillarNbt);
			}
		}
	}

	@Override
	public void onBlockTick(Level worldIn, BlockPos pos, BlockState state, RandomSource rand) {
		ITree tree = getTree();
		if (tree == null) {
			return;
		}

		IGenome genome = tree.getGenome();
		ITreeSpecies primary = genome.getActiveValue(TreeChromosomes.SPECIES);

		boolean isDestroyed = isDestroyed(tree, damage);
		for (ILeafTickHandler tickHandler : primary.getType().getLeafTickHandlers()) {
			if (tickHandler.onRandomLeafTick(tree, level, rand, getBlockPos(), isDestroyed)) {
				return;
			}
		}

		if (isDestroyed) {
			return;
		}

		if (damage > 0) {
			damage--;
		}

		if (hasFruit() && getRipeningTime() < ripeningPeriod) {
			//ITreekeepingMode treekeepingMode = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(level);
			//float sappinessModifier = treekeepingMode.getSappinessModifier(genome, 1f);
			float sappiness = genome.getActiveValue(TreeChromosomes.SAPPINESS);// * sappinessModifier;

			if (rand.nextFloat() < sappiness) {
				ripeningTime++;
				sendNetworkUpdateRipening();
			}
		}

		if (caterpillar != null) {
			matureCaterpillar();
		}

		effectData = tree.doEffect(effectData, level, getBlockPos());
	}

	@Override
	public void setTree(ITree tree) {
		ITree oldTree = getTree();
		super.setTree(tree);

		IGenome genome = tree.getGenome();
		this.species = genome.getActiveValue(TreeChromosomes.SPECIES);

		// update fruit state if genome changed
		if (oldTree != null && tree.getSpecies() != oldTree.getSpecies() || (this.level != null && this.level.isClientSide)) {
			setFruit(tree, false);
		}

		requestModelDataUpdate();
		setChanged();
	}

	// alwaysFruit: if true, random check for fruit chance will always succeed
	public void setFruit(ITree tree, boolean alwaysFruit) {
		IGenome genome = tree.getGenome();

		if (tree.hasFruitLeaves() && level != null && !level.isClientSide) {
			IFruit fruitProvider = genome.getActiveValue(TreeChromosomes.FRUIT);
			if (fruitProvider.isFruitLeaf()) {
				this.isFruitLeaf = alwaysFruit || fruitProvider.getFruitChance(genome, level) >= level.random.nextFloat();
			}
		}

		if (this.isFruitLeaf) {
			IFruit fruit = genome.getActiveValue(TreeChromosomes.FRUIT);
			if (this.level != null && this.level.isClientSide) {
				this.fruitSprite = fruit.getSprite(genome, this.level, getBlockPos(), getRipeningTime());
			}

			this.ripeningPeriod = (short) fruit.getRipeningPeriod();
		} else if (this.level != null && this.level.isClientSide) {
			this.fruitSprite = null;
		}
	}

	/* INFORMATION */
	private static boolean isDestroyed(@Nullable ITree tree, int damage) {
		return tree != null && damage > tree.getResilience();
	}

	public boolean isPollinated() {
		ITree tree = getTree();
		return tree != null && !isDestroyed(tree, damage) && tree.getMate() != null;
	}

	@OnlyIn(Dist.CLIENT)
	public int getFoliageColour() {
		final int baseColor = IForestryClientApi.INSTANCE.getTreeManager().getTint(this.species).get(this.level, this.worldPosition);

		ITree tree = getTree();
		if (isDestroyed(tree, damage)) {
			return ColourUtil.addRGBComponents(baseColor, 92, 61, 0);
		} else if (caterpillar != null) {
			return ColourUtil.multiplyRGBComponents(baseColor, 1.5f);
		} else {
			return baseColor;
		}
	}

	public int getFruitColour() {
		if (colourFruits == 0 && hasFruit()) {
			colourFruits = determineFruitColour();
		}
		return colourFruits;
	}

	private int determineFruitColour() {
		ITree tree = getTree();
		if (tree == null) {
			tree = SpeciesUtil.getTreeSpecies(ForestryTreeSpecies.BUSH_CHERRY).createIndividual();
		}
		IGenome genome = tree.getGenome();
		IFruit fruit = genome.getActiveValue(TreeChromosomes.FRUIT);
		return fruit.getColour(genome, level, getBlockPos(), getRipeningTime());
	}

	@Override
	public ModelData getModelData() {
		ModelData.Builder builder = ModelData.builder();
		builder.with(PROPERTY_SPECIES, this.species);
		builder.with(PROPERTY_POLLINATED, this.isPollinatedState);
		builder.with(PROPERTY_FRUIT_TEXTURE, this.fruitSprite);
		return builder.build();
	}

	public int getRipeningTime() {
		return ripeningTime;
	}

	public void setMate(ITree pollen) {
		getTree().setMate(pollen.getGenome());
		if (!level.isClientSide) {
			sendNetworkUpdate();
		}
	}

	/* NETWORK */
	public void sendNetworkUpdate() {
		NetworkUtil.sendNetworkPacket(new PacketTileStream(this), worldPosition, level);
	}

	private void sendNetworkUpdateRipening() {
		if (isRemoved()) {
			return;
		}
		int newColourFruits = determineFruitColour();
		if (newColourFruits == colourFruits) {
			return;
		}
		colourFruits = newColourFruits;

		PacketRipeningUpdate ripeningUpdate = new PacketRipeningUpdate(this);
		NetworkUtil.sendNetworkPacket(ripeningUpdate, worldPosition, level);
		setChanged();
	}

	private static final short hasFruitFlag = 1;
	private static final short isPollinatedFlag = 1 << 1;

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);

		byte leafState = 0;
		boolean hasFruit = hasFruit();

		if (isPollinated()) {
			leafState |= isPollinatedFlag;
		}

		if (hasFruit) {
			leafState |= hasFruitFlag;
		}

		data.writeByte(leafState);

		if (hasFruit) {
			String fruitAlleleUID = getTree().getGenome().getActiveAllele(TreeChromosomes.FRUIT).alleleId().toString();
			int colourFruits = getFruitColour();

			data.writeUtf(fruitAlleleUID);
			data.writeInt(colourFruits);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		ResourceLocation speciesId = null;
		if (data.readBoolean()) {
			speciesId = data.readResourceLocation(); // this is called instead of super.readData, be careful!
		}
		byte leafState = data.readByte();
		isFruitLeaf = (leafState & hasFruitFlag) > 0;
		isPollinatedState = (leafState & isPollinatedFlag) > 0;
		ResourceLocation fruitId = null;

		if (isFruitLeaf) {
			fruitId = data.readResourceLocation();
			colourFruits = data.readInt();
		}

		ITreeSpecies treeTemplate = SpeciesUtil.TREE_TYPE.get().getSpeciesSafe(speciesId);
		if (treeTemplate != null) {
			ITree tree;
			if (fruitId != null) {
				tree = treeTemplate.createIndividual(Map.of(TreeChromosomes.FRUIT, ForestryAlleles.REGISTRY.getAllele(fruitId)));
			} else {
				tree = treeTemplate.createIndividual();
			}
			if (isPollinatedState) {
				tree.setMate(tree.getGenome());
			}

			setTree(tree);

			ClientsideCode.markForUpdate(this.worldPosition);
		}
	}

	@Override
	public void fromRipeningPacket(int newColourFruits) {
		if (newColourFruits == colourFruits) {
			return;
		}
		colourFruits = newColourFruits;
		ClientsideCode.markForUpdate(this.worldPosition);
	}

	/* IFRUITBEARER */
	@Override
	public List<ItemStack> pickFruit(ItemStack tool) {
		ITree tree = getTree();
		if (tree == null || !hasFruit()) {
			return List.of();
		}

		List<ItemStack> produceStacks = tree.produceStacks(this.level, this.worldPosition, getRipeningTime());
		ripeningTime = 0;
		sendNetworkUpdateRipening();
		return produceStacks;
	}

	@Override
	public float getRipeness() {
		if (ripeningPeriod == 0) {
			return 1.0f;
		}
		if (getTree() == null) {
			return 0f;
		}
		return this.ripeningTime / (float) this.ripeningPeriod;
	}

	@Override
	public boolean hasFruit() {
		return this.isFruitLeaf && !isDestroyed(getTree(), damage);
	}

	@Override
	public void addRipeness(float add) {
		if (getTree() == null || !this.isFruitLeaf || getRipeningTime() >= this.ripeningPeriod) {
			return;
		}
		this.ripeningTime += this.ripeningPeriod * add;
		sendNetworkUpdateRipening();
	}

	/* IBUTTERFLYNURSERY */

	private void matureCaterpillar() {
		if (caterpillar == null) {
			return;
		}
		maturationTime++;

		ITree tree = getTree();
		boolean wasDestroyed = isDestroyed(tree, damage);
		damage += caterpillar.getGenome().getActiveValue(ButterflyChromosomes.METABOLISM);

		IGenome caterpillarGenome = caterpillar.getGenome();
		int caterpillarMatureTime = Math.round((float) caterpillarGenome.getActiveValue(ButterflyChromosomes.LIFESPAN) / (caterpillarGenome.getActiveValue(ButterflyChromosomes.FERTILITY) * 2));

		if (maturationTime >= caterpillarMatureTime) {
			SpeciesUtil.BUTTERFLY_TYPE.get().plantCocoon(level, worldPosition, caterpillar, 0, false);
			setCaterpillar(null);
		} else if (!wasDestroyed && isDestroyed(tree, damage)) {
			sendNetworkUpdate();
		}
	}

	@Override
	public BlockPos getCoordinates() {
		return getBlockPos();
	}

	@Override
	@Nullable
	public IButterfly getCaterpillar() {
		return caterpillar;
	}

	@Override
	public IIndividual getNanny() {
		return getTree();
	}

	@Override
	public void setCaterpillar(@Nullable IButterfly caterpillar) {
		maturationTime = 0;
		this.caterpillar = caterpillar;
		sendNetworkUpdate();
	}

	@Override
	public boolean canNurse(IButterfly caterpillar) {
		ITree tree = getTree();
		return !isDestroyed(tree, damage) && this.caterpillar == null;
	}

	@Override
	public Holder<Biome> getBiome() {
		return this.level.getBiome(worldPosition);
	}

	@Override
	public TemperatureType temperature() {
		return IForestryApi.INSTANCE.getClimateManager().getTemperature(getBiome());
	}

	@Override
	public HumidityType humidity() {
		return IForestryApi.INSTANCE.getClimateManager().getHumidity(getBiome());
	}

	@Override
	public Level getWorldObj() {
		return level;
	}

	@Override
	public boolean isHighlighted(Player player) {
		return isPollinated();
	}
}
