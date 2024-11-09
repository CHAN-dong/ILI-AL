package utils;

import dataowner.OptPLA;
import dataowner.Segment;

import java.util.Arrays;

public class BloomFilterTest {


    public static void main(String[] args) {
        long s,e;
        long[] arr = Utils.buildRandArr(100000, 0, 10000000, null);
//        long[] arr = {1,2,3,4,5,20,21,22,23,24};
        Arrays.sort(arr);

        int len = 100000;
        long[] queryArr = Utils.buildRandArr(len, 0, 100000000, null);
        long[] testQueryArr = Utils.buildRandArr(10000, 0, 100000000, null);


        OptPLA optPLA = new OptPLA(arr);
        Segment[] segments = optPLA.getSegments();
        System.out.println(segments.length);

        for (int i = 0; i < 11; ++i) {
            int part = (int) Math.pow(2, i);
            System.out.println("part:" + part);

            BloomFilter<Long> longBloomFilter = new BloomFilter<>(part, len, 1);
            for (long ele : arr)
                longBloomFilter.add(ele);
            double falsePositiveProbability = longBloomFilter.getFalsePositiveProbability();
            System.out.println("BloomFilter false positive probability: " + falsePositiveProbability);
            System.out.println("BloomFilter size: " + longBloomFilter.size());

            for (long query : testQueryArr) longBloomFilter.contains(query);
            s = System.nanoTime();
            for (long query : queryArr) {
                longBloomFilter.contains(query);
            }
            e = System.nanoTime();
            System.out.println("bloom find time: " + (e - s) / len + "ns");

            for (Segment segment : segments) {
//                Segment.setPartition(part);
                segment.setBitArray();
//                System.out.println(segment.getBitArray());
                System.out.println("segment bitsize: " + segment.leftBitArray.arrayLength);
                System.out.println("segment false positive rate:" + (1 - segment.getBitSetRate()));
            }

//            for (long query : testQueryArr) segments[0].optFindBound(query);
//            s = System.nanoTime();
////            for (long query : queryArr) {
//////                query = 3;
////                segments[0].contain(query);
//////                if (bounds[0] > query || bounds[1] < query) {
//////                    System.out.println(query + ":" + bounds[0] + "," + bounds[1]);
//////                }
////            }
//            e = System.nanoTime();
//            System.out.println("seg find time: " + (e - s) / len + "ns");
        }

    }

}
