package com.kport.CoulombForce.gui;

import com.kport.CoulombForce.GLFWWindowManager;
import com.kport.CoulombForce.Util;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.lwjgl.opengl.GL41C.*;

public class GUISlider implements GUIElement{
    private static int shader;

    private double[] pos1;
    private double[] pos2;
    private float radius;

    private double value;

    private boolean sliding = false;

    private int vao;
    private int vbo;

    private GLFWWindowManager windowManager;

    public GUISlider(double[] pos1_, double[] pos2_, float radius_, double value_){
        pos1 = pos1_;
        pos2 = pos2_;
        radius = radius_;

        value = value_;
    }

    @Override
    public void init(GLFWWindowManager windowManager) {
        this.windowManager = windowManager;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        //pos1
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        //pos2
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        //radius
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * 4, 4 * 4);
        glEnableVertexAttribArray(2);
        //value
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 6 * 4, 5 * 4);
        glEnableVertexAttribArray(3);

        if(shader == 0){
            int fsh = glCreateShader(GL_FRAGMENT_SHADER);
            int vsh = glCreateShader(GL_VERTEX_SHADER);
            try {
                glShaderSource(fsh, Files.readString(Path.of("./shaders/GUISliderFSH.glsl")));
                glShaderSource(vsh, Files.readString(Path.of("./shaders/GUISliderVSH.glsl")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            glCompileShader(fsh);
            glCompileShader(vsh);
            int[] status = new int[1];
            glGetShaderiv(fsh, GL_COMPILE_STATUS, status);
            if(status[0] == 0)
                throw new Error(glGetShaderInfoLog(fsh));
            glGetShaderiv(vsh, GL_COMPILE_STATUS, status);
            if(status[0] == 0)
                throw new Error(glGetShaderInfoLog(vsh));

            shader = glCreateProgram();
            glAttachShader(shader, fsh);
            glAttachShader(shader, vsh);
            glLinkProgram(shader);
            glDeleteShader(fsh);
            glDeleteShader(vsh);
            glGetProgramiv(shader, GL_LINK_STATUS, status);
            if(status[0] == 0){
                throw new Error(glGetProgramInfoLog(shader));
            }
        }
    }

    @Override
    public void render() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        float[] data = {(float)pos1[0], (float)pos1[1], (float)pos2[0], (float)pos2[1], radius, (float)value};
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
        glUseProgram(shader);
        int windowSizeLocation = glGetUniformLocation(shader, "windowSize");
        glUniform2iv(windowSizeLocation, windowManager.getWindowSize());
        glDrawArrays(GL_POINTS, 0, 1);
    }

    @Override
    public boolean handleMouseButtonEvent(long window, int button, int action, int i) {
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT){
            if(action == GLFW.GLFW_PRESS) {
                double[] mousePos = GLFWWindowManager.of(window).getNormalizedCursorPos();
                double[] sliderPos = Util.lerp(pos1, pos2, value);
                double dist = Util.len(Util.sub(mousePos, sliderPos));
                if (dist < radius * 2) {
                    sliding = true;
                    return true;
                }
            }
            else if(sliding){
                sliding = false;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean handleKeyEvent(long window, int key, int scancode, int action, int mods) {
        return false;
    }

    @Override
    public boolean handleCursorPosEvent(long window, double x, double y) {
        if(sliding){
            double h = Util.dot(Util.sub(new double[]{x, y}, pos1), Util.norm(Util.sub(pos2, pos1)));
            h /= Util.len(Util.sub(pos2, pos1));
            value = Util.clamp(h, 0, 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleScrollEvent(long window, double d, double dir) {
        return false;
    }

    public double getValue(){
        return value;
    }
}
