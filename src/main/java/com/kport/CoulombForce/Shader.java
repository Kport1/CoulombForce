package com.kport.CoulombForce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL41C.*;

public class Shader {
    private final int program;
    private Map<String, Integer> uniformLocations = new HashMap<>();

    public Shader(String fshSource, String vshSource){
        int fsh = glCreateShader(GL_FRAGMENT_SHADER);
        int vsh = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(fsh, fshSource);
        glShaderSource(vsh, vshSource);

        glCompileShader(fsh);
        glCompileShader(vsh);

        int[] status = new int[1];
        glGetShaderiv(fsh, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(fsh));
        glGetShaderiv(vsh, GL_COMPILE_STATUS, status);
        if(status[0] == 0)
            throw new Error(glGetShaderInfoLog(vsh));

        program = glCreateProgram();
        glAttachShader(program, fsh);
        glAttachShader(program, vsh);
        glLinkProgram(program);
        glDeleteShader(fsh);
        glDeleteShader(vsh);
        glGetProgramiv(program, GL_LINK_STATUS, status);
        if(status[0] == 0){
            throw new Error(glGetProgramInfoLog(program));
        }
    }

    public void use(){
        glUseProgram(program);
    }

    public int getUniformLocation(String name){
        uniformLocations.computeIfAbsent(name, (n) -> glGetUniformLocation(program, n));
        return uniformLocations.get(name);
    }
}
