package dataowner;
import utils.Utils;

import java.util.BitSet;

import static dataowner.Parameter.*;
import static dataowner.Parameter.OF_LMF;

public class LMFilter{
    public BitSet bitSet;
    //y = slop * x + inter
    public void printBitSet() {
        System.out.print("[");
        for (int i = 0; i < bitSet.size(); ++i) {
            System.out.print(bitSet.get(i) ? "1" : "0");
            if (i == bitSet.size() - 1) System.out.print("]");
            else System.out.print(", ");
        }
    }

    public LMFilter(Segment seg) {
        bitSet = new BitSet();
        for (long data : seg.segData) {
            double m = (seg.slop * (data - seg.inter));
            int p_l = Math.max(((int) m - err) * P_LMF + OF_LMF,0);
            int p_r = ((int)m + err) * P_LMF + OF_LMF;
            int dis = p_r - p_l;
            double m_ = m;
            for (int j = 1; j <= l; ++j) {
                m_ = m_ * dis;
                int tmp = ((int) m_ % dis + dis) % dis;
                int pos = tmp + p_l;
                if (m_ == 0) {
                    m_ = 3 * m;
                }
                bitSet.set(pos, true);
                m_ = tmp + (m_ - (int) m_);
            }
        }
    }


    public boolean isInFilter(long input, double slop, double inter) {
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
            if (!bitSet.get(pos)) return false;
        }
        return true;
    }

//    public void getModel(long[] data) {
//        int len = data.length;
//        double meanX = (double) (LongStream.of(data).sum()) / len;
//        double meanY = (len - 1) / 2.0;
//
//        double numerator = 0;
//
//        for (int i = 0; i < len; i++) {
//            numerator += (data[i] - meanX) * (i - meanY);
//        }
//
//        slop = 0;
//        for (int i = 0; i < len; ++i) {
//            slop += numerator / ((data[i] - meanX) * (i - meanX));
//        }
//
//        this.inter = meanY - slop * meanX;
//    }


    public static void main(String[] args) {
        long[] dataset = new long[]{7066,7191,8459,8465,10488,11219,13030,14869,14895,15226,16807,18799,18890,21188,21815,22033,23686,25691,26616,27313,28832,29327,29714,30943,32220,32319,32430,32478,33629,35264,36755,37814,37975,38361,38627,39496,40282,41858,43170,44286,44438,44820,45105,45365,45512,45955,46113,46601,46603,48770,49178,49351,49407,49713,50382,51701,52032,52887,53417,53930,54874,54980,55007,55617,55838,56251,57612,61898,62266,64111,65242,65252,68327,72868,72900,75689,75706,76661,76682,77137,77516,77530,78491,79991,82615,82714,83444,84100,84235,84417,85935,87402,87449,88283,88664,89140,91226,92673,94601,94851,95242,95584,96689,97580,98011,99784,100289,101536,101659,105383,105706,108289,108423,109752,111084,112468,114523,114917,118172,118366,119692,120682,123151,125251,125547,125800,126000,126022,126266,126498,127862,128318,131026,131229,133076,133706,134446,135144,136370,136390,136403,136795,137485,139871,140098,140439,141055,141147,141554,142363,144510,145972,146065,148037,148479,149501,150261,151440,152438,153612,153743,154401,155173,155220,155807,157665,158056,160369,160958,162014,162235,162706,165260,166280,168342,168770,168821,170061,170390,170987,171248,171430,172240,174249,174953,175023,176122,176770,177907,178490,180180,181376,181991,183608,183907,184935,186567,188603,188938,189343,189458,189508,190618,190786,191301,193241,195195,196823,197064,200067,201954,202341,205258,205853,208859,209205,209517,210208,211430,215170,215684,216080,217124,219471,219984,220330,225849,227675,228842,229330,232050,233394,236523,238694,240093,242285,243333,243815,243834,245366,245935,249275,252711,252906,253287,255485,257617,260636,265854,266145,267041,268110,271059,272147};

//        long[] dataset = new long[] {1, 5, 7, 10, 11, 12, 15};
//        Arrays.sort(dataset);
        Parameter.setLMFPar(8);
        OptPLA optPLA = new OptPLA(dataset);
        Segment segment = optPLA.getSegments()[0];

        System.out.println("slop:" + segment.slop * rate + ", inter:" + (segment.slop * segment.inter * rate));

        LMFilter lmFilter = new LMFilter(segment);
//        lmFilter.printBitSet();
        System.out.println();
        for (int i = 7185; i < 7196; ++i) {
            boolean inFilter = lmFilter.isInFilter(i, segment.slop, segment.inter);
            System.out.println(i + ":" + inFilter);
        }
//
//        HashSet st = new HashSet();
//        for (long data : dataset) st.add(data);
//        long[] queryArr = Utils.buildRandArr(100, 0, 800, null);
//        for (long que : queryArr) {
//            boolean a = st.contains(que);
//            boolean b = lmFilter.isInFilter(que);
//            if (a != b) {
//                System.out.println("st is :" + a + " filter is:" + b);
//            }
//        }
    }
}
