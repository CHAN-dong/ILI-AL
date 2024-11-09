package dataowner;
import it.unisa.dia.gas.jpbc.Element;
import server.VO;
import utils.BLS;
import utils.SHA;

import java.math.BigInteger;
import java.util.*;

import static dataowner.Parameter.rate;
import static utils.BLS.pairing;

public class DO {

    HashMap<Long, Index> indexes;

    HashMap<Long, Long> digest;

    HashMap<Long, byte[]> digestHash;

    public DO(HashMap<Long, long[]> invertedIndex) {
        indexes = new HashMap<>();
        digest = new HashMap<>();

        for (Map.Entry<Long, long[]> entry : invertedIndex.entrySet()) {
            long keyword = entry.getKey();
            long[] ids = entry.getValue();
            Index index = new Index(keyword, ids);
            index.len = ids.length;
            indexes.put(keyword, index);
            digest.put(keyword, ids[ids.length - 1]);
        }
    }

    byte[] getDigestHash(long[] ids) {
        byte[] hash = new byte[32];
        for (long id : ids) {
            hash = SHA.bytesXor(hash, SHA.hashToBytes(String.valueOf(id)));
        }
        return hash;
    }

    public void buildFilter() {
        digestHash = new HashMap<>();
        for (Map.Entry<Long, Index> entry : indexes.entrySet()) {
            Long keyword = entry.getKey();
            Index index = entry.getValue();
            byte[] hash = new byte[32];
            for (Segment seg : index.segments) {
                seg.buildFilter();
                hash = SHA.bytesXor(hash, getDigestHash(seg.segData));
            }
            digestHash.put(keyword, hash);
        }
    }

    public long[] getEndIds(long[] query) {
        int len = query.length;
        long[] endIds = new long[len];
        for (int i = 0; i < len; ++i) {
            endIds[i] = digest.get(query[i]);
        }
        return endIds;
    }

    public long[][] getEndIds(long[][] query) {
        int len = query.length;
        long[][] endIds = new long[len][];
        for (int i = 0; i < len; ++i) {
            endIds[i] = getEndIds(query[i]);
        }
        return endIds;
    }

    public LMFilter[][][] getFilters(long[][] query) {
        int len = query.length;
        LMFilter[][][] filter = new LMFilter[len][][];
        for (int i = 0; i < len; ++i) {
            filter[i] = getFilters(query[i]);
        }
        return filter;
    }

    public LMFilter[][] getFilters(long[] query) {
        int len = query.length;
        LMFilter[][] filters = new LMFilter[len][];
        for (int i = 0; i < query.length; ++i) {
            filters[i] = indexes.get(query[i]).getFilters();
        }
        return filters;
    }

    public byte[][][] getChainHash(long[][] query) {
        int len = query.length;
        byte[][][] hashes = new byte[len][][];
        for (int i = 0; i < len; ++i) {
            hashes[i] = getChainHash(query[i]);
        }
        return hashes;
    }

    public byte[][] getChainHash(long[] query) {
        int len = query.length;
        byte[][] hashes = new byte[len][];
        for (int i = 0; i < len; ++i) {
            hashes[i] = digestHash.get(query[i]);
        }
        return hashes;
    }

    public String getIdsAndModels(long[] query) {
        int len = query.length;
        long[] endId = getEndIds(query);
        BigInteger[] digest = null;long[][][] segments = null;
        if (digestHash != null) {
            digest = new BigInteger[len];
            for (int i = 0; i < len; ++i) {
                digest[i] = new BigInteger(digestHash.get(query[i])).abs();
            }
            segments = new long[len][][];
            for (int i = 0; i < len; ++i) {
                Index index = indexes.get(query[i]);
                long[][] seg = new long[index.segments.length][];
                for (int j = 0; j < index.segments.length; ++j) {
                    long[] modelAndIds = new long[index.segments[j].segData.length + 2];
                    modelAndIds[0] = (long) (index.segments[j].slop * rate);
                    modelAndIds[1] = (long) (index.segments[j].slop * index.segments[0].inter * rate);
                    for (int k = 0; k < index.segments[j].segData.length; ++k) {
                        modelAndIds[k + 2] = index.segments[j].segData[k];
                    }
                    seg[j] = modelAndIds;
                }
                segments[i] = seg;
            }
        }

        StringBuilder str = new StringBuilder();
        str.append("chainMessages--endId:\r\n");
        str.append(Arrays.toString(endId));
        if (digestHash != null) {
            str.append("\r\nchainMessages--chainMessages:\r\n");
            str.append("[");
            for (int i = 0; i < segments.length; ++i) {
                str.append("[");
                for (int j = 0; j < segments[i].length; ++j) {
                    str.append("[");
                    for (int k = 0; k < segments[i][j].length; ++k) {
                        str.append(segments[i][j][k]);
                        if (k == segments[i][j].length - 1) str.append("]");
                        else str.append(",");
                    }
                    if (j == segments[i].length - 1) str.append("]");
                    else str.append(",");
                }
                if (i == segments.length - 1) str.append("]");
                else str.append(",");
            }
            str.append("\r\nchainMessages--digestHash:\r\n");
            str.append("[");
            for (int i = 0; i < digest.length; ++i) {
                str.append("\"");
                str.append(digest[i]);
                str.append("\"");
                if (i != digest.length - 1) str.append(",");
                else str.append("]");
            }
        }
        return str.toString();
    }

    public HashMap<Long, Index> getIndexes() {
        return indexes;
    }

    public boolean verifyRes(long[] query, long[] endIds, VO vo) {
        int len = query.length;
        long tarId = -1, nextTarId = -1;
        int indexTag = 0, nextIndexTag = 0;
        int i = 0, j = 0, k = 0;
        Element hm = pairing.getG1().newZeroElement().getImmutable();
        while (tarId != Integer.MAX_VALUE) {
            int index = (int) vo.boundList.get(j)[0];
            long leftBound = vo.boundList.get(j)[1], rightBound = vo.boundList.get(j)[2];

            if (rightBound != Integer.MAX_VALUE) {
                byte[] hash = SHA.hashToBytes(query[index] + ":" + leftBound + "," + rightBound);
                hm.add(pairing.getG1().newElement().setFromHash(hash, 0, hash.length));
            } else if (endIds[index] != leftBound) {
                return false;
            }

            if (rightBound > nextTarId) {
                nextTarId = rightBound;
                nextIndexTag = index;
            }

            if (leftBound == tarId) {
                i = (i + 1) % len;
                if (i == indexTag) {
                    if (vo.res.get(k++) != tarId)
                        return false;
                } else {
                    //continue to find the next index
                    j++;
                    continue;
                }
            }

            //set the next tarId
            tarId = nextTarId;
            indexTag = nextIndexTag;
            i = (indexTag + 1) % len;
            j++;
        }

//        if (!BLS.verify(vo.aggSig, hm)) {
//            return false;
//        }

        return true;
    }

    public boolean optVerifyRes(long[] query, long[] endIds, VO vo) {
        long maxBound = -1;
        int k = 0, j = 0;
        byte[] hash = new byte[32];
        Element hm = pairing.getG1().newZeroElement().getImmutable();
        for (int i = 1; i < vo.tarIds.length; ++i) {
            long tarId = vo.tarIds[i];
            hash = SHA.bytesXor(hash, SHA.hashToBytes(String.valueOf(tarId)));
            if (tarId < maxBound) continue;
            if (vo.boundList.get(j).length == 2) {
                if (indexes.get(query[(int) vo.boundList.get(j)[0]]).segments[(int) vo.boundList.get(j)[1]].isInFilter(tarId)) {
                    return false;
                }
                j++;
            } else {
                int c = 0;
                while (j < vo.boundList.size() && vo.boundList.get(j)[1] == tarId) {
                    if (vo.boundList.get(j)[2] != Integer.MAX_VALUE) {
                        byte[] sh = SHA.hashToBytes(query[(int) vo.boundList.get(j)[0]] + ":" + vo.boundList.get(j)[1] + "," + vo.boundList.get(j)[2]);
                        hm.add(pairing.getG1().newElement().setFromHash(sh, 0, sh.length));
                    } else {
                        if (endIds[(int)vo.boundList.get(j)[0]] != vo.boundList.get(j)[1])
                            return false;
                    }
                    maxBound = Math.max(maxBound, vo.boundList.get(j)[2]);
                    j++;
                    c++;
                }
                if (c == query.length - 1) {
                    if (k >= vo.res.size() || vo.res.get(k++) != tarId)
                        return false;
                }
            }
        }
        if (!Arrays.equals(digestHash.get(query[(int)vo.tarIds[0]]), hash)) {
            return false;
        }

//        if (!BLS.aggVerify(vo.aggSig, messages)) {
//            return false;
//        }
        return true;
    }

}
