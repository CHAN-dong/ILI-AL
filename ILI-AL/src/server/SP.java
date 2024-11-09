package server;

import dataowner.Index;
import dataowner.Parameter;
import dataowner.Segment;
import it.unisa.dia.gas.jpbc.Element;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.util.*;

import static dataowner.Parameter.useBitset;

public class SP {
    HashMap<Long, Index> indexMap;

    public SP(HashMap<Long, Index> indexMap) {
        this.indexMap = indexMap;
    }

    public VO query(long[] keywords) {

        VO vo = new VO();
        int len = keywords.length;

        long tarId = 0;
        int indexTag = 0;
        Index[] queryIndex = new Index[len];
        int[] indexSegPos = new int[len];
        int[] indexScanPos = new int[len];
        for (int i = 0; i < len; ++i) {
            queryIndex[i] = indexMap.get(keywords[i]);
        }

        List<Element> tmpSig = new ArrayList<>();
        List<long[]> tmpBound = new ArrayList<>();
        Element maxBoundSig = null;
        long[] maxBound = new long[]{0,-1,-1};
        int i = 0;
        while (tarId != Integer.MAX_VALUE) {
            int segP = queryIndex[i].findSeg(indexSegPos[i], tarId);
            if (indexSegPos[i] != segP) {
                indexSegPos[i] = segP;
                indexScanPos[i] = 0;
            }
            int leftBoundPos, rightBoundPos;
            long leftBound, rightBound;
            if (useBitset)  leftBoundPos = queryIndex[i].segments[indexSegPos[i]].optFindBound(indexScanPos[i], tarId);
            else {
                leftBoundPos = queryIndex[i].segments[indexSegPos[i]].findBound(indexScanPos[i], tarId);
            }

            rightBoundPos = leftBoundPos + 1;

            if (leftBoundPos == -1) {
                leftBound = 0;
                rightBound = queryIndex[i].segments[indexSegPos[i]].segData[0];
            } else if (rightBoundPos == queryIndex[i].segments[indexSegPos[i]].segData.length) {
                rightBoundPos = 0;
                leftBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos];
                if (++indexSegPos[i] < queryIndex[i].segments.length) {
                    rightBound = queryIndex[i].segments[indexSegPos[i]].segData[0];
                } else {
                    rightBound = Integer.MAX_VALUE;
                }
            } else {
                leftBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos];
                rightBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos + 1];
            }
            indexScanPos[i] = rightBoundPos;

            long[] bound = new long[]{i, leftBound, rightBound};
            Element sig = null;
            if (rightBound != Integer.MAX_VALUE) {
                sig = queryIndex[i].segments[indexSegPos[i]].verificationObjects[rightBoundPos];
            }

            if (rightBound > maxBound[2]) {
                maxBound = bound;
                maxBoundSig = sig;
            }

            if (leftBound == tarId) {
                if (sig != null) tmpSig.add(sig);
                tmpBound.add(bound);
                i = (i + 1) % len;
                if (i == indexTag) {
                    //all index has tar, so add the tarId to the results
                    vo.sigList.addAll(tmpSig);
                    vo.boundList.addAll(tmpBound);
                    vo.res.add(tarId);
                    tmpSig = new ArrayList<>();
                    tmpBound = new ArrayList<>();
                } else {
                    //continue to find the next index
                    continue;
                }
            } else {
                if (maxBoundSig != null) vo.sigList.add(maxBoundSig);
                vo.boundList.add(maxBound);
                //need add a false proof if maxBound not a false proof
                if (maxBound[1] == tarId) {
                    if (sig != null) vo.sigList.add(sig);
                    vo.boundList.add(bound);
                    tmpSig = new ArrayList<>();
                    tmpBound = new ArrayList<>();
                }
            }

            //set the next tarId
            tarId = maxBound[2];
            indexTag = (int) maxBound[0];
            i = (indexTag + 1) % len;
        }

        return vo;
    }

    public VO optQuery(long[] keywords) {

        VO vo = new VO();
        int len = keywords.length;
        Index[] queryIndex = new Index[len];
        int[] indexSegPos = new int[len];
        int tarKeyword = 0;
        for (int i = 0; i < len; ++i) {
            queryIndex[i] = indexMap.get(keywords[i]);
            if (queryIndex[i].len < queryIndex[tarKeyword].len) {
                tarKeyword = i;
            }
        }

        int k = 0;
        long[] tarIds = new long[queryIndex[tarKeyword].len + 1];
        tarIds[k++] = tarKeyword;
        vo.setTarIds(tarIds);
        long maxBound = -1;
        for (Segment seg : queryIndex[tarKeyword].segments) {
            for (long id : seg.segData) {
                tarIds[k++] = id;
                if (id < maxBound) continue;
                //find in filter
                int i = (tarKeyword + 1) % len;
                while (i != tarKeyword) {
                    int segP = queryIndex[i].findSeg(indexSegPos[i], id);
                    if (!queryIndex[i].contains(segP, id)) {
                        vo.boundList.add(new long[]{i, indexSegPos[i]});
                        break;
                    }
                    i = (i + 1) % len;
                }
                if (i != tarKeyword) continue;

                //filter is in, continue to find by index
                i = (tarKeyword + 1) % len;
                while (i != tarKeyword) {
                    long leftBound, rightBound;
                    int leftBoundPos, rightBoundPos;
                    if (useBitset) {
//                        if (indexSegPos[i] >= queryIndex[i].segments.length) {
//                            int a = 1;
//                        }
                        leftBoundPos = queryIndex[i].segments[indexSegPos[i]].optFindBound(0, id);
                    }
                    else {
                        leftBoundPos = queryIndex[i].segments[indexSegPos[i]].findBound(0, id);
                    }
                    rightBoundPos = leftBoundPos + 1;

                    if (leftBoundPos == -1) {
                        leftBound = 0;
                        rightBound = queryIndex[i].segments[indexSegPos[i]].segData[0];
                    } else if (rightBoundPos == queryIndex[i].segments[indexSegPos[i]].segData.length) {
                        leftBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos];
                        if (++indexSegPos[i] < queryIndex[i].segments.length - 1) {
                            rightBoundPos = 0;
                            rightBound = queryIndex[i].segments[indexSegPos[i]].segData[0];
                        } else {
                            rightBound = Integer.MAX_VALUE;
                        }
                    } else {
                        leftBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos];
                        rightBound = queryIndex[i].segments[indexSegPos[i]].segData[leftBoundPos + 1];
                    }
                    maxBound = Math.max(maxBound, rightBound);
                    if (rightBound != Integer.MAX_VALUE) {
                        Element sig = queryIndex[i].segments[indexSegPos[i]].verificationObjects[rightBoundPos];
                        vo.sigList.add(sig);
                    }
                    long[] bound = new long[]{i, leftBound, rightBound};
                    vo.boundList.add(bound);
                    if (leftBound != id) break;
                    i = (i + 1) % len;
                }
                if (i == tarKeyword) vo.res.add(id);
            }
        }
        return vo;
    }


    public void addALT(int partition) {
        useBitset = true;
        Parameter.P_ALT = partition;
        for (Map.Entry<Long, Index> entry : indexMap.entrySet()) {
            Index index = entry.getValue();
            index.setBitArray();
        }
    }

    public long getIndexSize() {
        System.setProperty("java.vm.name", "Java HotSpot(TM) ");
        long sz = ObjectSizeCalculator.getObjectSize(indexMap);
        return sz;
    }


}
