package com.aearost.chestsorter.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

import com.aearost.chestsorter.ChatUtils;

public class Node {
	private Node leftChild;
	private Node rightChild;
	private ItemStack item;
	private String name;
	private int quantity;
	private boolean isBlock;
	private boolean hasItemMeta;

	public Node(ItemStack item) {
		this.leftChild = null;
		this.rightChild = null;
		this.item = item;
		this.name = item.getType().name();
		this.quantity = item.getAmount();
		this.isBlock = item.getType().isBlock();
		this.hasItemMeta = item.hasItemMeta();
	}

	public Node getLeftChild() {
		return leftChild;
	}

	public void setLeftChild(Node leftChild) {
		this.leftChild = leftChild;
	}

	public Node getRightChild() {
		return rightChild;
	}

	public void setRightChild(Node rightChild) {
		this.rightChild = rightChild;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public void setMaterial(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public boolean isBlock() {
		return isBlock;
	}

	public void setBlock(boolean isBlock) {
		this.isBlock = isBlock;
	}

	public boolean hasItemMeta() {
		return hasItemMeta;
	}

	public void setHasItemMeta(boolean hasItemMeta) {
		this.hasItemMeta = hasItemMeta;
	}

	@Override
	public String toString() {
		if (this.hasItemMeta && (!(this.item.getItemMeta() instanceof PotionMeta)
				|| !(this.item.getItemMeta() instanceof EnchantmentStorageMeta))) {
			return this.item.getItemMeta().getDisplayName();
		}
		return name;
	}

	/**
	 * 
	 * @param otherItem
	 * @return -1 if this.item should be placed after otherItem, 0 if they're the
	 *         same, or 1 if this.item should be placed before otherItem
	 */
	public int compareTo(ItemStack otherItem) {
		if (this.isBlock && !otherItem.getType().isBlock()) {
			return 1;
		} else if (!this.isBlock && otherItem.getType().isBlock()) {
			return -1;
		} else if (this.hasItemMeta && otherItem.hasItemMeta()) {
			String thisStripped = ChatUtils.stripColor(this.item.getItemMeta().getDisplayName());
			String otherStripped = ChatUtils.stripColor(otherItem.getItemMeta().getDisplayName());
			int compared = otherStripped.compareTo(thisStripped);
			if (compared > 0) {
				return 1;
			} else if (compared < 0) {
				return -1;
			} else {
				// Both display names are equal; check if they are empty
				if (thisStripped.isEmpty()) {
					// Because they are empty, we are almost sure that the items being compared are
					// from base Minecraft; first, check their metas to make sure they shouldn't be
					// sorted in a specific way
					ItemMeta thisMeta = this.item.getItemMeta();
					ItemMeta otherMeta = otherItem.getItemMeta();
					if (thisMeta instanceof PotionMeta && otherMeta instanceof PotionMeta) {
						return potionCompareTo(this.item, otherItem);
					} else if (thisMeta instanceof EnchantmentStorageMeta
							&& otherMeta instanceof EnchantmentStorageMeta) {
						return enchantedItemCompareTo(this.item, otherItem);
					} else {
						// If their metas don't match any one we specified, sort directly by name
						compared = compareStrings(this.name, otherItem.getType().name());
						if (compared > 0) {
							return 1;
						} else if (compared < 0) {
							return -1;
						} else {
							// If names match, place in order of enchantment
							if (!this.isBlock && !otherItem.getType().isBlock()) {
								return enchantedItemCompareTo(this.item, otherItem);
							}
							// If names match, place item at the end
							return 1;
						}
					}
				} else {
					// Both names correspond to the same exact item
					return 0;
				}
			}
		} else if (this.isBlock && otherItem.getType().isBlock()) {
			// We are looking at two blocks; we want to group similar ones together
			int grouped = groupSimilarBlocks(this.name, otherItem.getType().name());
			if (grouped != 0) {
				return grouped;
			}

			if (!this.hasItemMeta && otherItem.hasItemMeta()) {
				return 1;
			} else if (this.hasItemMeta && !otherItem.hasItemMeta()) {
				return -1;
			} else {
				return compareStrings(this.name, otherItem.getType().name());
			}
		} else {
			// We are looking at two items; we want to group similar ones together
			// We want to avoid grouping with potions, as they have non-similar names (so
			// random items can come between them if we aren't careful)
			String replacementForOtherItemName = otherItem.getType().name();
			if (this.item.getItemMeta() instanceof PotionMeta) {
				this.name = "POTION";
			} else if (otherItem.getItemMeta() instanceof PotionMeta) {
				replacementForOtherItemName = "POTION";
			}

			int grouped = groupSimilarItems(this.name, replacementForOtherItemName);
			if (grouped != 0) {
				return grouped;
			}

			// If they are similar or are not being grouped, sort by name
			int compared = compareStrings(this.name, replacementForOtherItemName);
			if (compared > 0) {
				return 1;
			} else if (compared < 0) {
				return -1;
			} else {
				// If names are equal and passed the similar check, sort by meta
				if (!this.hasItemMeta && otherItem.hasItemMeta()) {
					return 1;
				} else if (this.hasItemMeta && !otherItem.hasItemMeta()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}

	/**
	 * Takes two vanilla block names and creates a list of all key words to group
	 * blocks by. For each group string created, if the pair of block names both
	 * contain the same group string, they are considered similar and 0 is returned.
	 * The same happens if both don't contain any group string. If only one of them
	 * contains the group string, both block names will need to be modified to
	 * determine how they get sorted. The block name that contained the group string
	 * will simply be replaced by the group string itself, while the other block
	 * needs to be replaced by any group string that it matches, if it does match
	 * one (in order to maintain alphabetical order once all matching block names
	 * are essentially stripped and sorted by group string). Once these replacements
	 * are done, both strings are compared and returned based on the compareStrings
	 * method.
	 * 
	 * @param block1Name
	 * @param block2Name
	 * @return -1 if block1Name should be placed after block2Name, 0 if both blocks
	 *         are similar, or 1 if block1Name should be placed before block2Name
	 */
	private int groupSimilarBlocks(String block1Name, String block2Name) {
		List<String> groupStrings = new ArrayList<String>(List.of("SAPLING", "ACACIA", "ANDESITE", "BED", "BIRCH",
				"BLACKSTONE", "BLOCK", "CARPET", "CONCRETE", "CORAL", "DARK_OAK", "DIORITE", "GLASS", "GRANITE", "ICE",
				"JUNGLE", "NETHER", "OAK", "PRISMARINE", "SANDSTONE", "SPRUCE", "STONE", "TERRACOTTA", "WOOL"));

		for (String groupString : groupStrings) {
			if (block1Name.contains(groupString) && !block2Name.contains(groupString)) {
				return compareStrings(groupString, replaceStringWithGroupString(block2Name, groupStrings));
			} else if (block2Name.contains(groupString) && !block1Name.contains(groupString)) {
				return compareStrings(replaceStringWithGroupString(block1Name, groupStrings), groupString);
			}
		}
		return 0;
	}

	/**
	 * Checks if the given string name contains any of the given group strings. If
	 * it does, return the corresponding group string. If it doesn't, return itself.
	 * 
	 * @param name
	 * @param groupStrings
	 * @return the group string contained by name, or name
	 */
	private String replaceStringWithGroupString(String name, List<String> groupStrings) {
		for (String groupString : groupStrings) {
			if (name.contains(groupString))
				return groupString;
		}
		return name;
	}

	/**
	 * Takes two vanilla item names and tests if they are similar in some way so
	 * that they may be properly grouped. They are considered similar if they both
	 * are or aren't able to be grouped by a specific grouping term. If this is the
	 * case, the next test begins. If only one of them is able to be grouped, then
	 * the items are not considered similar and the method exits there. Current
	 * groupings: spawn eggs, equipment (tools, swords and armor).
	 * 
	 * @param item1Name
	 * @param item2Name
	 * @return -1 if item1Name should be placed after item2Name, 0 if they are
	 *         similar, or -1 if item1Name should be placed before item2Name
	 */
	private int groupSimilarItems(String item1Name, String item2Name) {
		int grouped = groupSpawnEggs(item1Name, item2Name);
		if (grouped != 0) {
			return grouped;
		}
		return compareEquipmentType(item1Name, item2Name);
	}

	private int groupSpawnEggs(String item1Name, String item2Name) {
		if (item1Name.contains("SPAWN_EGG") && !item2Name.contains("SPAWN_EGG")) {
			return compareStrings("SPAWN_EGG", item2Name);
		} else if (!item1Name.contains("SPAWN_EGG") && item2Name.contains("SPAWN_EGG")) {
			return compareStrings(item1Name, "SPAWN_EGG");
		}
		return 0;
	}

	// Compare type first, then material
	private int compareEquipmentType(String equipment1Name, String equipment2Name) {
		String equipment1Type = equipment1Name.substring(equipment1Name.indexOf("_") + 1);
		String equipment2Type = equipment2Name.substring(equipment2Name.indexOf("_") + 1);
		int equipment1OrderingInt = getEquipmentTypeOrderingInt(equipment1Type);
		int equipment2OrderingInt = getEquipmentTypeOrderingInt(equipment2Type);
		return compareInts(equipment1OrderingInt, equipment2OrderingInt);
	}

	private int getEquipmentTypeOrderingInt(String equipmentType) {
		switch (equipmentType) {
		case "SHOVEL":
			return 9;
		case "PICKAXE":
			return 8;
		case "AXE":
			return 7;
		case "HOE":
			return 6;
		case "SWORD":
			return 5;
		case "HELMET":
			return 4;
		case "CHESTPLATE":
			return 3;
		case "LEGGINGS":
			return 2;
		case "BOOTS":
			return 1;
		default:
			return -1;
		}
	}

	/**
	 * Compares two potions that have 1 effect. May or may not work with potions
	 * that have more than 1 effect.
	 * 
	 * @param potion1
	 * @param potion2
	 * @return -1 if potion1 should be placed after potion2, 0 if the potions are
	 *         the same, or 1 if potion1 should be placed before potion2
	 */
	private int potionCompareTo(ItemStack potion1, ItemStack potion2) {
		String potion1Type = potion1.getType().name();
		String potion2Type = potion2.getType().name();

		if (potion1Type.equals(potion2Type)) {
			PotionData potion1Data = ((PotionMeta) potion1.getItemMeta()).getBasePotionData();
			PotionData potion2Data = ((PotionMeta) potion2.getItemMeta()).getBasePotionData();
			String potion1Effect = potion1Data.getType().name();
			String potion2Effect = potion2Data.getType().name();

			int compared = compareStrings(potion1Effect, potion2Effect);
			if (compared > 0) {
				return 1;
			} else if (compared < 0) {
				return -1;
			} else {
				// Effects are the same, check the modifiers
				int potion1Modifier = getPotionModifier(potion1Data);
				int potion2Modifier = getPotionModifier(potion2Data);
				return compareInts(potion1Modifier, potion2Modifier);
			}
		} else {
			return comparePotionTypes(potion1Type, potion2Type);
		}
	}

	/**
	 * Determines if the given PotionData is normal, extended or upgraded. The int
	 * returned determines in which order that type of modified potion will be
	 * sorted. Order is: normal, extended, upgraded.
	 * 
	 * @param potionData
	 * @return -1 if it's an upgraded potion, 0 if it is an extended potion, or 1 if
	 *         it is a normal potion
	 */
	private int getPotionModifier(PotionData potionData) {
		if (potionData.isUpgraded()) {
			return -1;
		} else if (potionData.isExtended()) {
			return 0;
		} else {
			return 1;
		}
	}

	/** TODO Make this work with other method to make things more consistent and not use ugly boolean strings
	 * Compares two different potion types. The int returned determines in which
	 * order these potion types will be sorted. Order is: POTION, SPLASH_POTION,
	 * LINGERING_POTION.
	 * 
	 * @param potion1Type
	 * @param potion2Type
	 * @return -1 potion1 should be placed after potion2 or 1 if potion1 should be
	 *         before after potion2
	 */
	private int getPotionType(String potionType) {
		if (potionType.equals("POTION")) {
			return -1;
		} else if (potionType.equals("SPLASH_POTION")) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Compares two different potion types. The int returned determines in which
	 * order these potion types will be sorted. Order is: POTION, SPLASH_POTION,
	 * LINGERING_POTION.
	 * 
	 * @param potion1Type
	 * @param potion2Type
	 * @return -1 potion1 should be placed after potion2 or 1 if potion1 should be
	 *         before after potion2
	 */
	private int comparePotionTypes(String potion1Type, String potion2Type) {
		if (potion1Type.equals("POTION")) {
			return 1;
		} else if (potion1Type.equals("SPLASH_POTION")) {
			if (potion2Type.equals("POTION")) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Compares two enchanted items. Also works on two enchanted books.
	 * 
	 * @param eItem1
	 * @param eItem2
	 * @return -1 if eItem1 should be placed after eItem2, 0 if the items have the
	 *         same exact enchantments and same durability, or 1 if eItem1 should be
	 *         before after eItem2
	 */
	private int enchantedItemCompareTo(ItemStack eItem1, ItemStack eItem2) {
		List<String> eItem1Enchants = getItemOrBookEnchants(eItem1);
		List<String> eItem2Enchants = getItemOrBookEnchants(eItem2);

		int eItem1EnchantsSize = eItem1Enchants.size();
		int eItem2EnchantsSize = eItem2Enchants.size();
		int smallerListLength = eItem1EnchantsSize < eItem2EnchantsSize ? eItem1EnchantsSize : eItem2EnchantsSize;
		for (int i = 0; i < smallerListLength; i++) {
			if (eItem2Enchants.get(i).compareTo(eItem1Enchants.get(i)) < 0) {
				return -1;
			} else if (eItem2Enchants.get(i).compareTo(eItem1Enchants.get(i)) > 0) {
				return 1;
			}
		}

		if (eItem1EnchantsSize > eItem2EnchantsSize) {
			return -1;
		} else if (eItem1EnchantsSize < eItem2EnchantsSize) {
			return 1;
		} else {
			// Sort in order of durability if all enchantments match (if the item has
			// durability)
			if (eItem1.getItemMeta() instanceof Damageable && eItem2.getItemMeta() instanceof Damageable) {
				boolean eItem1HasDamage = ((Damageable) eItem1.getItemMeta()).hasDamage();
				boolean eItem2HasDamage = ((Damageable) eItem2.getItemMeta()).hasDamage();
				if (eItem1HasDamage && !eItem2HasDamage) {
					return -1;
				} else if (!eItem1HasDamage && eItem2HasDamage) {
					return 1;
				} else if (eItem1HasDamage && eItem2HasDamage) {
					int eItem1Damage = ((Damageable) eItem1.getItemMeta()).getDamage();
					int eItem2Damage = ((Damageable) eItem2.getItemMeta()).getDamage();
					// We want the opposite effect of this method here, so we send the parameters in
					// the opposite order
					return compareInts(eItem2Damage, eItem1Damage);
				} else {
					return 0;
				}
			}
			return 0;
		}
	}

	/**
	 * Gets all enchantments from the given item or book and returns it as a list of
	 * strings following this format: "ENCHANTMENT_NAME,LEVEL".
	 * 
	 * @param eItem
	 * @return a list of strings representing all enchantments on the given item
	 *         following this format: "ENCHANTMENT_NAME,LEVEL"
	 */
	private List<String> getItemOrBookEnchants(ItemStack eItem) {
		Set<Entry<Enchantment, Integer>> eItemEnchantsSet;
		if (eItem.getItemMeta() instanceof EnchantmentStorageMeta) {
			eItemEnchantsSet = ((EnchantmentStorageMeta) eItem.getItemMeta()).getStoredEnchants().entrySet();
		} else {
			eItemEnchantsSet = eItem.getItemMeta().getEnchants().entrySet();
		}

		List<String> eItemEnchants = new ArrayList<String>();
		for (Map.Entry<Enchantment, Integer> entry : eItemEnchantsSet) {
			eItemEnchants.add(entry.getKey().getKey() + "," + entry.getValue());
		}
		return eItemEnchants;
	}

	/**
	 * @param int1
	 * @param int2
	 * @return -1, 0 or 1 as int1 is less than, equal to, or greater than int2
	 */
	private int compareInts(int int1, int int2) {
		if (int1 < int2) {
			return -1;
		} else if (int1 == int2) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * @param string1
	 * @param string2
	 * @return the opposite of String's compareTo method, because we are sorting
	 *         words the opposite way
	 */
	private int compareStrings(String string1, String string2) {
		return -1 * string1.compareTo(string2);
	}
}
