package edu.asu.cc.medicare;

import java.util.HashMap;
import java.util.Map;

public class MedicareUser {
    String firstName;
    String lastName ;
    String emailAddress;
    AccountType type;

    public MedicareUser(String firstName, String lastName, String emailAddress, AccountType type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.type = type;
    }

    public Map<String, String> getMap() {
        Map<String, String> ret = new HashMap<>();
        ret.put("firstName", firstName);
        ret.put("lastName", lastName);
        ret.put("emailAddress", emailAddress);
        ret.put("type", type.toString());
        return ret;
    }
}
