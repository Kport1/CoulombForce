package com.kport.CoulombForce;

public interface LineSegment {
    double getRadius();

    double[] getP1();

    double[] getP2();

    void setP1(double x, double y);

    void setP2(double x, double y);

    void setP1(double[] p);

    void setP2(double[] p);

    public void addP1(double[] p);

    public void addP2(double[] p);

    void setR(double r_);

    double[] getNormal(double[] p);
}
