package com.example.BPlusTree.tree;

import java.util.ArrayList;
import java.util.List;

public class BPlusTree {
    private Node root;
    private final int order;

    public BPlusTree(int order) {
        if (order < 2) {
            throw new IllegalArgumentException("Order must be at least 2");
        }
        this.order = order;
        this.root = new LeafNode(order);
    }

    public Node getRoot() {
        return root;
    }

    private LeafNode findLeafNode(int key) {
        Node current = root;
        while (!current.isLeaf()) {
            InternalNode internal = (InternalNode) current;
            int i = 0;
            while (i < internal.getKeys().size() && key >= internal.getKeys().get(i)) {
                i++;
            }
            current = internal.getPointers().get(i);
        }
        return (LeafNode) current;
    }

    private void splitInternal(InternalNode node) {
        List<Integer> keys = node.getKeys();
        List<Node> pointers = node.getPointers();

        int mid = (keys.size() - 1) / 2;
        int promotedKey = keys.get(mid);
        InternalNode rightNode = new InternalNode(order);

        List<Integer> rightKeys = new ArrayList<>(keys.subList(mid + 1, keys.size()));
        rightNode.getKeys().addAll(rightKeys);

        List<Node> rightPointers = new ArrayList<>(pointers.subList(mid + 1, pointers.size()));
        rightNode.getPointers().addAll(rightPointers);

        keys.subList(mid, keys.size()).clear();
        pointers.subList(mid + 1, pointers.size()).clear();

        for (Node child : rightNode.getPointers()) {
            if (child != null) {
                child.setParent(rightNode);
            }
        }

        Node parent = node.getParent();

        if (parent == null) {
            InternalNode newRoot = new InternalNode(order);
            newRoot.getKeys().add(promotedKey);
            newRoot.getPointers().add(node);       
            newRoot.getPointers().add(rightNode); 
            node.setParent(newRoot);
            rightNode.setParent(newRoot);
            root = newRoot;
        } else {
            rightNode.setParent(parent);
            ((InternalNode) parent).insertKey(promotedKey, rightNode);
            handleOverflow(parent);
        }
    }

    private void splitLeaf(LeafNode leaf) {
        LeafNode rightLeaf = new LeafNode(order);
        int splitIndex = (leaf.getKeys().size()) / 2;

        rightLeaf.getKeys().addAll(leaf.getKeys().subList(splitIndex, leaf.getKeys().size()));
        rightLeaf.getRecordPointers().addAll(leaf.getRecordPointers().subList(splitIndex, leaf.getRecordPointers().size()));

        leaf.getKeys().subList(splitIndex, leaf.getKeys().size()).clear();
        leaf.getRecordPointers().subList(splitIndex, leaf.getRecordPointers().size()).clear();

        rightLeaf.setNext(leaf.getNext());
        leaf.setNext(rightLeaf);

        int promotedKey = rightLeaf.getKeys().get(0);
        Node parent = leaf.getParent();

        if (parent == null) {
            InternalNode newRoot = new InternalNode(order);
            newRoot.getKeys().add(promotedKey);
            newRoot.getPointers().add(leaf);
            newRoot.getPointers().add(rightLeaf);
            leaf.setParent(newRoot);
            rightLeaf.setParent(newRoot);
            root = newRoot;
        } else {
            rightLeaf.setParent(parent);
            ((InternalNode) parent).insertKey(promotedKey, rightLeaf);
            handleOverflow(parent);
        }
    }

    private void handleOverflow(Node node) {
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            if (leaf.getKeys().size() > order) { 
                splitLeaf(leaf);
            }
        } else {
            InternalNode internal = (InternalNode) node;
            if (internal.getKeys().size() >= order) { 
                splitInternal(internal);
            }
        }
    }

    public void insert(int key, int recordPointer) {
        LeafNode leaf = findLeafNode(key);
        leaf.insertKey(key, recordPointer);
        handleOverflow(leaf);
    }
}