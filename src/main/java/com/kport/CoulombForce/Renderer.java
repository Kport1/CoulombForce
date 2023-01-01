package com.kport.CoulombForce;

import com.kport.CoulombForce.gui.GUIElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    private static int arrowShader;

    private static List<LineSegment> additionalLineSegments = new ArrayList<>();
    private static List<LineSegment> arrows = new ArrayList<>();


    //private static NkContext nkContext;

    public static List<GUIElement> guiElements = new ArrayList<>();

    private static GLFWWindowManager windowManager;

    public static void init(GLFWWindowManager windowManager_){
        windowManager = windowManager_;

        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_POINT_SPRITE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

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
        //glDeleteShader(lineVSH);
        glGetProgramiv(lineShader, GL_LINK_STATUS, status);
        if(status[0] == 0){
            throw new Error(glGetProgramInfoLog(lineShader));
        }

        int arrowFSH = glCreateShader(GL_FRAGMENT_SHADER);
        try {
            glShaderSource(arrowFSH, Files.readString(Path.of("./shaders/arrowFSH.glsl")));
        } catch (IOException e){
            e.printStackTrace();
        }
        glCompileShader(arrowFSH);
        glGetShaderiv(arrowFSH, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(arrowFSH));

        arrowShader = glCreateProgram();
        glAttachShader(arrowShader, arrowFSH);
        glAttachShader(arrowShader, lineVSH);
        glLinkProgram(arrowShader);
        glDeleteShader(arrowFSH);
        glDeleteShader(lineVSH);
        glGetProgramiv(arrowShader, GL_LINK_STATUS, status);
        if(status[0] == 0){
            throw new Error(glGetProgramInfoLog(arrowShader));
        }


        /*nkContext = NkContext.create();
        Nuklear.nk_init(nkContext,
                NkAllocator.create().alloc((handle, old, size) -> MemoryUtil.nmemAllocChecked(size))
                        .mfree((handle, ptr) -> MemoryUtil.nmemFree(ptr)),
                null);*/

        windowManager.addMouseCallback((window, button, action, i) -> {
            for (GUIElement guiElement : guiElements) {
                guiElement.handleMouseButtonEvent(window, button, action, i);
            }
        });

        windowManager.addKeyCallback((window, key, scancode, action, mods) -> {
            for (GUIElement guiElement : guiElements) {
                guiElement.handleKeyEvent(window, key, scancode, action, mods);
            }
        });

        windowManager.addCursorPosCallback((window, x, y) -> {
            for (GUIElement guiElement : guiElements) {
                guiElement.handleCursorPosEvent(window, x, y);
            }
        });

        windowManager.addScrollCallback((window, d, dir) -> {
            for (GUIElement guiElement : guiElements) {
                guiElement.handleScrollEvent(window, d, dir);
            }
        });
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


        List<LineSegment> lineSegments = objects.stream().filter(o -> o instanceof LineSegment).map(o -> (LineSegment)o).collect(Collectors.toList());
        lineSegments.addAll(additionalLineSegments);
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


        float[] arrowVertexData = new float[arrows.size() * 5];
        for (int i = 0; i < arrows.size(); i++) {
            arrowVertexData[i * 5] = (float)arrows.get(i).getP1()[0];
            arrowVertexData[i * 5 + 1] = (float)arrows.get(i).getP1()[1];

            arrowVertexData[i * 5 + 2] = (float)arrows.get(i).getP2()[0];
            arrowVertexData[i * 5 + 3] = (float)arrows.get(i).getP2()[1];

            arrowVertexData[i * 5 + 4] = (float)arrows.get(i).getRadius();
        }

        glBindVertexArray(lineVertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, lineVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, arrowVertexData, GL_DYNAMIC_DRAW);
        glUseProgram(arrowShader);
        glDrawArrays(GL_POINTS, 0, arrows.size());


        for (GUIElement guiElement : guiElements) {
            guiElement.render();
        }
    }

    public static void addLineSegment(LineSegment lineSegment){
        additionalLineSegments.add(lineSegment);
    }

    public static void removeLineSegment(LineSegment lineSegment){
        additionalLineSegments.remove(lineSegment);
    }

    public static void addArrow(LineSegment lineSegment){
        arrows.add(lineSegment);
    }

    public static void removeArrow(LineSegment lineSegment){
        arrows.remove(lineSegment);
    }

    public static void addGUIElement(GUIElement element){
        guiElements.add(element);
        element.init(windowManager);
    }

    public static void removeGUIElement(GUIElement element){
        guiElements.remove(element);
    }
}
