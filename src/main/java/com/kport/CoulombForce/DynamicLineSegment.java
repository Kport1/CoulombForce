package com.kport.CoulombForce;

import java.util.Arrays;

import static com.kport.CoulombForce.Util.*;

public class DynamicLineSegment implements PhysicsObject, LineSegment{
    private final double[] p1;
    private final double[] p2;

    private final double[] prevP1;
    private final double[] prevP2;

    private final double[] p1Acc = {0, 0};
    private final double[] p2Acc = {0, 0};

    private double r;
    private final double m;

    private boolean p1Fixed = false;

    private double dist;

    public DynamicLineSegment(double[] p1_, double[] p2_, double r_, double m_) {
        p1 = p1_;
        p2 = p2_;
        r = r_;
        m = m_;

        prevP1 = p1.clone();
        prevP2 = p2.clone();

        dist = Util.len(Util.sub(p2, p1));
    }


    @Override
    public void update(double dt) {
        if(!p1Fixed) {
            double p1vx = p1[0] - prevP1[0];
            double p1vy = p1[1] - prevP1[1];

            prevP1[0] = p1[0];
            prevP1[1] = p1[1];

            p1[0] += p1vx + p1Acc[0] * dt * dt;
            p1[1] += p1vy + p1Acc[1] * dt * dt;

            p1Acc[0] = 0;
            p1Acc[1] = 0;
        }

        double p2vx = p2[0] - prevP2[0];
        double p2vy = p2[1] - prevP2[1];

        prevP2[0] = p2[0];
        prevP2[1] = p2[1];

        p2[0] += p2vx + p2Acc[0] * dt * dt;
        p2[1] += p2vy + p2Acc[1] * dt * dt;

        p2Acc[0] = 0;
        p2Acc[1] = 0;

        double distDiff = Util.len(Util.sub(p2, p1)) - dist;
        double[] norm = Util.norm(Util.sub(p2, p1));
        if(Double.isNaN(norm[0]) || Double.isNaN(norm[1])) norm = new double[]{0, 0};
        if(p1Fixed){
            p2[0] += norm[0] * distDiff * -1;
            p2[1] += norm[1] * distDiff * -1;
        }
        else {
            p1[0] += norm[0] * distDiff * 0.5;
            p1[1] += norm[1] * distDiff * 0.5;
            p2[0] += norm[0] * distDiff * -0.5;
            p2[1] += norm[1] * distDiff * -0.5;
        }

        p1[0] = Util.clamp(p1[0], -1, 1);
        p1[1] = Util.clamp(p1[1], -1, 1);
        p2[0] = Util.clamp(p2[0], -1, 1);
        p2[1] = Util.clamp(p2[1], -1, 1);
    }

    public void applyForce(double[] force, double[] pos){
        double h = Util.dot(Util.sub(pos, p1), Util.norm(Util.sub(p2, p1))) / Util.len(Util.sub(p2, p1));
        if(Double.isNaN(h)) h = 0;
        h = Util.clamp(h, 0, 1);
        p1Acc[0] += force[0] / m * (1 - h);
        p1Acc[1] += force[1] / m * (1 - h);
        p2Acc[0] += force[0] / m * h;
        p2Acc[1] += force[1] / m * h;
    }

    public void addPos(double[] p, double[] pos){
        double h = Util.dot(Util.sub(pos, p1), Util.norm(Util.sub(p2, p1))) / Util.len(Util.sub(p2, p1));
        if(Double.isNaN(h)) h = 0;
        h = Util.clamp(h, 0, 1);
        p1[0] += p[0] * (1 - h);
        p1[1] += p[1] * (1 - h);
        p2[0] += p[0] * h;
        p2[1] += p[1] * h;
    }

    public void setP1Fixed(boolean fixed){
        p1Fixed = fixed;
    }

    @Override
    public double[] getNormal(double[] p){
        double[] dp = sub(p2, p1);
        double d2 = dp[0] * dp[0] + dp[1] * dp[1];
        double h = dot(sub(p, p1), sub(p2, p1)) / d2;
        if(Double.isNaN(h)) h = 0;
        h = clamp(h, 0, 1);
        return sub(p, add(p1, mul(sub(p2, p1), h)));
    }

    public double[] getP1Vel(){
        double vx = p1[0] - prevP1[0];
        double vy = p1[1] - prevP1[1];
        return new double[]{vx, vy};
    }

    public void setP1Vel(double[] vel){
        prevP1[0] = p1[0] - vel[0];
        prevP1[1] = p1[1] - vel[1];
    }

    public double[] getP2Vel(){
        double vx = p2[0] - prevP2[0];
        double vy = p2[1] - prevP2[1];
        return new double[]{vx, vy};
    }

    public void setP2Vel(double[] vel){
        prevP2[0] = p2[0] - vel[0];
        prevP2[1] = p2[1] - vel[1];
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
    public void setP1(double x, double y) {
        p1[0] = x;
        p1[1] = y;
        dist = Util.len(Util.sub(p2, p1));
    }

    @Override
    public void setP2(double x, double y) {
        p2[0] = x;
        p2[1] = y;
        dist = Util.len(Util.sub(p2, p1));
    }

    @Override
    public void setP1(double[] p) {
        p1[0] = p[0];
        p1[1] = p[1];
        dist = Util.len(Util.sub(p2, p1));
    }

    @Override
    public void setP2(double[] p) {
        p2[0] = p[0];
        p2[1] = p[1];
        dist = Util.len(Util.sub(p2, p1));
    }

    @Override
    public void setR(double r_) {
        r = r_;
    }
}
