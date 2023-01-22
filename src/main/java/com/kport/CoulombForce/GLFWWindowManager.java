package com.kport.CoulombForce;

import com.kport.CoulombForce.gui.GUIElement;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41C;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GLFWWindowManager implements Closeable {

    private static Map<Long, GLFWWindowManager> windowManagerMap = new HashMap<>();

    private final Consumer<Long> loop;
    private final Consumer<Long> start;
    private final List<GLFWKeyCallbackI> keyCallbacks = new ArrayList<>();
    private final List<GLFWMouseButtonCallbackI> mouseButtonCallbacks = new ArrayList<>();
    private final List<GLFWCursorPosCallbackI> cursorPosCallbacks = new ArrayList<>();
    private final List<GLFWScrollCallbackI> scrollCallbacks = new ArrayList<>();

    private final long window;

    public static GLFWWindowManager of(long window){
        return windowManagerMap.get(window);
    }

    public GLFWWindowManager(int w, int h, String title, Consumer<Long> start_, Consumer<Long> loop_){
        GLFWErrorCallback.createPrint(System.err).set();
        if(!GLFW.glfwInit()) throw new IllegalStateException("Couldn't initialize GLFW");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(w, h, title, 0, 0);
        if(window == 0) throw new IllegalStateException("Couldn't create window");

        windowManagerMap.put(window, this);

        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            for (GUIElement element : Renderer.guiElements) {
                if(element.handleKeyEvent(window, key, scancode, action, mods)) return;
            }

            if(key == GLFW.GLFW_KEY_ESCAPE){
                GLFW.glfwSetWindowShouldClose(window, true);
            }
            for (GLFWKeyCallbackI keyCallback : keyCallbacks) {
                keyCallback.invoke(window, key, scancode, action, mods);
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, i) -> {
            for (GUIElement element : Renderer.guiElements) {
                if(element.handleMouseButtonEvent(window, button, action, i)) return;
            }

            for (GLFWMouseButtonCallbackI mouseButtonCallback : mouseButtonCallbacks) {
                mouseButtonCallback.invoke(window, button, action, i);
            }
        });

        GLFW.glfwSetCursorPosCallback(window, (window, x, y) -> {
            double[] coords = normalizeCoords(x, y);
            for (GUIElement element : Renderer.guiElements) {
                if(element.handleCursorPosEvent(window, coords[0], coords[1])) return;
            }

            for (GLFWCursorPosCallbackI cursorPosCallback : cursorPosCallbacks) {
                cursorPosCallback.invoke(window, coords[0], coords[1]);
            }
        });

        GLFW.glfwSetScrollCallback(window, (window, d, dir) -> {
            for (GUIElement element : Renderer.guiElements) {
                if(element.handleScrollEvent(window, d, dir)) return;
            }

            for (GLFWScrollCallbackI scrollCallback : scrollCallbacks) {
                scrollCallback.invoke(window, d, dir);
            }
        });

        GLFW.glfwSetWindowSizeCallback(window, (window, width, height) -> {
            GL41C.glViewport(0, 0, width, height);
        });

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0);

        start = start_;
        loop = loop_;
    }

    public void addKeyCallback(GLFWKeyCallbackI callback){
        keyCallbacks.add(callback);
    }

    public void addMouseCallback(GLFWMouseButtonCallbackI callback){
        mouseButtonCallbacks.add(callback);
    }

    public void addCursorPosCallback(GLFWCursorPosCallbackI callback){
        cursorPosCallbacks.add(callback);
    }

    public void addScrollCallback(GLFWScrollCallbackI callback){
        scrollCallbacks.add(callback);
    }

    public double[] normalizeCoords(double x, double y){
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(window, width, height);
        x /= width[0] * 0.5;
        x -= 1;
        y /= height[0] * -0.5;
        y += 1;
        return new double[]{x, y};
    }

    public double[] getNormalizedCursorPos(){
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        return normalizeCoords(x[0], y[0]);
    }

    public int[] getWindowSize(){
        int[] wx = new int[1];
        int[] wy = new int[1];
        GLFW.glfwGetWindowSize(window, wx, wy);
        return new int[]{wx[0], wy[0]};
    }

    public void start(){
        GL.createCapabilities();
        start.accept(window);
        while(!GLFW.glfwWindowShouldClose(window)){
            loop.accept(window);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        close();
    }

    @Override
    public void close() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }
}
