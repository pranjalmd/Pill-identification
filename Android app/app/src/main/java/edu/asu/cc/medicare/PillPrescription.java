package edu.asu.cc.medicare;

import java.util.HashMap;
import java.util.Map;

public class PillPrescription {
    String proprietaryName;
    String ndc11Code;
    String morning;
    String noon;
    String evening;
    String bedtime;
    String refillDate;

    public PillPrescription(String proprietaryName, String ndc11Code, String morning, String noon, String evening, String bedtime, String refillDate) {
        this.proprietaryName = proprietaryName;
        this.ndc11Code = ndc11Code;
        this.morning = morning;
        this.noon = noon;
        this.evening = evening;
        this.bedtime = bedtime;
        this.refillDate = refillDate;
    }

    public Map<String, String> getMap() {
        Map<String, String> ret = new HashMap<>();
        ret.put("proprietaryName", proprietaryName);
        ret.put("ndc11Code", ndc11Code);
        ret.put("morning", morning);
        ret.put("noon", noon);
        ret.put("evening", evening);
        ret.put("bedtime", bedtime);
        ret.put("refillDate", refillDate);
        return ret;
    }
}
