package com.kport.CoulombForce;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Particle implements PhysicsObject{
    private final double[] pos;
    private final double[] prev_pos;
    //private double[] vel = new double[]{0, 0};
    private final double[] acc = new double[]{0, 0};
    private final double mass;

    private final double charge;
    private final double radius;

    public Particle(double[] pos_, double mass_, double charge_, double radius_){
        pos = pos_;
        prev_pos = pos_.clone();
        mass = mass_;
        charge = charge_;
        radius = radius_;
    }

    @Override
    public void update(double dt){
        double vx = pos[0] - prev_pos[0];
        double vy = pos[1] - prev_pos[1];

        prev_pos[0] = pos[0];
        prev_pos[1] = pos[1];

        pos[0] += vx + acc[0] * dt * dt;
        pos[1] += vy + acc[1] * dt * dt;

        acc[0] = 0;
        acc[1] = 0;

        pos[0] = Util.clamp(pos[0], -0.95, 0.95);
        pos[1] = Util.clamp(pos[1], -0.95, 0.95);
    }

    public void applyForce(double[] force){
        acc[0] += force[0] / mass;
        acc[1] += force[1] / mass;
    }

    public double[] getPos(){
        return pos;
    }

    public void addPos(double[] p){
        pos[0] += p[0];
        pos[1] += p[1];
    }

    public double getCharge() {
        return charge;
    }

    public double getRadius() {
        return radius;
    }

    public double getMass(){
        return mass;
    }

    public double[] getVel(){
        double vx = pos[0] - prev_pos[0];
        double vy = pos[1] - prev_pos[1];
        return new double[]{vx, vy};
    }

    public void setVel(double[] vel){
        prev_pos[0] = pos[0] - vel[0];
        prev_pos[1] = pos[1] - vel[1];
    }

    public void addVel(double[] vel){
        prev_pos[0] -= vel[0];
        prev_pos[1] -= vel[1];
    }

    public double[] getImpulse(){
        double vx = pos[0] - prev_pos[0];
        double vy = pos[1] - prev_pos[1];
        return new double[]{vx * mass, vy * mass};
    }

    public void setImpulse(double[] impulse){
        setVel(new double[]{impulse[0] / mass, impulse[1] / mass});
    }

    public void addImpulse(double[] impulse){
        addVel(new double[]{impulse[0] / mass, impulse[1] / mass});
    }
}
