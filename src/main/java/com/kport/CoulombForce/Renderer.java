package com.kport.CoulombForce;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL41.*;

public class Renderer {
    private static int particleVertexBuffer;
    private static int particleVertexArray;
    private static int particleShader;

    private static int lineVertexBuffer;
    private static int lineVertexArray;
    private static int lineShader;

    private static List<StaticLineSegment> additionalLineSegments = new ArrayList<>();
    private static double[] mouseVector = {0, 0};

    private static GLFWWindowManager windowManager;

    public static void init(GLFWWindowManager windowManager_){
        windowManager = windowManager_;

        particleVertexArray = glGenVertexArrays();
        glBindVertexArray(particleVertexArray);
        particleVertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, particleVertexBuffer);
        //Pos
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0);
        glEnableVertexAttribArray(0);
        //Radius
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 5 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        //Charge
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 5 * 4, 4 * 4);
        glEnableVertexAttribArray(2);

        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_POINT_SPRITE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

        int particleFSH = glCreateShader(GL_FRAGMENT_SHADER);
        int particleVSH = glCreateShader(GL_VERTEX_SHADER);
        try {
            glShaderSource(particleFSH, Files.readString(Path.of("./shaders/particleFSH.glsl")));
            glShaderSource(particleVSH, Files.readString(Path.of("./shaders/particleVSH.glsl")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        glCompileShader(particleFSH);
        glCompileShader(particleVSH);
        int[] status = new int[1];
        glGetShaderiv(particleFSH, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(particleFSH));
        glGetShaderiv(particleVSH, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(particleVSH));

        particleShader = glCreateProgram();
        glAttachShader(particleShader, particleFSH);
        glAttachShader(particleShader, particleVSH);
        glLinkProgram(particleShader);
        glDeleteShader(particleFSH);
        glDeleteShader(particleVSH);
        glGetProgramiv(particleShader, GL_LINK_STATUS, status);
        if(status[0] == 0){
            throw new Error(glGetProgramInfoLog(particleShader));
        }



        lineVertexArray = glGenVertexArrays();
        glBindVertexArray(lineVertexArray);
        lineVertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, lineVertexBuffer);
        //Pos1
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * 4, 0);
        glEnableVertexAttribArray(0);
        //Pos2
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        //Radius
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 5 * 4, 4 * 4);
        glEnableVertexAttribArray(2);

        int lineFSH = glCreateShader(GL_FRAGMENT_SHADER);
        int lineVSH = glCreateShader(GL_VERTEX_SHADER);
        try {
            glShaderSource(lineFSH, Files.readString(Path.of("./shaders/lineFSH.glsl")));
            glShaderSource(lineVSH, Files.readString(Path.of("./shaders/lineVSH.glsl")));
        } catch (IOException e){
            e.printStackTrace();
        }
        glCompileShader(lineFSH);
        glCompileShader(lineVSH);
        glGetShaderiv(lineFSH, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(lineFSH));
        glGetShaderiv(lineVSH, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(lineVSH));

        lineShader = glCreateProgram();
        glAttachShader(lineShader, lineFSH);
        glAttachShader(lineShader, lineVSH);
        glLinkProgram(lineShader);
        glDeleteShader(lineFSH);
        glDeleteShader(lineVSH);
        glGetProgramiv(lineShader, GL_LINK_STATUS, status);
        if(status[0] == 0){
            throw new Error(glGetProgramInfoLog(lineShader));
        }
    }

    public static void renderObjects(){
        List<PhysicsObject> objects = Physics.objects;

        List<Particle> particles = objects.stream().filter(o -> o instanceof Particle).map(o -> (Particle)o).collect(Collectors.toList());
        float[] particleVertexData = new float[particles.size() * 5];
        for (int i = 0; i < particles.size(); i++) {
            particleVertexData[i * 5] =     (float)particles.get(i).getPos()[0];
            particleVertexData[i * 5 + 1] = (float)particles.get(i).getPos()[1];
            particleVertexData[i * 5 + 2] = 0;
            particleVertexData[i * 5 + 3] = (float)particles.get(i).getRadius();
            particleVertexData[i * 5 + 4] = (float)particles.get(i).getCharge();
        }

        glBindVertexArray(particleVertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, particleVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, particleVertexData, GL_DYNAMIC_DRAW);
        glUseProgram(particleShader);
        glDrawArrays(GL_POINTS, 0, particles.size());


        List<StaticLineSegment> lineSegments = objects.stream().filter(o -> o instanceof StaticLineSegment).map(o -> (StaticLineSegment)o).collect(Collectors.toList());
        lineSegments.addAll(additionalLineSegments);
        if(Util.len(mouseVector) > Double.MIN_VALUE * 128)
        lineSegments.add(new StaticLineSegment(windowManager.getNormalizedCursorPos(), Util.add(windowManager.getNormalizedCursorPos(), mouseVector), 0.005));
        float[] lineSegmentVertexData = new float[lineSegments.size() * 5];
        for (int i = 0; i < lineSegments.size(); i++) {
            lineSegmentVertexData[i * 5] = (float)lineSegments.get(i).getP1()[0];
            lineSegmentVertexData[i * 5 + 1] = (float)lineSegments.get(i).getP1()[1];

            lineSegmentVertexData[i * 5 + 2] = (float)lineSegments.get(i).getP2()[0];
            lineSegmentVertexData[i * 5 + 3] = (float)lineSegments.get(i).getP2()[1];

            lineSegmentVertexData[i * 5 + 4] = (float)lineSegments.get(i).getRadius();
        }

        glBindVertexArray(lineVertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, lineVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, lineSegmentVertexData, GL_DYNAMIC_DRAW);
        glUseProgram(lineShader);
        glDrawArrays(GL_POINTS, 0, lineSegments.size());
    }

    public static void addLineSegment(StaticLineSegment lineSegment){
        additionalLineSegments.add(lineSegment);
    }

    public static void removeLineSegment(StaticLineSegment lineSegment){
        additionalLineSegments.remove(lineSegment);
    }

    public static void setMouseVector(double[] vec){
        mouseVector = vec;
    }
}
