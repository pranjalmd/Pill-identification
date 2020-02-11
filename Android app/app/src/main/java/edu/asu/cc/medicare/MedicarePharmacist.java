package edu.asu.cc.medicare;

import java.util.Map;

public class MedicarePharmacist extends MedicareUser {

    public MedicarePharmacist(String firstName, String lastName, String emailAddress) {
        super(firstName, lastName, emailAddress, AccountType.PHARMACIST);
    }

    public Map<String, String> getMap() {
        return super.getMap();
    }
}
