package com.kport.CoulombForce;

import static com.kport.CoulombForce.Util.*;

public class StaticLineSegment implements PhysicsObject, LineSegment{
    private final double[] p1;
    private final double[] p2;
    private double r;

    public StaticLineSegment(double[] p1_, double[] p2_, double r_){
        p1 = p1_;
        p2 = p2_;
        r = r_;
    }

    @Override
    public void update(double dt) {}

    @Override
    public double[] getNormal(double[] p){
        double[] dp = sub(p2, p1);
        double d2 = dp[0] * dp[0] + dp[1] * dp[1];
        double h = dot(sub(p, p1), sub(p2, p1)) / d2;
        if(Double.isNaN(h)) h = 0;
        h = clamp(h, 0, 1);
        return sub(p, add(p1, mul(sub(p2, p1), h)));
    }

    @Override
    public double getRadius() {
        return r;
    }

    @Override
    public double[] getP1() {
        return p1;
    }

    @Override
    public double[] getP2() {
        return p2;
    }

    @Override
    public void setP1(double x, double y){
        p1[0] = x;
        p1[1] = y;
    }

    @Override
    public void setP2(double x, double y){
        p2[0] = x;
        p2[1] = y;
    }

    @Override
    public void setP1(double[] p){
        p1[0] = p[0];
        p1[1] = p[1];
    }

    @Override
    public void setP2(double[] p){
        p2[0] = p[0];
        p2[1] = p[1];
    }

    @Override
    public void addP1(double[] p){
        p1[0] += p[0];
        p1[1] += p[1];
    }

    @Override
    public void addP2(double[] p){
        p2[0] += p[0];
        p2[1] += p[1];
    }

    @Override
    public void setR(double r_){
        r = r_;
    }
}
