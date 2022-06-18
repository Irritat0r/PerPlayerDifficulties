package com.jishunamatata.perplayerdifficulty;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemBuilder {

	private static Encoder encoder = Base64.getEncoder().withoutPadding();

	private ItemStack item;
	private ItemMeta meta;

	private ItemBuilder() {
	}

	public ItemBuilder(Material material) {
		this(material, 1);
	}

	public ItemBuilder(Material material, int amount) {
		this.item = new ItemStack(material, amount);
		this.meta = this.item.getItemMeta();
	}

	public static ItemBuilder modifyItem(ItemStack item) {
		ItemBuilder builder = new ItemBuilder();
		builder.item = item;
		builder.meta = item.getItemMeta();

		return builder;
	}

	public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
		this.meta.addEnchant(enchantment, level, true);

		return this;
	}

	public ItemBuilder withName(String name) {
		this.meta.setDisplayName(name);

		return this;
	}
	
	public ItemBuilder addLore(List<String> lore) {
		List<String> itemLore = ItemBuilder.getLore(meta);
		itemLore.addAll(lore);

		meta.setLore(itemLore);

		return this;
	}

	public ItemBuilder addLore(String... lore) {
		List<String> itemLore = ItemBuilder.getLore(meta);
		for (String loreLine : lore)
			itemLore.add(loreLine);

		meta.setLore(itemLore);

		return this;
	}

	public ItemBuilder withLore(List<String> lore) {
		this.meta.setLore(lore);
		return this;
	}

	public ItemBuilder withFlags(ItemFlag... flags) {
		this.meta.addItemFlags(flags);

		return this;
	}

	public ItemBuilder withPersistantData(NamespacedKey key, Object value) {
		PersistentDataContainer data = this.meta.getPersistentDataContainer();

		if (value instanceof Integer) {
			data.set(key, PersistentDataType.INTEGER, (int) value);
		} else if (value instanceof String) {
			data.set(key, PersistentDataType.STRING, (String) value);
		} else if (value instanceof Short) {
			data.set(key, PersistentDataType.SHORT, (short) value);
		} else if (value instanceof Double) {
			data.set(key, PersistentDataType.DOUBLE, (double) value);
		} else if (value instanceof Byte) {
			data.set(key, PersistentDataType.BYTE, (byte) value);
		} else if (value instanceof Float) {
			data.set(key, PersistentDataType.FLOAT, (float) value);
		} else if (value instanceof Long) {
			data.set(key, PersistentDataType.LONG, (long) value);
		}

		return this;
	}

	public ItemBuilder withModelData(int index) {
		this.meta.setCustomModelData(index);

		return this;
	}

	public ItemBuilder withOwner(OfflinePlayer player) {
		if (!(meta instanceof SkullMeta))
			throw new UnsupportedOperationException(meta.getClass().getTypeName() + " is not of type SkullMeta");

		((SkullMeta) meta).setOwningPlayer(player);

		return this;
	}

	public ItemBuilder withTexture(String value) {
		return withTexture(UUID.nameUUIDFromBytes(value.getBytes()), value);
	}

	public ItemBuilder withTexture(String id, String value) {
		return withTexture(UUID.fromString(id), value);
	}

	public ItemBuilder withTexture(UUID id, String value) {

		if (!(meta instanceof SkullMeta))
			throw new UnsupportedOperationException(meta.getClass().getTypeName() + " is not of type SkullMeta");

		StringBuilder builder = new StringBuilder("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/");
		builder.append(value);
		builder.append("\"}}}");

		GameProfile profile = new GameProfile(id, null);
		profile.getProperties().put("textures", new Property("textures", encoder.encodeToString(builder.toString().getBytes())));

		try {
			Method method = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
			method.setAccessible(true);

			method.invoke(meta, profile);
		} catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}

		return this;
	}

	public ItemStack build() {
		ItemStack finalItem = this.item;
		finalItem.setItemMeta(this.meta);

		return finalItem;
	}

	// ============================== Getters ============================== //

	public String getName() {
		return this.meta.getDisplayName();
	}

	// =========================== Static Methods =========================== //
	public static ItemStack createNamedItem(Material mat, int amount, String name) {
		ItemStack item = new ItemStack(mat, amount);

		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		item.setItemMeta(im);

		return item;
	}

	public static List<String> getLore(ItemMeta meta) {
		return meta.hasLore() ? meta.getLore() : new ArrayList<>();
	}

}
