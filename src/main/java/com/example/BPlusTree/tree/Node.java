package com.example.BPlusTree.tree;

import java.util.ArrayList;
import java.util.List;

public class Node {
    protected boolean isLeaf;
    protected int order;
    protected final List<Integer> keys;
    protected Node parent;

    public Node(boolean isLeaf, int order) {
        this.isLeaf = isLeaf;
        this.order = order;
        this.keys = new ArrayList<>();
        this.parent = null;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public int getOrder() {
        return order;
    }

    public List<Integer> getKeys() {
        return keys;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
