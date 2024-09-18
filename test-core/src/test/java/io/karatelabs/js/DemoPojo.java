package io.karatelabs.js;

import java.util.function.Function;

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
    private Integer[] integerArray;
    private int[] intArray;

    public final String instanceField = "instance-field";

    public static String staticField = "static-field";

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

    public Integer[] getIntegerArray() {
        return integerArray;
    }

    public void setIntegerArray(Integer[] integerArray) {
        this.integerArray = integerArray;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
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

    public Integer[] doIntegerArray() {
        return integerArray;
    }

    public Function<Integer, String> doWorkFn() {
        return x -> x + "";
    }

}
