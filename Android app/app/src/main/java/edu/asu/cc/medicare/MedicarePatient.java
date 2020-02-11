package edu.asu.cc.medicare;

import java.util.Date;
import java.util.Map;

public class MedicarePatient extends MedicareUser {
    String age;

    public MedicarePatient(String firstName, String lastName, String emailAddress) {
        super(firstName, lastName, emailAddress, AccountType.PATIENT);
    }

    public Map<String, String> getMap() {
        return super.getMap();
    }
}