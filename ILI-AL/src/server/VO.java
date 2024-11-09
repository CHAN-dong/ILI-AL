package server;

import it.unisa.dia.gas.jpbc.Element;
import utils.BLS;
import utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VO {
    public List<Long> res = new ArrayList<>();
    public List<Element> sigList = new ArrayList<>();
    public List<long[]> boundList = new ArrayList<>();
    public Element aggSig;
    public long[] tarIds;

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("VO--res:\r\n");
        str.append(res.toString());
        str.append("\r\nVO--boundList:\r\n");
        str.append("[");
        for (int i = 0; i < boundList.size(); ++i) {
            str.append(Arrays.toString(boundList.get(i)));
            if (i != boundList.size() - 1) str.append(",");
            else str.append("]");
        }
//        str.append("\r\nVO--sig:\r\n");
//        if (aggSig != null) {
//            str.append("\r\n");
//            str.append(aggSig.toString());
//        }
        str.append("\r\nVO--tarIds:\r\n");
        if (tarIds != null) {
            str.append(Arrays.toString(tarIds));
        }
        return str.toString();
    }

    public void setTarIds(long[] tarIds) {
        this.tarIds = tarIds;
    }

    public void aggregateSig() {
        aggSig = BLS.aggregate(sigList);
    }

    public long getVoSize() throws UnsupportedEncodingException {
        StringBuilder str = new StringBuilder();
        for (long[] bound : boundList) str.append(Arrays.toString(bound));
        if (aggSig != null) str.append(aggSig.toString());
        return Utils.getStrSize(str.toString());
    }
}
