package com.kport.CoulombForce;

public class Util {

    public static double clamp(double x, double min, double max){
        return Math.min(Math.max(x, min), max);
    }

    public static double dot(double[] a, double[] b){
        return a[0] * b[0] + a[1] * b[1];
    }

    public static double[] add(double[] a, double[] b){
        return new double[]{a[0] + b[0], a[1] + b[1]};
    }

    public static double[] sub(double[] a, double[] b){
        return new double[]{a[0] - b[0], a[1] - b[1]};
    }

    public static double[] mul(double[] a, double b){
        return new double[]{a[0] * b, a[1] * b};
    }

    public static double[] div(double[] a, double b){
        return new double[]{a[0] / b, a[1] / b};
    }

    public static double len(double[] a){
        return Math.sqrt(a[0] * a[0] + a[1] * a[1]);
    }

    public static double[] norm(double[] a){
        return div(a, len(a));
    }
}
