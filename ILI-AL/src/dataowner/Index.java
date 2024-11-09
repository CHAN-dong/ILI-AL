package dataowner;


import java.io.Serializable;

import static dataowner.Parameter.useBitset;

public class Index implements Serializable {
    public int len;
    public Segment[] segments;

    public LMFilter[] getFilters() {
        int len = segments.length;
        LMFilter[] filters = new LMFilter[len];
        for (int i = 0; i < segments.length; ++i) {
            filters[i] = segments[i].filter;
        }
        return filters;
    }

    public boolean contains (int segmentPos, long key) {
        return segments[segmentPos].isInFilter(key);
    }

    public Index(long keyword, long[] ids) {
        OptPLA optPLA = new OptPLA(ids);
        segments = optPLA.getSegments();

        for (Segment seg : segments) {
            if (useBitset) seg.setBitArray();
            seg.setVerificationObjects(keyword);
        }

    }

    public void setBitArray() {

        for (Segment seg : segments) {
            seg.setBitArray();
        }
    }

    //find the segment pos of the tar by exponential search
    public int findSeg(int pos, long tar) {
        int n = segments.length;
        int p = pos + 1;
        while (p < n && segments[p].segData[0] <= tar) {
            p++;
        }
        return p - 1;


//        //exponential search
//        int bound = 1;
//        while (pos + bound < n && segments[pos + bound].segData[0] < tar) {
//            bound *= 2;
//        }
//
//        //binary search
//        int l = pos + bound / 2, r = Math.min(pos + bound, n - 1);
//        while (l <= r) {
//            int mid = (l + r) / 2;
//            if (segments[mid].segData[0] <= tar) l = mid + 1;
//            else r = mid - 1;
//        }
//        return Math.max(l - 1, 0);
    }




}
