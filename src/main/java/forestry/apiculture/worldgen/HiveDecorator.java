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
package forestry.apiculture.worldgen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.core.config.Config;
import forestry.core.config.Defaults;
import forestry.plugins.PluginApiculture;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class HiveDecorator {

	@SuppressWarnings("rawtypes")
	public static final EventType EVENT_TYPE = EnumHelper.addEnum(EventType.class, "FORESTRY_HIVES", new Class[0], new Object[0]);
	private static HiveDecorator instance;

	public static HiveDecorator instance() {
		if (instance == null) {
			instance = new HiveDecorator();
		}
		return instance;
	}

	private HiveDecorator() {
	}

	@SubscribeEvent
	public void generate(PopulateChunkEvent.Post event) {
		if (!TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ, event.hasVillageGenerated, EVENT_TYPE)) {
			return;
		}
		decorateHives(event.world, event.rand, event.chunkX, event.chunkZ);
	}

	private void decorateHives(World world, Random rand, int chunkX, int chunkZ) {
		List<Hive> hives = PluginApiculture.hiveRegistry.getHives();
		Collections.shuffle(hives, rand);

		for (Hive hive : hives) {
			if (Config.generateBeehivesDebug)
				genHiveDebug(world, chunkX, chunkZ, hive);
			else
				genHive(world, rand, chunkX, chunkZ, hive);
		}
	}

	public boolean genHive(World world, Random rand, int chunkX, int chunkZ, Hive hive) {
		if (hive.genChance() < rand.nextFloat() * 128.0f)
			return false;

		int worldX = chunkX * 16;
		int worldZ = chunkZ * 16;

		BiomeGenBase biome = world.getBiomeGenForCoords(worldX, worldZ);
		EnumHumidity humidity = EnumHumidity.getFromValue(biome.rainfall);

		if (!hive.isGoodBiome(biome) || !hive.isGoodHumidity(humidity))
			return false;

		for (int tries = 0; tries < 4; tries ++) {
			int x = worldX + rand.nextInt(16);
			int z = worldZ + rand.nextInt(16);

			if (tryGenHive(world, x, z, hive))
				return true;
		}

		return false;
	}

	private void genHiveDebug(World world, int chunkX, int chunkZ, Hive hive) {
		int worldX = chunkX * 16;
		int worldZ = chunkZ * 16;
		BiomeGenBase biome = world.getBiomeGenForCoords(worldX, worldZ);
		EnumHumidity humidity = EnumHumidity.getFromValue(biome.rainfall);

		if (!hive.isGoodBiome(biome) || !hive.isGoodHumidity(humidity))
			return;

		for (int x = 0; x < 16; x++)
			for (int z = 0; z < 16; z++)
				tryGenHive(world, worldX + x, worldZ + z, hive);
	}

	private boolean tryGenHive(World world, int x, int z, Hive hive) {

		int y = hive.getYForHive(world, x, z);

		if (y < 0)
			return false;

		if (!hive.canReplace(world, x, y, z))
			return false;

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		EnumTemperature temperature = EnumTemperature.getFromValue(biome.getFloatTemperature(x, y, z));
		if (!hive.isGoodTemperature(temperature))
			return false;

		if (!hive.isValidLocation(world, x, y, z))
			return false;

		return setHive(world, x, y, z, hive);
	}

	protected boolean setHive(World world, int x, int y, int z, Hive hive) {
		Block hiveBlock = hive.getHiveBlock();
		boolean placed = world.setBlock(x, y, z, hiveBlock, hive.getHiveMeta(), Defaults.FLAG_BLOCK_SYNCH);
		if (!placed)
			return false;

		Block placedBlock = world.getBlock(x, y, z);
		if (!Block.isEqualTo(hiveBlock, placedBlock))
			return false;

		hiveBlock.onBlockAdded(world, x, y, z);
		world.markBlockForUpdate(x, y, z);

		hive.postGen(world, x, y, z);
		return true;
	}
}
