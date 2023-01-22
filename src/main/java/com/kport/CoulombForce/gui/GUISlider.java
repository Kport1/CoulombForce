package com.kport.CoulombForce.gui;

import com.kport.CoulombForce.GLFWWindowManager;
import com.kport.CoulombForce.Renderer;
import com.kport.CoulombForce.Shader;
import com.kport.CoulombForce.Util;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.lwjgl.opengl.GL41C.*;

public class GUISlider implements GUIElement{
    private static Shader shader;

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

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * 4, 0);
        glEnableVertexAttribArray(0);

        glBufferData(GL_ARRAY_BUFFER, Renderer.quadVertices, GL_STATIC_DRAW);
        /*//pos1
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
        glEnableVertexAttribArray(3);*/

        if(shader == null) {
            try {
                shader = new Shader(Files.readString(Path.of("./shaders/GUISliderFSH.glsl")),
                                    Files.readString(Path.of("./shaders/GUISliderVSH.glsl")));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void render() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //float[] data = {(float)pos1[0], (float)pos1[1], (float)pos2[0], (float)pos2[1], radius, (float)value};
        //glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
        shader.use();
        int windowSizeLocation = shader.getUniformLocation("windowSize");
        glUniform2iv(windowSizeLocation, windowManager.getWindowSize());

        int pos1Location = shader.getUniformLocation("pos1");
        int pos2Location = shader.getUniformLocation("pos2");
        int radiusLocation = shader.getUniformLocation("radius");
        int valueLocation = shader.getUniformLocation("value");

        glUniform2f(pos1Location, (float)pos1[0], (float)pos1[1]);
        glUniform2f(pos2Location, (float)pos2[0], (float)pos2[1]);
        glUniform1f(radiusLocation, radius);
        glUniform1f(valueLocation, (float)value);

        glDrawArrays(GL_TRIANGLES, 0, 6);
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
