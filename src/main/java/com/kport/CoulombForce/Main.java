package com.kport.CoulombForce;

import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.lwjgl.opengl.GL40.*;

public class Main {
    public static GLFWWindowManager windowManager;
    public static void main(String[] args) {
        AtomicInteger frame = new AtomicInteger();
        AtomicLong prevFrameTime = new AtomicLong();
        prevFrameTime.set(System.nanoTime());

        double[] fieldVisualizerLen = {0.01};
        double[] simSpeed = {1};
        windowManager = new GLFWWindowManager(800, 800, "Coulomb-Force",
            (window) ->
            {
                Renderer.init(windowManager);
            },


            (window) ->
            {
                glClearColor(0, 0, 0, 1);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                double dt = (System.nanoTime() - prevFrameTime.get()) / 1_000_000_000d;
                prevFrameTime.set(System.nanoTime());
                Physics.update(dt * simSpeed[0]);

                Renderer.setMouseVector(Util.mul(Physics.getElectricFieldAt(windowManager.getNormalizedCursorPos()), -fieldVisualizerLen[0]));
                Renderer.renderObjects();


                frame.getAndIncrement();
            });

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

                case GLFW.GLFW_KEY_RIGHT -> {
                    Physics.temperature += 50;
                    System.out.println(Physics.temperature);
                }

                case GLFW.GLFW_KEY_LEFT -> {
                    Physics.temperature -= 50;
                    System.out.println(Physics.temperature);
                }

                case GLFW.GLFW_KEY_UP -> {
                    simSpeed[0] *= 2;
                    for (PhysicsObject object : Physics.objects) {
                        if(object instanceof Particle p){
                            double[] v = p.getVel();
                            p.setVel(Util.mul(v, 2));
                        }
                    }
                }

                case GLFW.GLFW_KEY_DOWN -> {
                    simSpeed[0] *= 0.5;
                    for (PhysicsObject object : Physics.objects) {
                        if(object instanceof Particle p){
                            double[] v = p.getVel();
                            p.setVel(Util.mul(v, 0.5));
                        }
                    }
                }
            }
        });

        final double[][] coordLeftPress = {new double[2]};
        final double[] lineThickness = {0.02};
        final StaticLineSegment[] ls = new StaticLineSegment[1];
        windowManager.addMouseCallback((window, button, action, i) -> {
            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT){
                if(action == GLFW.GLFW_PRESS) {
                    coordLeftPress[0] = windowManager.getNormalizedCursorPos();
                    lineThickness[0] = 0.02;
                    ls[0] = new StaticLineSegment(coordLeftPress[0], coordLeftPress[0].clone(), lineThickness[0]);
                    Renderer.addLineSegment(ls[0]);
                }
                if(action == GLFW.GLFW_RELEASE) {
                    Physics.addPhysicsObject(ls[0]);
                    Renderer.removeLineSegment(ls[0]);
                }
            }
        });

        windowManager.addCursorPosCallback((window, x, y) -> {
            if(GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                ls[0].setP2(x, y);
            }
        });

        windowManager.addScrollCallback((window, d, dir) -> {
            if(GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                lineThickness[0] += dir * 0.005;
                lineThickness[0] = Util.clamp(lineThickness[0], 0.005, 0.5);
                ls[0].setR(lineThickness[0]);
            }
            else {
                fieldVisualizerLen[0] += dir * 0.001;
            }
        });

        windowManager.start();
    }
}
