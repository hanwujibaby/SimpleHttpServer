package com.wayleynam.utils;

public class ClassA {

    private ClassB b;

    public ClassA() {
        this.b = new ClassB(this);
    }


    public static void main(String[] args) {
        ClassA a=new ClassA();
        System.out.println("done");
    }
}
