package com.example.BPlusTree.tree;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {
    private final List<Node> pointers;

    public InternalNode(int order) {
        super(false, order);
        this.pointers = new ArrayList<>();
    }

    public List<Node> getPointers() {
        return pointers;
    }

    public void insertKey(int key, Node pointer) {
        int insertIndex = 0;
        while (insertIndex < keys.size() && keys.get(insertIndex) < key) {
            insertIndex++;
        }
        keys.add(insertIndex, key);
        pointers.add(insertIndex + 1, pointer);
    }
}
