package planetZoooom.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;

import planetZoooom.input.Cursor;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.Game;
import planetZoooom.utils.Info;
import planetZoooom.utils.MatrixUtils;
import planetZoooom.utils.Timer;

public class CoreEngine
{
    private final Game game;

    public static Matrix4f projectionMatrix;
    public static Matrix4f orthographicProjectionMatrix;

    private float fovParam = 45.0f;

    public boolean running;
    public long windowHandle;
  
    boolean fullscreen = false;
    
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorCallback;

    public Timer timer;
    
    public CoreEngine(Game game) {
        this.game = game;

        projectionMatrix = MatrixUtils.perspectiveProjectionMatrix(fovParam, Info.gameRes[0], Info.gameRes[1]);
        orthographicProjectionMatrix = MatrixUtils.orthographicProjectionMatrix(0, -Info.gameRes[0], -Info.gameRes[1], 0.0f, -1.0f, 1.0f);
    }

    public void start() {
        running = true;

        init();

        while(running) {
            update();

            if(glfwWindowShouldClose(windowHandle) == GL_TRUE) {
                running = false;
            }
        }

        keyCallback.release();
        cursorCallback.release();
    }

    public void init() {
        if(glfwInit() != GL_TRUE)
        {
            System.err.println("can't initialize GLFW");
        }

        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        
        // necessary for OpenGL 3/4:
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    	GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
    	GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    	GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    	if(fullscreen) {
    		long monitor = glfwGetPrimaryMonitor();
    		GLFWvidmode mode = new GLFWvidmode(glfwGetVideoMode(monitor));
    		
    		windowHandle = glfwCreateWindow(mode.getWidth(), mode.getHeight(), "Stare into it device: " + windowHandle, monitor, NULL);
       	} 
    	else {
    		windowHandle = glfwCreateWindow(Info.gameRes[0], Info.gameRes[1], "Stare into it device: " + windowHandle, NULL, NULL);	
    	}
        
        if(windowHandle == NULL) {
            System.err.println("Window creation failed");
        }
        
        glfwSetWindowPos(windowHandle, 100, 100);

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(0); //vSync

        glfwShowWindow(windowHandle);

        GLContext.createFromCurrent();

        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetKeyCallback(windowHandle, keyCallback = new Keyboard());
        glfwSetCursorPosCallback(windowHandle, cursorCallback = new Cursor());

        game.init();
        
        timer = new Timer(); //not sure if best here
    }

    public void update() {
        glfwPollEvents();
   
        Info.fov = fovParam;
        Info.fps = timer.getFPS();
        game.update(timer.getDeltaTime());
        game.render();
        
        if(Keyboard.isKeyPressed(GLFW_KEY_ESCAPE))
        	glfwSetWindowShouldClose(windowHandle, GL_TRUE);
        
        glfwSwapBuffers(windowHandle);
    }

}
