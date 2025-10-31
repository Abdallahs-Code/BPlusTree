package com.example.BPlusTree.Storage;

import java.util.ArrayList;
import java.util.List;

public class FileSystem {
    private final List<Block> blocks = new ArrayList<>();

    public int insertRecord(Record record) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.addRecord(record)) {
                int recordIndex = block.getRecords().size() - 1;
                return (i * (Block.BLOCK_SIZE / Block.RECORD_SIZE)) + recordIndex;
            }
        }
        Block newBlock = new Block();
        newBlock.addRecord(record);
        blocks.add(newBlock);
        int blockIndex = blocks.size() - 1;
        return (blockIndex * (Block.BLOCK_SIZE / Block.RECORD_SIZE));
    }

    public void printBlocks() {
        System.out.println("---- File Blocks ----");
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + " (" + blocks.get(i).getRecords().size() + " records)");
        }
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}
