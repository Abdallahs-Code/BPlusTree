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

    public boolean deleteRecordsBySSN(String ssn) {
        for (Block block : blocks) {
            for (Record record : block.getRecords()) {
                if (record.getSSN().equals(ssn) && !record.isDeleted()) {
                    record.markDeleted();
                    return true;
                }
            }
        }
        return false;
    }


    public void printBlocks() {
        System.out.println("---- File Blocks ----");
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            System.out.println("Block " + i + " (" + block.getRecords().size() + " records)");
            for (int j = 0; j < block.getRecords().size(); j++) {
                Record r = block.getRecords().get(j);
                String status = r.isDeleted() ? "[DELETED]" : "[ACTIVE]";
                System.out.printf("  Slot %d: %s %s%n", j, status, r.toString());
            }
            System.out.println();
        }
    }



    public List<Block> getBlocks() {
        return blocks;
    }
}
