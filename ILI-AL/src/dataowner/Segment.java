package dataowner;

import it.unisa.dia.gas.jpbc.Element;
import utils.BLS;
import utils.Utils;

import java.io.Serializable;
import java.util.BitSet;

import static dataowner.OptPLA.*;
import static dataowner.Parameter.*;

public class Segment implements Serializable {
    double slop;
    double inter;
    public long[] segData;
    public Element[] verificationObjects;

    //Auxiliary Location Table
    public BitArray leftBitArray = null;
    public BitArray rightBitArray = null;
    public int offset;

    //filter
    public LMFilter filter;

    public Segment() {}

    public Segment(Segment segment) {
        this.slop = segment.slop;
        this.inter = segment.inter;
        this.segData = segment.segData;
    }
    public Segment(double slop, double inter, long[] segData) {
        this.slop = slop;
        this.inter = inter;
        this.segData = segData;
    }
    public void setBitArray() {
        double prePos = slop * (segData[0] - inter);
        double lastPos = slop * (segData[segData.length - 1] - inter);
        double nextPos;

        offset = (int) (prePos * P_ALT);
        int prePos0 = offset, prePos1 = (int) prePos;
        int lastPos0 = (int) (lastPos * P_ALT), lastPos1 = (int) lastPos;
        int nextPos0, nextPos1;

        int len =  lastPos0 - prePos0 + 1;

        int[] leftArray = new int[len];
        int[] rightArray = new int[len];
        int maxLeft = 0, minLeft = 0, maxRight = 0, minRight = 0;

        //set first element
        leftArray[0] = -prePos1;
        maxLeft = Math.max(leftArray[0], maxLeft);
        minLeft = Math.min(leftArray[0], minLeft);

        //set last element
        rightArray[lastPos0 - offset] = segData.length - 1 - lastPos1;
        maxRight = Math.max(rightArray[lastPos0 - offset], maxRight);
        minRight = Math.min(rightArray[lastPos0 - offset], minRight);

        //set remain elements
        for (int i = 1; i < segData.length; ++i) {
            nextPos =  slop * (segData[i] - inter);
            nextPos0 = (int) (nextPos * P_ALT);
            nextPos1 = (int) nextPos;
            if (nextPos0 != prePos0) {
                rightArray[prePos0 - offset] = i - 1 - prePos1;
                maxRight = Math.max(rightArray[prePos0 - offset], maxRight);
                minRight = Math.min(rightArray[prePos0 - offset], minRight);
                for (int j = prePos0 + 1; j < nextPos0; ++j) {
                    rightArray[j - offset] = i - 1 - j / P_ALT;
                    leftArray[j - offset] = rightArray[j - offset] + 1;
                    maxLeft = Math.max(leftArray[j - offset], maxLeft);
                    minLeft = Math.min(leftArray[j - offset], minLeft);
                    maxRight = Math.max(rightArray[j - offset], maxRight);
                    minRight = Math.min(rightArray[j - offset], minRight);
                }
                leftArray[nextPos0 - offset] = i - nextPos1;
                maxLeft = Math.max(leftArray[nextPos0 - offset], maxLeft);
                minLeft = Math.min(leftArray[nextPos0 - offset], minLeft);
                prePos0 = nextPos0;
                prePos1 = nextPos1;
            }
        }

        leftBitArray = new BitArray(len, minLeft, maxLeft);
        rightBitArray = new BitArray(len, minRight, maxRight);
        for (int i = 0; i < len; ++i) {
            leftBitArray.set(i, leftArray[i]);
            rightBitArray.set(i, rightArray[i]);
        }
    }

    public void setVerificationObjects(long keyword) {
        int len = segData.length;
        verificationObjects = new Element[len];
//        long pre = -1;
//        for (int i = 0; i < segData.length; ++i) {
//            String message = keyword + ":" + pre + "," + segData[i];
//            verificationObjects[i] = BLS.sign(message);
//            pre = segData[i];
//        }
    }

    //find left bound position,-1 means no left bound
    public int optFindBound(int prePos, long tar) {
        double pos = slop * (tar - inter);
        int originalPos = (int) pos;
        int bitArrayPos = (int) (pos * P_ALT) - offset;
        if (bitArrayPos < 0) {
            return -1;
        } else if (bitArrayPos >= leftBitArray.arrayLength) {
            return segData.length - 1;
        } else {
            int left = originalPos + leftBitArray.get(bitArrayPos);
            int right = originalPos + rightBitArray.get(bitArrayPos);
            if (left < right) {
                return Utils.findLeftBound(segData, tar, Math.max(prePos, left), right);
            } else {
                return right;
            }
        }
    }

    // return false : the key must not in seg, else the key may in seg or not
//    public boolean contain(long key) {
//        double pos = slop * (key - inter);
//        int bitArrayPos = (int) (pos * partition - offset);
//        if (bitArrayPos < 0 || bitArrayPos >= leftBitArray.arrayLength || bitSet.get(bitArrayPos)) return false;
//        return true;
//    }

    public int findBound(int prePos, long tar) {
        int pos = (int) (slop * (tar - inter));
        int l = Math.max(prePos, Math.min(pos - err - 1, segData.length - 1));
        int r = Math.min(segData.length - 1, Math.max(pos + err + 1, 0));
        int left = Utils.findLeftBound(segData, tar, l, r);

//        return new long[] {left == -1 ? Long.MIN_VALUE : segData[left], left == segData.length - 1 ? Long.MAX_VALUE : segData[left + 1]};
        return left;
    }


    public long getKey() {
        return segData[segData.length - 1];
    }

    public int size() {
        return segData.length;
    }

    //Find the left boundary
    public boolean lookup(long tar) {
        int pos = findLeftBound(tar);
        return pos >= 0 && pos < segData.length && segData[pos] == tar;
    }

    public int findLeftBound(long tar) {
        int pos = (int) (slop * (tar - inter));
        int l = Math.max(0, pos - err);
        int r = Math.min(segData.length - 1, pos + err);
        return Utils.findLeftBound(segData, tar, l, r);
    }

    public void buildFilter() {
        filter = new LMFilter(this);
    }

    public boolean isInFilter(long input) {
        double m = (slop * (input - inter));
        int p_l = Math.max(((int) m - err) * P_LMF + OF_LMF,0);
        int p_r = ((int)m + err) * P_LMF + OF_LMF;
        int dis = p_r - p_l;
        double m_ = m;
        for (int j = 1; j <= l; ++j) {
            m_ = m_ * dis;
            int tmp = ((int) m_ % dis + dis) % dis;
            m_ = tmp + (m_ - (int) m_);
            int pos = tmp + p_l;
            if (m_ == 0) {
                m_ = 3 * m;
            }
            if (pos < 0 || pos > filter.bitSet.size() || !filter.bitSet.get(pos)) return false;
        }
        return true;
    }


    public String getBitArray() {
        StringBuilder str = new StringBuilder();
        str.append("\n");
        str.append("LeftBitArray" + leftBitArray.arrayLength + ": ").append(leftBitArray.getBitArray());
        str.append("\n");
        str.append("RightBitArray" + rightBitArray.arrayLength + ": ").append(rightBitArray.getBitArray());
//        str.append("\n");
//        str.append("BitSet" + bitSet.size() + ": ");
//        for (int i = 0; i < leftBitArray.arrayLength; ++i) {
//            str.append(bitSet.get(i) ? 1 : 0).append(" ");
//        }
        return str.toString();
    }

    public double getBitSetRate() {
        int len = leftBitArray.arrayLength;
        int c= 0;
        for (int i = 0; i < len; ++i) {
            if (rightBitArray.get(i) < leftBitArray.get(i)) c++;
        }
        return (double) c / len;
    }


}
