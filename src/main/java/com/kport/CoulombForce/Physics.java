package com.kport.CoulombForce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Physics {

    public static final List<PhysicsObject> objects = new ArrayList<>();
    public static int subSteps = 32;
    public static final double COULOMB_STRENGTH = 5;
    public static double dragStrength = 0;
    private static double temperature = 0;
    private static double restitution = 0.7;

    public static void update(double dt){
        double subDt = dt / subSteps;
        for (int i = 0; i < subSteps; i++) {
            applyCoulombForce();
            applyRandomForce();
            applyDragForce(subDt);
            //applyGravity();

            //circleCollisionElastic();
            circleCollision();

            circleStaticLineSegmentCollision();
            circleDynamicLineSegmentCollision();

            dynamicStaticLineSegmentCollision();

            updateObjects(subDt);
        }
    }

    private static void applyCoulombForce(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof Particle particle)
            for (PhysicsObject object2 : objects) {
                if(object1 == object2) continue;
                if(object2 instanceof Particle other && particle.getCharge() != 0) {
                    double dx = particle.getPos()[0] - other.getPos()[0];
                    double dy = particle.getPos()[1] - other.getPos()[1];
                    double r = Math.sqrt(dx * dx + dy * dy);
                    //if(r <= (particle.getRadius() + other.getRadius()) * 0.99) continue;

                    double mag = particle.getCharge() * other.getCharge() / (r * r) * COULOMB_STRENGTH;
                    double[] force = new double[]{dx / r * mag, dy / r * mag};
                    particle.applyForce(force);
                }
            }
        }
    }

    private static final Random rnd = new Random();
    private static void applyRandomForce(){
        for (PhysicsObject object : objects) {
            if(object instanceof Particle particle) {
                double rndX = (rnd.nextDouble() * 2 - 1) * temperature;
                double rndY = (rnd.nextDouble() * 2 - 1) * temperature;
                particle.applyForce(new double[]{rndX, rndY});
            }
        }
    }

    private static void applyDragForce(double dt){
        for(PhysicsObject object : objects){
            if(object instanceof Particle particle){
                double drag = Util.len(particle.getVel()) / dt;
                drag *= drag * -dragStrength;
                if(drag == 0) continue;
                double[] force = Util.mul(Util.norm(particle.getVel()), drag);
                particle.applyForce(force);
            }
            if(object instanceof DynamicLineSegment ls){
                double[] p1Vel = ls.getP1Vel();
                double p1Drag = Util.len(p1Vel) / dt;
                if(p1Drag != 0) {
                    p1Drag *= p1Drag * -dragStrength;
                    double[] p1Force = Util.mul(Util.norm(p1Vel), p1Drag);
                    ls.applyForce(p1Force, ls.getP1());
                }

                double[] p2Vel = ls.getP2Vel();
                double p2Drag = Util.len(p2Vel) / dt;
                if(p2Drag != 0) {
                    p2Drag *= p2Drag * -dragStrength;
                    double[] p2Force = Util.mul(Util.norm(p2Vel), p2Drag);
                    ls.applyForce(p2Force, ls.getP2());
                }
            }
        }
    }

    private static void applyGravity(){
        for (PhysicsObject object : objects) {
            if(object instanceof Particle p){
                p.applyForce(new double[]{0, 0});
            }
        }
    }

    private static void circleCollision(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof Particle particle)
                for (PhysicsObject object2 : objects) {
                    if(object1 == object2) continue;
                    if(object2 instanceof Particle other) {

                        double[] dp = Util.sub(particle.getPos(), other.getPos());
                        double r = Util.len(dp);
                        double penDepth = particle.getRadius() + other.getRadius() - r;

                        if (penDepth > 0) {
                            double[] nd = Util.div(dp, r);
                            particle.addPos(Util.mul(nd, 0.5 * penDepth));
                            other.addPos(Util.mul(nd, -0.5 * penDepth));
                        }
                    }
                }
        }
    }

    private static void circleCollisionElastic(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof Particle particle)
            for (PhysicsObject object2 : objects) {
                if(object1 == object2) continue;
                if(object2 instanceof Particle other) {

                    double[] dp = Util.sub(particle.getPos(), other.getPos());
                    double r = Util.len(dp);
                    double penDepth = particle.getRadius() + other.getRadius() - r;

                    if (penDepth > 0) {
                        double[] rv = Util.sub(particle.getVel(), other.getVel());
                        double[] normal = Util.norm(dp);
                        double velAlongNormal = Util.dot(rv, normal);
                        if(velAlongNormal > 0) continue;

                        double impulseS = (-(1 + restitution) * velAlongNormal) / (1 / particle.getMass() + 1 / other.getMass());
                        double[] impulse = Util.mul(normal, impulseS);
                        particle.addImpulse(impulse);
                        other.addImpulse(Util.mul(impulse, -1));

                        double[] correction = Util.mul(normal, penDepth / (1 / particle.getMass() + 1 / other.getMass()) * 0.2);
                        particle.addPos(Util.div(correction, particle.getMass()));
                        other.addPos(Util.div(Util.mul(correction, -1), other.getMass()));
                    }
                }
            }
        }
    }

    private static void circleStaticLineSegmentCollision(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof Particle p)
            for(PhysicsObject object2 : objects){
                if(object2 instanceof StaticLineSegment ls){
                    double[] normal = ls.getNormal(p.getPos());
                    double dist = Util.len(normal) - p.getRadius() - ls.getRadius();
                    if(dist < 0){
                        p.addPos(Util.mul(Util.norm(normal), dist * -(1 + restitution)));
                    }
                }
            }
        }
    }

    private static void circleDynamicLineSegmentCollision(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof Particle p)
                for(PhysicsObject object2 : objects){
                    if(object2 instanceof DynamicLineSegment ls){
                        double[] normal = ls.getNormal(p.getPos());
                        double dist = Util.len(normal) - p.getRadius() - ls.getRadius();
                        if(dist < 0){
                            p.addPos(Util.mul(Util.norm(normal), dist * -0.5));
                            ls.addPos(Util.mul(Util.norm(normal), dist * 0.5), p.getPos());
                        }
                    }
                }
        }
    }

    private static void dynamicStaticLineSegmentCollision(){
        for (PhysicsObject object1 : objects) {
            if(object1 instanceof StaticLineSegment sls)
                for(PhysicsObject object2 : objects){
                    if(object2 instanceof DynamicLineSegment dls){
                        double[] normal1 = sls.getNormal(dls.getP1());
                        double dist1 = Util.len(normal1) - dls.getRadius() - sls.getRadius();
                        if(dist1 < 0){
                            dls.addP1(Util.mul(Util.norm(normal1), dist1 * -(1 + restitution)));
                        }
                        double[] normal2 = sls.getNormal(dls.getP2());
                        double dist2 = Util.len(normal2) - dls.getRadius() - sls.getRadius();
                        if(dist2 < 0){
                            dls.addP2(Util.mul(Util.norm(normal2), dist2 * -(1 + restitution)));
                        }

                        double[] normal3 = dls.getNormal(sls.getP1());
                        double dist3 = Util.len(normal3) - dls.getRadius() - sls.getRadius();
                        if(dist3 < 0){
                            dls.addPos(Util.mul(Util.norm(normal3), dist3 * (1 + restitution)), sls.getP1());
                        }
                        double[] normal4 = dls.getNormal(sls.getP2());
                        double dist4 = Util.len(normal4) - dls.getRadius() - sls.getRadius();
                        if(dist4 < 0){
                            dls.addPos(Util.mul(Util.norm(normal4), dist4 * (1 + restitution)), sls.getP2());
                        }
                    }
                }
        }
    }

    private static void updateObjects(double dt){
        for (PhysicsObject object : objects) {
            object.update(dt);
        }
    }

    public static PhysicsObject addPhysicsObject(PhysicsObject object){
        objects.add(object);
        return object;
    }

    public static void clearPhysicsObjects(){
        objects.clear();
    }

    public static double[] getElectricFieldAt(double[] pos){
        double[] field = {0, 0};
        for (PhysicsObject object : objects) {
            if(object instanceof Particle p){
                double[] dp = Util.sub(p.getPos(), pos);
                double r = Util.len(dp);

                double mag = p.getCharge() / (r * r) * COULOMB_STRENGTH;
                double[] force = Util.mul(Util.norm(dp), mag);
                field = Util.sub(field, force);
            }
        }
        return field;
    }

    public static void setTemperature(double temperature) {
        Physics.temperature = temperature;
    }

    public static void setDragStrength(double dragStrength){
        Physics.dragStrength = dragStrength;
    }
}
