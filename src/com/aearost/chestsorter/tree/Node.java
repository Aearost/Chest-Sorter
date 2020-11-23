package com.aearost.chestsorter.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	public int compareTo(ItemStack otherItem) {
		if (this.isBlock && !otherItem.getType().isBlock()) {
			return 1;
		} else if (!this.isBlock && otherItem.getType().isBlock()) {
			return -1;
		}
		// Both are blocks or both are items
		else {
			if (this.hasItemMeta && otherItem.hasItemMeta()) {
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

//						Bukkit.broadcastMessage("-----");
//						Bukkit.broadcastMessage("this: " + this.item.toString());
//						Bukkit.broadcastMessage("other: " + otherItem.toString());
						if (thisMeta instanceof PotionMeta && otherMeta instanceof PotionMeta) {
							return potionCompareTo(this.item, otherItem);
						} else if (thisMeta instanceof EnchantmentStorageMeta
								&& otherMeta instanceof EnchantmentStorageMeta) {
							return enchantedItemCompareTo(this.item, otherItem);
						}

						// If their metas don't match any one we specified, sort directly by name
						else {
							compared = otherItem.getType().name().compareTo(this.getName());
							if (compared > 0) {
								return 1;
							} else if (compared < 0) {
								return -1;
							} else {
								// If names match, place in order of enchantment
								if (!this.isBlock && !otherItem.getType().isBlock()) {
									return enchantedItemCompareTo(this.item, otherItem);
								}
								// If nothing matches, place item at the end
								return 1;
							}
						}
					} else {
						// Both names correspond to the same exact item
						return 0;
					}
				}
			} else {
				if (this.isBlock) {
					if (!this.hasItemMeta && otherItem.hasItemMeta()) {
						return 1;
					} else if (this.hasItemMeta && !otherItem.hasItemMeta()) {
						return -1;
					} else {
						int compared = otherItem.getType().name().compareTo(this.name);
						if (compared > 0) {
							return 1;
						} else if (compared < 0) {
							return -1;
						} else {
							// Items are the same
							return 0;
						}
					}
				} else {
					// We are looking at two items; we want to group all spawn eggs together
					if (this.name.contains("SPAWN_EGG")) {
						if (!otherItem.getType().name().contains("SPAWN_EGG")) {
							return -1;
						}
					} else if (otherItem.getType().name().contains("SPAWN_EGG")) {
						if (!this.name.contains("SPAWN_EGG")) {
							return 1;
						}
					}

					int compared = otherItem.getType().name().compareTo(this.name);
					if (compared > 0) {
						return 1;
					} else if (compared < 0) {
						return -1;
					} else {
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
		}
	}

	/**
	 * Compares two potions that have 1 effect. May work weirdly with potions that
	 * have more than 1 effect.
	 * 
	 * @param potion1
	 * @param potion2
	 * @return 0 if the potions are the same, 1 if potion1 is less than potion2 or
	 *         -1 if potion1 is greater than potion2
	 */
	private int potionCompareTo(ItemStack potion1, ItemStack potion2) {
		String potion1Type = potion1.getType().name();
		String potion2Type = potion2.getType().name();

		if (potion1Type.equals(potion2Type)) {
			PotionData potion1Data = ((PotionMeta) potion1.getItemMeta()).getBasePotionData();
			PotionData potion2Data = ((PotionMeta) potion2.getItemMeta()).getBasePotionData();
			String potion1Effect = potion1Data.getType().name();
			String potion2Effect = potion2Data.getType().name();
			int compared = potion2Effect.compareTo(potion1Effect);
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
			String potion = "POTION";
			String splashPotion = "SPLASH_POTION";
			if (potion1Type.equals(potion)) {
				return 1;
			} else if (potion1Type.equals(splashPotion)) {
				if (potion2Type.equals(potion)) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return -1;
			}
		}
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
	 * Determines if the given PotionData is normal, extended or upgraded.
	 * 
	 * @param potion
	 * @return -1 if it's a normal potion, 0 if it is an extended potion or 1 if it
	 *         is an upgraded potion
	 */
	private int getPotionModifier(PotionData potion) {
		if (potion.isExtended()) {
			return 0;
		} else if (potion.isUpgraded()) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * Compares two enchanted items. Also works on two enchanted books.
	 * 
	 * @param eItem1
	 * @param eItem2
	 * @return 0 if the items have the same exact enchantments, same number and same
	 *         durability - 1 if eItem1 is less than eItem2 or -1 if eItem1 is
	 *         greater than eItem2
	 */
	private int enchantedItemCompareTo(ItemStack eItem1, ItemStack eItem2) {
		Set<Entry<Enchantment, Integer>> e1ItemMetaEnchants;
		Set<Entry<Enchantment, Integer>> e2ItemMetaEnchants;
		if (eItem1.getItemMeta() instanceof EnchantmentStorageMeta
				&& eItem2.getItemMeta() instanceof EnchantmentStorageMeta) {
			e1ItemMetaEnchants = ((EnchantmentStorageMeta) eItem1.getItemMeta()).getStoredEnchants().entrySet();
			e2ItemMetaEnchants = ((EnchantmentStorageMeta) eItem2.getItemMeta()).getStoredEnchants().entrySet();
		} else {
			e1ItemMetaEnchants = eItem1.getItemMeta().getEnchants().entrySet();
			e2ItemMetaEnchants = eItem2.getItemMeta().getEnchants().entrySet();
		}
		List<String> eItem1Enchants = new ArrayList<String>();
		List<String> eItem2Enchants = new ArrayList<String>();

		for (Map.Entry<Enchantment, Integer> entry : e1ItemMetaEnchants) {
			eItem1Enchants.add(entry.getKey().getKey() + "," + entry.getValue());
		}

		for (Map.Entry<Enchantment, Integer> entry : e2ItemMetaEnchants) {
			eItem2Enchants.add(entry.getKey().getKey() + "," + entry.getValue());
		}

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
					if (eItem1Damage < eItem2Damage) {
						return -1;
					} else if (eItem1Damage > eItem2Damage) {
						return 1;
					} else {
						return 0;
					}
				} else {
					return 0;
				}
			}
			return 0;
		}
	}

	@Override
	public String toString() {
		if (this.hasItemMeta && (!(this.item.getItemMeta() instanceof PotionMeta)
				|| !(this.item.getItemMeta() instanceof EnchantmentStorageMeta))) {
			return this.item.getItemMeta().getDisplayName();
		}
		return name;
	}
}
