package dataowner;

import java.io.Serializable;

public class BitArray implements Serializable {
    private long[] array;
    private int bitSize;
    public int arrayLength;

    private int minValue;

    // 构造函数，初始化数组
    public BitArray(int length, int minValue, int maxValue) {
        this.bitSize = Math.max(1, (int) Math.ceil(Math.log(maxValue - minValue + 1) / Math.log(2)));
        this.minValue = minValue;
        this.arrayLength = length;
        int totalBits = length * bitSize;
        int longSize = (totalBits + 63) / 64; // 每64位一个long，向上取整
        this.array = new long[longSize];
    }

    // 设置第index个元素的值为value
    public void set(int index, int value) {
        value = value - minValue;

        if (index < 0 || index >= arrayLength || value < 0 || value >= (1 << bitSize)) {
            throw new IllegalArgumentException("Index or value out of bounds");
        }
        int bitIndex = index * bitSize;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;

        // 清除目标位置的原值
        long mask = (1L << bitSize) - 1;
        array[longIndex] &= ~(mask << bitOffset);
        array[longIndex] |= ((long) value & mask) << bitOffset;

        // 如果跨越了long边界
        if (bitOffset + bitSize > 64) {
            int bitsInFirstLong = 64 - bitOffset;
            array[longIndex + 1] &= ~(mask >> bitsInFirstLong);
            array[longIndex + 1] |= ((long) value & mask) >> bitsInFirstLong;
        }
    }

    // 获取第index个元素的值
    public int get(int index) {
        if (index < 0 || index >= arrayLength) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        int bitIndex = index * bitSize;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;

        long mask = (1L << bitSize) - 1;
        long value = (array[longIndex] >>> bitOffset) & mask;

        // 如果跨越了long边界
        if (bitOffset + bitSize > 64) {
            int bitsInFirstLong = 64 - bitOffset;
            int bitsInNextLong = bitSize - bitsInFirstLong;
            value |= (array[longIndex + 1] & ((1L << bitsInNextLong) - 1)) << bitsInFirstLong;
        }

        return (int) value + minValue;
    }

    public String getBitArray() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arrayLength; ++i) {
            str.append(get(i)).append(" ");
        }
        return str.toString();
    }

    // 测试方法
    public static void main(String[] args) {
        int length = 10;
        int bitSize = 5;
        BitArray bitArray = new BitArray(length, bitSize, 0);

        // 设置一些值
        for (int i = 0; i < length; i++) {
            bitArray.set(i, i + 1);
        }

        // 获取并打印这些值
        for (int i = 0; i < length; i++) {
            System.out.println("Index " + i + ": " + bitArray.get(i));
        }
    }
}

