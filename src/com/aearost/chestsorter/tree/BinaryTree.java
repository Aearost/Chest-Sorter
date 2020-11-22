package com.aearost.chestsorter.tree;

import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class BinaryTree {
	private Node root;

	private BinaryTree() {
	}

	public static BinaryTree makeBinaryTree(ItemStack[] items) {

		BinaryTree tree = new BinaryTree();
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

	public void add(ItemStack item) {
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

	public void traverseInOrder(Node node) {
		if (node != null) {
			traverseInOrder(node.getLeftChild());
			Bukkit.broadcastMessage(node.getItem().toString());
			traverseInOrder(node.getRightChild());
		}
	}

	public ItemStack[] translateInOrder(int chestLength) {
		ItemStack[] itemsInOrder = new ItemStack[chestLength];
		if (root == null) {
			return itemsInOrder;
		}

		Stack<Node> s = new Stack<Node>();
		Node curr = root;

		// traverse the tree
		while (curr != null || s.size() > 0) {

			/*
			 * Reach the left most Node of the curr Node
			 */
			while (curr != null) {
				/*
				 * place pointer to a tree node on the stack before traversing the node's left
				 * subtree
				 */
				s.push(curr);
				curr = curr.getLeftChild();
			}

			/* Current must be NULL at this point */
			curr = s.pop();

			//System.out.print(curr.data + " ");

			/*
			 * we have visited the node and its left subtree. Now, it's right subtree's turn
			 */
			curr = curr.getRightChild();
		}
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node node) {
		this.root = node;
	}
}
