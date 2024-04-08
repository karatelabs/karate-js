package io.karatelabs.js;

public class DemoPojo {

    public DemoPojo() {

    }

    public DemoPojo(String s) {
        this.stringValue = s;
    }

    public DemoPojo(String s, int i) {
        this.stringValue = s;
        this.intValue = i;
    }

    private boolean booleanValue;
    private String stringValue;
    private int intValue;
    private double doubleValue;

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String doWork() {
        return "hello";
    }

    public String doWork(String arg1) {
        return "hello" + arg1;
    }

    public String doWork(String arg1, boolean arg2) {
        return "hello" + arg1 + arg2;
    }

    public Object varArgs(Object[] args) {
        return args[args.length - 1];
    }

}
