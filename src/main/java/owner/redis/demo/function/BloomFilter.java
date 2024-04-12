package owner.redis.demo.function;

import java.util.BitSet;

/**
 * 自定义实现布隆过滤器
 */
public class BloomFilter {
    private BitSet bitSet;
    private int size;
    private int[] hashFunctions;

    public static void main(String[] args) {
        BloomFilter bloomFilter = new BloomFilter(1000, 3);
        bloomFilter.add("apple");
        bloomFilter.add("banana");

        System.out.println(bloomFilter.contains("apple")); // true
        System.out.println(bloomFilter.contains("orange")); // false
    }

    public BloomFilter(int size, int numHashFunctions) {
        this.size = size;
        this.bitSet = new BitSet(size);
        this.hashFunctions = new int[numHashFunctions];
    }

    public void add(String element) {
        for (int i = 0; i < hashFunctions.length; i++) {
            int hash = Math.abs(hash(element, i));
            bitSet.set(hash % size);
        }
    }

    public boolean contains(String element) {
        for (int i = 0; i < hashFunctions.length; i++) {
            int hash = Math.abs(hash(element, i));
            if (!bitSet.get(hash % size)) {
                return false;
            }
        }
        return true;
    }

    private int hash(String element, int index) {
        // 使用简单的哈希函数，可以根据实际情况选择更复杂的哈希函数
        return (element.hashCode() + index) % size;
    }
}
