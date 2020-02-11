package edu.asu.cc.medicare;

public enum AccountType {
    PATIENT("patient", 0),
    PHARMACIST("pharmacist", 1);

    private String stringValue;
    private int intValue;
    AccountType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public static AccountType fromString(String text) {
        for (AccountType it : AccountType.values()) {
            if (it.stringValue.equalsIgnoreCase(text)) {
                return it;
            }
        }
        return null;
    }

    public int getIntValue() { return intValue; }

}
