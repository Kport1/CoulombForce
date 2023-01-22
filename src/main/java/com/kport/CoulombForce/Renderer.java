package com.kport.CoulombForce;

import com.kport.CoulombForce.gui.GUIElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL41C.*;

public class Renderer {
    private static int particleVertexBuffer;
    private static int particleVertexArray;
    private static Shader particleShader;

    private static int lineVertexBuffer;
    private static int lineVertexArray;
    private static Shader lineShader;

    private static Shader arrowShader;

    private static List<LineSegment> additionalLineSegments = new ArrayList<>();
    private static List<LineSegment> arrows = new ArrayList<>();

    public static List<GUIElement> guiElements = new ArrayList<>();

    private static GLFWWindowManager windowManager;

    public static final float[] quadVertices = {
            -1, -1,
            1, -1,
            -1, 1,
            1, -1,
            -1, 1,
            1, 1
    };

    public static void init(GLFWWindowManager windowManager_){
        windowManager = windowManager_;

        glEnable(GL_PROGRAM_POINT_SIZE);
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


        try {
            particleShader = new Shader(Files.readString(Path.of("./shaders/particleFSH.glsl")),
                                        Files.readString(Path.of("./shaders/particleVSH.glsl")));

            lineShader = new Shader(Files.readString(Path.of("./shaders/lineFSH.glsl")),
                                    Files.readString(Path.of("./shaders/lineVSH.glsl")));

            arrowShader = new Shader(Files.readString(Path.of("./shaders/arrowFSH.glsl")),
                                     Files.readString(Path.of("./shaders/lineVSH.glsl")));

        } catch (IOException e) {
            e.printStackTrace();
        }



        lineVertexArray = glGenVertexArrays();
        glBindVertexArray(lineVertexArray);
        lineVertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, lineVertexBuffer);

        //Pos
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * 4, 0);
        glEnableVertexAttribArray(0);

        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);



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
        particleShader.use();
        int windowSizeLocation = particleShader.getUniformLocation("windowSize");
        glUniform2iv(windowSizeLocation, windowManager.getWindowSize());
        glDrawArrays(GL_POINTS, 0, particles.size());


        List<LineSegment> lineSegments = objects.stream().filter(o -> o instanceof LineSegment).map(o -> (LineSegment)o).collect(Collectors.toList());
        lineSegments.addAll(additionalLineSegments);

        glBindVertexArray(lineVertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, lineVertexBuffer);
        lineShader.use();

        windowSizeLocation = lineShader.getUniformLocation("windowSize");
        glUniform2iv(windowSizeLocation, windowManager.getWindowSize());

        int pos1Location = lineShader.getUniformLocation("pos1");
        int pos2Location = lineShader.getUniformLocation("pos2");
        int radiusLocation = lineShader.getUniformLocation("radius");

        for (LineSegment lineSegment : lineSegments) {
            glUniform2f(pos1Location, (float) lineSegment.getP1()[0], (float) lineSegment.getP1()[1]);
            glUniform2f(pos2Location, (float) lineSegment.getP2()[0], (float) lineSegment.getP2()[1]);
            glUniform1f(radiusLocation, (float) lineSegment.getRadius());
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }


        arrowShader.use();

        windowSizeLocation = arrowShader.getUniformLocation("windowSize");
        glUniform2iv(windowSizeLocation, windowManager.getWindowSize());

        pos1Location = arrowShader.getUniformLocation("pos1");
        pos2Location = arrowShader.getUniformLocation("pos2");
        radiusLocation = arrowShader.getUniformLocation("radius");

        for (LineSegment arrow : arrows) {
            glUniform2f(pos1Location, (float) arrow.getP1()[0], (float) arrow.getP1()[1]);
            glUniform2f(pos2Location, (float) arrow.getP2()[0], (float) arrow.getP2()[1]);
            glUniform1f(radiusLocation, (float) arrow.getRadius());
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }



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
