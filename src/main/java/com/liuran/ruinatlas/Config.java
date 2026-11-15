package com.liuran.ruinatlas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * 本地配置
 * <p>NeoForge 版用 ModConfigSpec 由 FML 生成配置文件；Fabric 没有配置 API，
 * 这里用一份 properties 文件保留同样的键名、默认值与校验逻辑。
 */
public class Config {
	private static final String FILE_NAME = "ruinatlas-common.properties";

	public static boolean LOG_DIRT_BLOCK = true;
	public static int MAGIC_NUMBER = 42;
	public static String MAGIC_NUMBER_INTRODUCTION = "The magic number is... ";
	public static List<String> ITEM_STRINGS = new ArrayList<>(List.of("minecraft:iron_ingot"));

	private Config() {
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Properties properties = new Properties();
		properties.setProperty("logDirtBlock", Boolean.toString(LOG_DIRT_BLOCK));
		properties.setProperty("magicNumber", Integer.toString(MAGIC_NUMBER));
		properties.setProperty("magicNumberIntroduction", MAGIC_NUMBER_INTRODUCTION);
		properties.setProperty("items", String.join(",", ITEM_STRINGS));

		if (Files.exists(path)) {
			try (InputStream input = Files.newInputStream(path)) {
				properties.load(input);
			} catch (IOException exception) {
				RuinAtlas.LOGGER.warn("读取配置失败：{}", path, exception);
			}
		} else {
			try (OutputStream output = Files.newOutputStream(path)) {
				properties.store(output, "墟舆录 Ruin Atlas 通用配置");
			} catch (IOException exception) {
				RuinAtlas.LOGGER.warn("写入配置失败：{}", path, exception);
			}
		}

		LOG_DIRT_BLOCK = Boolean.parseBoolean(properties.getProperty("logDirtBlock", "true"));
		MAGIC_NUMBER = Integer.parseInt(properties.getProperty("magicNumber", "42"));
		MAGIC_NUMBER_INTRODUCTION = properties.getProperty("magicNumberIntroduction", "The magic number is... ");

		List<String> items = new ArrayList<>();

		for (String item : properties.getProperty("items", "minecraft:iron_ingot").split(",")) {
			if (validateItemName(item)) {
				items.add(item.trim());
			}
		}

		ITEM_STRINGS = List.copyOf(items);
	}

	private static boolean validateItemName(final Object obj) {
		return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName.trim()));
	}
}
