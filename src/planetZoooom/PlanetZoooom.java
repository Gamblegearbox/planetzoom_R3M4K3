package planetZoooom;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.CoreEngine;
import planetZoooom.engine.Camera;
import planetZoooom.gameContent.*;
import planetZoooom.geometry.*;
import planetZoooom.graphics.ShaderProgram;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.Game;
import planetZoooom.utils.Info;

public class PlanetZoooom implements Game 
{
	private static final int HUD_MODE_OFF = 0;
	private static final int HUD_MODE_INFO = 1;

	private static final Vector3f CAM_START_POSITION = new Vector3f(0.0f, 20.0f, 100.0f);


	// GAMEOBJECTS
	private PolyBullet[] bullets;
	private Polystrip polystrip;
	private PlayerShip playerShip;
	private HeadsUpDisplay hud;

	// SHADERS
	private ShaderProgram hudShader;
	private ShaderProgram unshadedVertexColorShader;

	// MATRICES // VECTORS
	private Matrix4f modelViewMatrix;
	private Matrix4f normalMatrix;
	
	// CONTROLS
	private boolean wireframe = true;
	private float time = 0;
	private int hudMode = 1;
	
	@Override
	public void init() {
		printVersionInfo();
		Info.camera = new Camera(new Vector3f(CAM_START_POSITION.x, CAM_START_POSITION.y, CAM_START_POSITION.z));
		
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		
		initOpenGL();
		initShaders();
		initGameObjects();

		hudMode = 1;
		updateHud(hudMode);
	}

	private void initOpenGL() {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);		
        glEnable(GL_POINT_SMOOTH);
        glPointSize(3.5f);
		glLineWidth(3.0f);
	}
	
	private void initShaders() {
		hudShader = new ShaderProgram("HUDShader");
		unshadedVertexColorShader = new ShaderProgram("unshadedVertexColor");
	}

	private void initGameObjects() {
		polystrip = new Polystrip(1, 50, false);
		playerShip = new PlayerShip();
		bullets = new PolyBullet[100];
		
		for (int i = 0; i < bullets.length; i++) {
			bullets[i] = new PolyBullet(10f);
		}

		hud = new HeadsUpDisplay(10, 10, "arial_nm.png");
	}

	@Override
	public void update(float deltaTime) {
		time += deltaTime;

		Info.camera.update(deltaTime);
		playerShip.update(deltaTime);
		
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_5)){ wireframe = !wireframe; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_TAB)){ 
			hudMode = (hudMode+1) % 2; 
			updateHud(hudMode);	
		}
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_ENTER)){ shoot(); }
		for (int i = 0; i < bullets.length; i++) {
			bullets[i].update(deltaTime);
		}
	}

	@Override
	public void render(){
		
		// check if rendered (inFrustum, debug, freeze, etc) and add to render list
		// sort render list by distance
		// render list content
		
		// for (MeshObject mesh : meshes){
			// 	//TOTO: check if visible / infrustum
			// 	//update distance
			// 	float distance = GameUtils.getDistanceBetween(mesh.getPosition(), Info.camera.getPosition());
			// 	mesh.setDistanceToCam(distance);
			// }
			// meshes.sort( (a, b) -> { return a.compareTo(b); } );
			
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glUseProgram(unshadedVertexColorShader.getId());
		unshadedVertexColorShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
		drawLevel();
		drawPlayer();
		drawEffects();

		if(hudMode != HUD_MODE_OFF) {
			drawHUD();
		}
	}
	
	private void drawLevel() {
		Matrix4f.mul(Info.camera.getViewMatrix(), polystrip.getModelMatrix(), modelViewMatrix);
		Matrix4f.invert(polystrip.getModelMatrix(), normalMatrix);

		unshadedVertexColorShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		unshadedVertexColorShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		
		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			polystrip.render(GL_LINE_LOOP);
			polystrip.render(GL_POINTS);
		 	glDepthFunc(GL_LESS);
		}
		else {
			polystrip.render(GL_TRIANGLES);
		}
	}

	private void drawPlayer(){
		Matrix4f.mul(Info.camera.getViewMatrix(), playerShip.getModelMatrix(), modelViewMatrix);
		Matrix4f.invert(polystrip.getModelMatrix(), normalMatrix);

		unshadedVertexColorShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		unshadedVertexColorShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);

		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			playerShip.render(GL_LINE_LOOP);
			playerShip.render(GL_POINTS);
		 	glDepthFunc(GL_LESS);
		}
		else {
			playerShip.render(GL_TRIANGLES);
		}
	}

	private void drawEffects() {
		Matrix4f viewMatrix4f = Info.camera.getViewMatrix();

		for (int i = 0; i < bullets.length; i++) {
			Matrix4f.mul(viewMatrix4f, bullets[i].getModelMatrix(), modelViewMatrix);
			unshadedVertexColorShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);	
			
			bullets[i].render(GL_POINTS);
			bullets[i].render(GL_LINES);
		}
	}
	
	private void drawHUD() {
		unshadedVertexColorShader.loadUniformMat4f(CoreEngine.orthographicProjectionMatrix, "projectionMatrix", false);
		unshadedVertexColorShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
		hud.getBackgroundMesh().render(GL_TRIANGLES);

		glUseProgram(hudShader.getId());	
		hudShader.loadUniformMat4f(CoreEngine.orthographicProjectionMatrix, "projectionMatrix", false);
		hudShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
		hud.getTextMesh().render(GL_TRIANGLES);
		glUseProgram(0);
	}
	
	private void updateHud(int mode) {
		switch(mode) {
			case HUD_MODE_OFF: {
				hud.update("");
				return;
			}
			case HUD_MODE_INFO: {
				hud.update("FPS: " + Info.fps);
				return;
			}
		}
		
		throw new IllegalArgumentException();
	}
			
	private void printVersionInfo() {
		System.out.println("GPU Vendor: " + glGetString(GL_VENDOR));
		System.out.println("OpenGL version: " + glGetString(GL_VERSION));
		System.out.println("GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));
	}
	
	private void shoot() {
		for (int i = 0; i < bullets.length; i++) {
			
			if (bullets[i].getIsActive() == false) {
				bullets[i].setIsActive(playerShip.getPosition());
				return;
			}
		}
	}
}
