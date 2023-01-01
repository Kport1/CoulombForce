package com.kport.CoulombForce.gui;

import com.kport.CoulombForce.GLFWWindowManager;

public interface GUIElement {

    void init(GLFWWindowManager windowManager);
    void render();


    boolean handleMouseButtonEvent(long window, int button, int action, int i);
    boolean handleKeyEvent(long window, int key, int scancode, int action, int mods);
    boolean handleCursorPosEvent(long window, double x, double y);
    boolean handleScrollEvent(long window, double d, double dir);

}
