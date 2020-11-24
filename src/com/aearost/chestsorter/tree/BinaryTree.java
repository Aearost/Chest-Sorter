package com.aearost.chestsorter.tree;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class BinaryTree {
	private Node root;
	private ItemStack[] itemsInOrder;
	private int itemsInOrderIndex;

	private BinaryTree(int chestLength) {
		root = null;
		itemsInOrder = new ItemStack[chestLength];
	}

	public Node getRoot() {
		return this.root;
	}
	
	public ItemStack[] getItemsInOrder() {
		if (this.root == null) {
			return this.itemsInOrder;
		}
		traverseAndStoreInOrder(this.root);
		return this.itemsInOrder;
	}

	public void printInOrder(Node node) {
		if (node != null) {
			printInOrder(node.getLeftChild());
			Bukkit.broadcastMessage(node.getItem().toString() + ", Quantity=" + node.getQuantity());
			printInOrder(node.getRightChild());
		}
	}

	public static BinaryTree makeBinaryTree(ItemStack[] items) {
		BinaryTree tree = new BinaryTree(items.length);
		for (ItemStack item : items) {
			if (item != null) {
				tree.add(item);
			}
		}

		if (tree.getRoot() == null) {
			return null;
		}

		return tree;
	}

	private void add(ItemStack item) {
		root = addRecursive(root, item);
	}

	private Node addRecursive(Node current, ItemStack item) {
		if (current != null) {
//			Bukkit.broadcastMessage("Current: " + current.toString());
		}
//		Bukkit.broadcastMessage("Item: " + item.getType().name());

		if (current == null) {
//			Bukkit.broadcastMessage("New leaf: " + item.getType().name());
			return new Node(item);
		}

		int compared = current.compareTo(item);
		if (compared > 0) {
//			Bukkit.broadcastMessage("Going right");
			current.setRightChild(addRecursive(current.getRightChild(), item));
		} else if (compared < 0) {
//			Bukkit.broadcastMessage("Going left");
			current.setLeftChild(addRecursive(current.getLeftChild(), item));
		} else {
//			Bukkit.broadcastMessage("Item is same");
			// The item already exists, increment its quantity
			current.setQuantity(current.getQuantity() + item.getAmount());
			return current;
		}
		return current;
	}
	
	private void traverseAndStoreInOrder(Node node) {
		if (node != null) {
			traverseAndStoreInOrder(node.getLeftChild());
			
			while (node.getQuantity() > node.getItem().getMaxStackSize()) {
				ItemStack item = node.getItem().clone();
				item.setAmount(item.getMaxStackSize());
				this.itemsInOrder[this.itemsInOrderIndex] = item;
				this.itemsInOrderIndex++;
				node.setQuantity(node.getQuantity() - item.getMaxStackSize());
			}
			node.getItem().setAmount(node.getQuantity());
			this.itemsInOrder[this.itemsInOrderIndex] = node.getItem();
			this.itemsInOrderIndex++;
			
			traverseAndStoreInOrder(node.getRightChild());
		}
	}
}
