package com.aearost.chestsorter.tree;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
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
							return enchantedBookCompareTo(this.item, otherItem);
						}
						// If their metas don't match any one we specified, sort directly by name
						else {
							compared = otherItem.getType().name().compareTo(this.getName());
							if (compared > 0) {
								return 1;
							} else if (compared < 0) {
								return -1;
							} else {
								// If nothing matches, place the item at the end
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
				// Effects are the same
				int potion1Modifier = -1;
				if (potion1Data.isExtended()) {
					potion1Modifier = 0;
				} else if (potion1Data.isUpgraded()) {
					potion1Modifier = 1;
				}

				int potion2Modifier = -1;
				if (potion2Data.isExtended()) {
					potion2Modifier = 0;
				} else if (potion2Data.isUpgraded()) {
					potion2Modifier = 1;
				}

				if (potion1Modifier > potion2Modifier) {
					return -1;
				} else if (potion1Modifier < potion2Modifier) {
					return 1;
				} else {
					return 0;
				}
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
	 * Compares two enchanted books. Only looks at the first enchantment. Because of
	 * this, even if two books have the same first enchantment, they will be
	 * classified as separate stacks. Therefore, this compareTo method never returns
	 * 0.
	 * 
	 * @param book1
	 * @param book2
	 * @return 1 if the enchanted books are the "same" or -1 if book1 is greater
	 *         than book2
	 */
	@SuppressWarnings("deprecation")
	private int enchantedBookCompareTo(ItemStack book1, ItemStack book2) {
		EnchantmentStorageMeta book1Meta = (EnchantmentStorageMeta) book1.getItemMeta();
		EnchantmentStorageMeta book2Meta = (EnchantmentStorageMeta) book2.getItemMeta();
		String book1FirstEnchant = null;
		String book2FirstEnchant = null;

		for (Map.Entry<Enchantment, Integer> entry : book1Meta.getStoredEnchants().entrySet()) {
			book1FirstEnchant = entry.getKey().getName() + " " + entry.getValue();
			break;
		}

		for (Map.Entry<Enchantment, Integer> entry : book2Meta.getStoredEnchants().entrySet()) {
			book2FirstEnchant = entry.getKey().getName() + " " + entry.getValue();
			break;
		}

		int compared = book2FirstEnchant.compareTo(book1FirstEnchant);
		if (compared < 0) {
			return -1;
		} else {
			return 1;
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
