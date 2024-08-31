package com.kport.CoulombForce;

import com.kport.CoulombForce.gui.GUISlider;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.lwjgl.opengl.GL40.*;

public class Main {
    public static GLFWWindowManager windowManager;
    private static final double FIELD_VIS_DENSITY = 0.1;
    private static final double FIELD_VIS_STRENGTH = 0.0002;

    private static final LineSegment mouseVector = new StaticLineSegment(new double[]{0, 0}, new double[]{0, 0}, 0.008);

    static double fieldVisualizerLen = 0.01;

    private static final GUISlider tempSlider = new GUISlider(new double[]{-0.980, 0.980}, new double[]{-0.2, 0.980}, 0.01f, 0);
    private static final GUISlider simSpeedSlider = new GUISlider(new double[]{-0.980, 0.900}, new double[]{-0.2, 0.900}, 0.01f, 0.8);
    private static final GUISlider dragStrengthSlider = new GUISlider(new double[]{-0.980, 0.820}, new double[]{-0.2, 0.820}, 0.01f, 0.05);

    private static double simSpeed = 1;

    public static void main(String[] args) {
        AtomicInteger frame = new AtomicInteger();
        AtomicLong prevFrameTime = new AtomicLong();
        prevFrameTime.set(System.nanoTime());

        windowManager = new GLFWWindowManager(800, 800, "Coulomb-Force",
            (window) ->
            {
                Renderer.init(windowManager);

                Renderer.addArrow(mouseVector);
                Renderer.addGUIElement(tempSlider);
                Renderer.addGUIElement(simSpeedSlider);
                Renderer.addGUIElement(dragStrengthSlider);

            },


            (window) ->
            {
                glClearColor(0, 0, 0, 1);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                double dt = (System.nanoTime() - prevFrameTime.get()) / 1_000_000_000d;
                prevFrameTime.set(System.nanoTime());

                double prevSimSpeed = simSpeed;
                simSpeed = Math.pow(2, (simSpeedSlider.getValue() - 0.8) * 10);
                simSpeed = simSpeedSlider.getValue() == 0? 0.00001 : simSpeed;

                double simSpeedRatio = simSpeed / prevSimSpeed;
                for (PhysicsObject object : Physics.objects) {
                    if(object instanceof Particle p){
                        double[] v = p.getVel();
                        p.setVel(Util.mul(v, simSpeedRatio));
                    }
                    else if(object instanceof DynamicLineSegment ls){
                        double[] v1 = ls.getP1Vel();
                        double[] v2 = ls.getP2Vel();
                        ls.setP1Vel(Util.mul(v1, simSpeedRatio));
                        ls.setP2Vel(Util.mul(v2, simSpeedRatio));
                    }
                }

                Physics.setDragStrength(dragStrengthSlider.getValue() * 100);

                Physics.update(dt * simSpeed);

                mouseVector.setP1(windowManager.getNormalizedCursorPos());
                mouseVector.setP2(Util.add(windowManager.getNormalizedCursorPos(), Util.mul(Physics.getElectricFieldAt(windowManager.getNormalizedCursorPos()), fieldVisualizerLen)));

                ArrayList<StaticLineSegment> fieldArrows = new ArrayList<>();
                for(float x = -1; x < 1; x += FIELD_VIS_DENSITY){
                    for(float y = -1; y < 1; y += FIELD_VIS_DENSITY){
                        double[] p = {x, y};
                        double[] fieldVec = Util.mul(Physics.getElectricFieldAt(p), FIELD_VIS_STRENGTH);
                        if(Util.len(fieldVec) > 1.414 * FIELD_VIS_DENSITY){
                            fieldVec[0] = 0;
                            fieldVec[1] = 0;
                        }
                        StaticLineSegment arrow = new StaticLineSegment(p, Util.add(p, fieldVec), 0.005);
                        fieldArrows.add(arrow);
                        Renderer.addArrow(arrow);
                    }
                }

                Renderer.renderObjects();

                for (StaticLineSegment arrow : fieldArrows) {
                    Renderer.removeArrow(arrow);
                }

                Physics.setTemperature(tempSlider.getValue() * 2000);

                frame.getAndIncrement();
            });

        final double[][] coordPress = {new double[2]};
        final double[] lineThickness = {0.02};
        final LineSegment[] ls = new LineSegment[1];

        windowManager.addKeyCallback((window, key, scancode, action, mods) -> {
            if(action != GLFW.GLFW_PRESS) return;
            double[] coords = windowManager.getNormalizedCursorPos();
            switch (key){
                case GLFW.GLFW_KEY_C -> {
                    Physics.clearPhysicsObjects();
                }

                case GLFW.GLFW_KEY_Q -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, 1, 0.05));
                }

                case GLFW.GLFW_KEY_W -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, -1, 0.05));
                }

                case GLFW.GLFW_KEY_A -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, 0.5, 0.03));
                }

                case GLFW.GLFW_KEY_S -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, -0.5, 0.03));
                }

                case GLFW.GLFW_KEY_Z -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, 0.25, 0.02));
                }

                case GLFW.GLFW_KEY_X -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, -0.25, 0.02));
                }

                case GLFW.GLFW_KEY_E -> {
                    Physics.addPhysicsObject(new Particle(coords, 1, 0, 0.02));
                }

                case GLFW.GLFW_KEY_UP -> {
                    //simSpeed *= 2;
                    for (PhysicsObject object : Physics.objects) {
                        if(object instanceof Particle p){
                            double[] v = p.getVel();
                            p.setVel(Util.mul(v, 2));
                        }
                    }
                }

                case GLFW.GLFW_KEY_DOWN -> {
                    //simSpeed *= 0.5;
                    for (PhysicsObject object : Physics.objects) {
                        if(object instanceof Particle p){
                            double[] v = p.getVel();
                            p.setVel(Util.mul(v, 0.5));
                        }
                    }
                }

                case GLFW.GLFW_KEY_L -> {
                    if(!(ls[0] instanceof DynamicLineSegment dls)) return;

                    coordPress[0] = windowManager.getNormalizedCursorPos();

                    for (PhysicsObject object : Physics.objects) {
                        if(object instanceof DynamicLineSegment dls2){
                            if(Util.len(Util.sub(dls2.getP1(), coordPress[0])) < dls2.getRadius() && dls2.getP1Link() == null){
                                dls2.setP1Link(dls);
                            }
                        }
                    }

                    dls.setP1Vel(new double[]{0, 0});
                    dls.setP2Vel(new double[]{0, 0});
                    Physics.addPhysicsObject(dls);
                    Renderer.removeLineSegment(dls);
                    //lineThickness[0] = 0.02;
                    ls[0] = new DynamicLineSegment(coordPress[0], coordPress[0].clone(), lineThickness[0], 1);
                    Renderer.addLineSegment(ls[0]);
                    dls.setP2Link((DynamicLineSegment) ls[0]);
                }

                case GLFW.GLFW_KEY_F -> {
                    if(!(ls[0] instanceof DynamicLineSegment dls)) return;
                    dls.setP1Fixed(true);
                }

                case GLFW.GLFW_KEY_G -> {
                    if(!(ls[0] instanceof DynamicLineSegment dls)) return;
                    dls.setP2Fixed(true);
                }
            }
        });

        windowManager.addMouseCallback((window, button, action, i) -> {
            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT){
                if(action == GLFW.GLFW_PRESS) {
                    coordPress[0] = windowManager.getNormalizedCursorPos();
                    lineThickness[0] = 0.02;
                    ls[0] = new StaticLineSegment(coordPress[0], coordPress[0].clone(), lineThickness[0]);
                    Renderer.addLineSegment(ls[0]);
                }
                if(action == GLFW.GLFW_RELEASE) {
                    Physics.addPhysicsObject((PhysicsObject) ls[0]);
                    Renderer.removeLineSegment(ls[0]);
                }
            }
            else if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT){
                if(action == GLFW.GLFW_PRESS){
                    coordPress[0] = windowManager.getNormalizedCursorPos();
                    lineThickness[0] = 0.02;
                    ls[0] = new DynamicLineSegment(coordPress[0], coordPress[0].clone(), lineThickness[0], 1);
                    Renderer.addLineSegment(ls[0]);
                }
                if(action == GLFW.GLFW_RELEASE) {
                    ((DynamicLineSegment)ls[0]).setP1Vel(new double[]{0, 0});
                    ((DynamicLineSegment)ls[0]).setP2Vel(new double[]{0, 0});
                    Physics.addPhysicsObject((PhysicsObject) ls[0]);
                    Renderer.removeLineSegment(ls[0]);
                }
            }
        });

        windowManager.addCursorPosCallback((window, x, y) -> {
            if(GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS)
            {
                ls[0].setP2(x, y);
            }
        });

        windowManager.addScrollCallback((window, d, dir) -> {
            if(GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS)
            {
                lineThickness[0] += dir * 0.005;
                lineThickness[0] = Util.clamp(lineThickness[0], 0.005, 0.5);
                ls[0].setR(lineThickness[0]);
            }
            else {
                fieldVisualizerLen *= dir == 1? 2 : 0.5;
            }
        });

        windowManager.start();
    }
}
