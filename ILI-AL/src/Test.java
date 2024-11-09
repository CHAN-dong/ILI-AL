import dataowner.BitArray;
import dataowner.DO;
import dataowner.LMFilter;
import dataowner.Parameter;
import server.SP;
import server.VO;
import utils.IOTools;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
        String invertPath;



        invertPath = "./test.txt";
        Test(invertPath);

//        System.out.println("----------------------------------------");
//        Parameter.setALTPar(256, 1, true);
//        invertPath = "E:\\dataset\\keywords\\dblp_100w_inverted.txt";
//        Test(invertPath);
//        System.out.println("----------------------------------------");
//        Parameter.setALTPar(256, 1, true);
//        invertPath = "E:\\dataset\\keywords\\Twitter_100w_inverted.txt";
//        Test(invertPath);
//        System.out.println("----------------------------------------");
//        Parameter.setALTPar(256, 2, true);
//        invertPath = "E:\\dataset\\keywords\\Normal_100w_inverted.txt";
//        Test(invertPath);
//        System.out.println("----------------------------------------");
//        Parameter.setALTPar(32, 2, true);
//        invertPath = "E:\\dataset\\keywords\\Uniform_100w_inverted.txt";
//        Test(invertPath);


//        invertPath = "E:\\dataset\\keywords\\test_inverted.txt";
//        errAndPartTest(invertPath);

    }

    public static void errAndPartTest(String invertPath) throws IOException {
        HashMap<Long, long[]> invertedIndex = IOTools.readData(invertPath);
        int queryLen = 1000;
        long[][] query = Utils.generateQuery(queryLen, 4, 1, invertedIndex.size());

        List<List<Long>> queryTime = new ArrayList<>();
        List<List<Double>> indexSize = new ArrayList<>();

        long s, e;
        for (int err = 2; err <= 1024; err *= 2) {
            List<Long> timeList = new ArrayList<>();
            List<Double> indexSizeList = new ArrayList<>();
            queryTime.add(timeList);
            indexSize.add(indexSizeList);
            double basicIndexSize = 0;
            for (int part = 0; part <= 8; ) {
//                System.out.println("err: " + err + ", part: " + part);
                if (part == 0) {
                    Parameter.setALTPar(err-1, part, false);
                    part = 1;
                } else {
                    Parameter.setALTPar(err-1, part, true);
                    part *= 2;
                }
                DO dataOwner = new DO(invertedIndex);
                SP server = new SP(dataOwner.getIndexes());
                double sz = server.getIndexSize() / 1024.0 / 1024.0;
                if (part == 1) basicIndexSize = sz;
                indexSizeList.add((double)Math.round((sz - basicIndexSize) * 100) / 100);

                for (int i = 0; i < queryLen; ++i) {
                    server.query(query[i]);
                }

                s = System.nanoTime();
                for (int i = 0; i < queryLen; ++i) {
                    server.query(query[i]);
                }
                e = System.nanoTime();
                timeList.add((e - s) / queryLen);
            }
        }
        System.out.println("query time:");
        for (int i = 0; i < indexSize.size(); ++i) {
            System.out.println(indexSize.get(i).toString());
            System.out.println();
        }
    }

    public static void Test(String invertPath) throws IOException {
        HashMap<Long, long[]> invertedIndex = IOTools.readData(invertPath);
        int keywordSize = invertedIndex.size();
        long[][] query;
        int queryLen = 1000;
        long s, e;

        Parameter.setALTPar(255, 1, true);

        s = System.currentTimeMillis();
        DO dataOwner = new DO(invertedIndex);
        e = System.currentTimeMillis();
        System.out.println("Index build time: " + (e - s) + "ms");

        SP server = new SP(dataOwner.getIndexes());
        long indexSize = server.getIndexSize();
        System.out.println( "Index size: " + indexSize / 1024.0 / 1024.0 + "mb");

//        for (int c = 2; c <= 10; ++c) {
//            query = Utils.generateQuery(queryLen, c, 1, keywordSize);
////            long[][] endIds = dataOwner.getEndIds(query);
//            VO[] vos = new VO[queryLen];
//            for (int i = 0; i < queryLen; ++i) {
//                vos[i] = server.query(query[i]);
//            }
//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
//                vos[i] = server.query(query[i]);
//            }
//            e = System.nanoTime();
//            System.out.println("query keywords: " + c + ", query time: " + (e - s) / queryLen + "ns");
//            for (int i = 0; i < queryLen; ++i) {
//                vos[i].aggregateSig();
//            }
//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
//                vos[i].aggregateSig();
//            }
//            e = System.nanoTime();
//            System.out.println("query keywords: " + c + ", agg sig time: " + (e - s) / queryLen + "ns");
//            s = 0;
//            for (int i = 0; i < queryLen; ++i) {
//                s += vos[i].getVoSize();
//            }
//            System.out.println("query keywords: " + c + ", vo size: " + s / queryLen / 1024.0 + "kb");
//            for (int i = 0; i < queryLen; ++i) {
//                dataOwner.verifyRes(query[i], endIds[i], vos[i]);
//            }
//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
//                dataOwner.verifyRes(query[i], endIds[i], vos[i]);
//            }
//            e = System.nanoTime();
//            System.out.println("query keywords: " + c + ", verify time: " + (double)(e - s) / queryLen / 1e6 + "ms");
        }


//        for (int c = 3; c <= 3; ++c) {
//            long[] c_query = Utils.generateQuery(c, 1, keywordSize);
////            long[] c_query = new long[]{455, 909, 1363, 1817, 2271, 2725, 3179, 3633, 4087, 4541};
////            for (int x = 5; x < 7; x += 2) {
//                VO vo = server.query(c_query);
//                System.out.println("query" + c + ":");
//                System.out.println(Arrays.toString(c_query));
//                System.out.println(dataOwner.getIdsAndModels(c_query));
//                System.out.println("VO:");
//                System.out.println(vo.toString());
//                System.out.println();
////            }
//        }



//        for (int part = 1; part <= 16; part *= 2) {
//            Parameter.setLMFPar(part);
//            dataOwner.buildFilter();
//            int c = 4;
//            query = Utils.generateQuery(queryLen, c, 1, keywordSize);
//            long[][] endIds = dataOwner.getEndIds(query);
//            VO[] vos = new VO[queryLen];
//
//            for (int i = 0; i < queryLen; ++i) {
//                vos[i] = server.optQuery(query[i]);
//            }
//            s = System.nanoTime();
//            for (int i = 0;  i < queryLen; ++i) {
//                vos[i] = server.optQuery(query[i]);
//            }
//            e = System.nanoTime();
//            System.out.println("part: " + part + ", query keywords: " + c + ", opt query time: " + (e - s) / queryLen + "ns");
////                for (int i = 0; i < queryLen; ++i) {
////                    vos[i].aggregateSig();
////                }
////                s = System.nanoTime();
////                for (int i = 0; i < queryLen; ++i) {
////                    vos[i].aggregateSig();
////                }
////                e = System.nanoTime();
////                System.out.println("part: " + part + ", opt agg sig time: " + (e - s) / queryLen + "ns");
//            s = 0; e = 0;
//            for (int i = 0; i < queryLen; ++i) {
//                s += vos[i].getVoSize();
//                e += vos[i].sigList.size();
//            }
//            System.out.println("part: " + part + ", opt vo size: " + s / 1024.0 / queryLen + "kb");
//            System.out.println("part: " + part + ", opt vo sigList size: " + e / queryLen);
//            for (int i = 0; i < queryLen; ++i) {
//                dataOwner.optVerifyRes(query[i], endIds[i], vos[i]);
//            }
//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
//                dataOwner.optVerifyRes(query[i], endIds[i], vos[i]);
//            }
//            e = System.nanoTime();
//            System.out.println("part: " + part + ", query keywords: " + c + ", opt verify time: " + (e - s) / 1e6 / queryLen + "ms");
//        }
//    }

}



//            s = 0;
//            for (int i = 0; i < queryLen; ++i) {
////                voes[i].aggregateSig();
//                s += voes[i].getVoSize();
//                System.out.println("query:");
//                System.out.println(Arrays.toString(query[i]));
//                System.out.println("chainMessages:");
//                System.out.println(dataOwner.getIdsAndModels(query[i]));
//                System.out.println("VO:");
//                System.out.println(voes[i].toString());
//                System.out.println();
//            }
//            System.out.println("query keywords: " + c + ", vo size: " + s / queryLen / 1024.0 + "kb");



//        Parameter.setLMFPar(8);
//        s = System.currentTimeMillis();
//        dataOwner.buildFilter();
//        e = System.currentTimeMillis();
//        System.out.println("Filter build time: " + (e - s) + "ms");
//        indexSize = server.getIndexSize();
//        System.out.println( "Index size: " + indexSize / 1024.0 / 1024.0 + "mb");
//        for (int c = 2; c <= 2; ++c) {
////            query = Utils.generateQuery(queryLen, c, 1, keywordSize);
//            query[0] = Utils.generateQuery(c, 1, keywordSize);
//
//            for (int k = 0; k < query[0].length; ++k) {
//                System.out.println(invertedIndex.get(query[0][k]).length);
//            }

//            query[0] = new long[]{};
//            query[0] = new long[]{455, 909, 1363, 3633, 4087, 4541, 1367, 1366, 1364, 1365};
//            query[0] = new long[]{455, 909, 1363, 1817, 2271, 2725, 3179, 3633, 4087, 4541};
//            query[0] = new long[]{909, 1363, 1817, 2271, 3179, 3633, 4087, 4541};
//            query[0] = new long[]{909, 1817, 2725,3633,4087};
//            endIds = dataOwner.getEndIds(query);
//            LMFilter[][][] filters = dataOwner.getFilters(query);
//            byte[][][] chainHash = dataOwner.getChainHash(query);
//
//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
////                query[i] = new long[]{4541, 4541};
//                voes[i] = server.optQuery(query[i]);
//            }
//            e = System.nanoTime();
//            System.out.println("query keywords: " + c + ", query time: " + (e - s) / queryLen + "ns");
//
//            s = 0;
//            for (int i = 0; i < queryLen; ++i) {
////                voes[i].aggregateSig();
//                s += voes[i].getVoSize();

//                for (int k = 0; k < query[i].length; ++k) {
//                    System.out.println("query:");
//                    System.out.println(Arrays.toString(new long[]{query[i][k]}));
//                    System.out.println("chainMessages:");
//                    System.out.println(dataOwner.getIdsAndModels(new long[]{query[i][k]}));
//                    System.out.println("\n\n");
//                }

//                System.out.println("query:");
//                System.out.println(Arrays.toString(query[i]));
//                System.out.println("chainMessages:");
//                System.out.println(dataOwner.getIdsAndModels(query[i]));
//
//
//
//                System.out.println("VO:");
//                System.out.println(voes[i].toString());
//                System.out.println();
//            }
//            System.out.println("query keywords: " + c + ", vo size: " + s / queryLen / 1024.0 + "kb");

//            s = System.nanoTime();
//            for (int i = 0; i < queryLen; ++i) {
//                boolean b = dataOwner.optVerifyRes(query[i], endIds[i], voes[i]);
////                if (!b) {
////                    System.out.println("139: false");
////                }
//            }
//            e = System.nanoTime();
//            System.out.println("query keywords: " + c + ", verify time: " + (e - s) / queryLen + "ns");
//        }
