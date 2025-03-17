package planetZoooom;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.CoreEngine;
import planetZoooom.gameContent.BillBoard;
import planetZoooom.gameContent.FreeCamera;
import planetZoooom.gameContent.HeadsUpDisplay;
import planetZoooom.gameContent.Planet;
import planetZoooom.graphics.ShaderProgram;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.Game;
import planetZoooom.utils.GameUtils;
import planetZoooom.utils.Info;

public class PlanetZoooom implements Game 
{
	private static final int HUD_MODE_OFF = 0;
	private static final int HUD_MODE_INFO = 1;

	private static final float[] HUD_BG_PURPLE = new float[] {0.73f, 0.47f, 0.8f, 0.9f};
	private static final float[] HUD_BG_GREY = new float[] {0.6f, 0.6f, 0.6f, 0.9f};

	private static final Vector3f SUN_POSITION = new Vector3f(-500.0f, 0.0f, 0.0f);
	private static final Vector3f CAM_START_POSITION = new Vector3f(0.0f, 0.0f, 300.0f);
	private static final float CAM_COLLISION_OFFSET = 2.0f;
	private static final float AMBIENT_LIGHT_STRENGTH = 0.1f;

	// GAMEOBJECTS
	private Planet planet;
	private HeadsUpDisplay hud;
	private BillBoard sun;

	// SHADERS
	private ShaderProgram planetShader;
	private ShaderProgram waterShader;
	private ShaderProgram wireFrameShader;	
	private ShaderProgram hudShader;
	private ShaderProgram sunShader;
	private ShaderProgram atmosphereShader;
	private ShaderProgram colorShader;

	// MATRICES
	private Matrix4f modelViewMatrix;
	private Matrix4f normalMatrix;
	//private Matrix4f orthographicProjectionMatrix;
	private Vector3f lightDirection;
	
	// CONTROLS
	private boolean wireframe = false;

	private float time = 0;
	private int hudMode;
	private Vector3f planetToCam;
	
	@Override
	public void init() {
		printVersionInfo();
		planetToCam = new Vector3f();
		Info.camera = new FreeCamera(new Vector3f(CAM_START_POSITION.x, CAM_START_POSITION.y, CAM_START_POSITION.z));
		
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		lightDirection = new Vector3f();
		
		initOpenGL();
		initShaders();
		initGameObjects();

		hudMode = 0;
	}

	private void initOpenGL() {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glPointSize(2.5f);
	}
	
	private void initShaders() {
		hudShader = new ShaderProgram("HUDShader");
		planetShader = new ShaderProgram("planetShader");
		waterShader = new ShaderProgram("waterShader");
		wireFrameShader = new ShaderProgram("wireFrameShader");
		sunShader = new ShaderProgram("sunShader");
		atmosphereShader = new ShaderProgram("atmosphereShader");
		colorShader = new ShaderProgram("colorShader");
	}

	private void initGameObjects() {
		planet = new Planet(100.0f, 5, new Vector3f(0f, 0f, 0f), modelViewMatrix);
		hud = new HeadsUpDisplay(10, 10, "arial_nm.png", HUD_BG_GREY);
		sun = new BillBoard(SUN_POSITION, 100.0f, 100.0f);
	}

	@Override
	public void update(float deltaTime) {
		time += deltaTime;

		this.processKeyboardInputs(deltaTime);
		this.updateCamera();

		updateHud(hudMode);	
	}

	@Override
	public void render(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

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

		drawSun();	
		drawPlanet();
		drawHUD();
	}

	private void drawSun() {
		Matrix4f.mul(Info.camera.getViewMatrix(), sun.getModelMatrix(), modelViewMatrix);
	
		glUseProgram(sunShader.getId());
		sunShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
		sunShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		sunShader.loadUniform1f(time, "time");

		sun.render(GL_TRIANGLES);
	}
	
	private void drawPlanet() {
		Matrix4f.mul(Info.camera.getViewMatrix(), planet.getWaterSurface().getModelMatrix(), modelViewMatrix);
		
		drawPlanetSurface();

		glFrontFace(GL_CW);
		drawAtmosphere();
		glFrontFace(GL_CCW);
			
		if(planet.getHasWater()) {
			drawWaterSurface();
		}
		
	}

	private void drawAtmosphere() {
		Matrix4f.invert(planet.getAtmosphere().getModelMatrix(), normalMatrix);
		Vector3f.sub(SUN_POSITION, planet.getAtmosphere().getPosition(), lightDirection);
		lightDirection.normalise();
		
		glUseProgram(atmosphereShader.getId());
		atmosphereShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
		atmosphereShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		atmosphereShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		atmosphereShader.loadUniformVec3f(SUN_POSITION, "lightPosition");
		atmosphereShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		atmosphereShader.loadUniform1f(planet.getRadius(), "radius");
		atmosphereShader.loadUniform1f(AMBIENT_LIGHT_STRENGTH, "ambientLight");
			
		planet.getAtmosphere().render(GL_TRIANGLES);

		if(wireframe)
		{
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
			wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			wireFrameShader.loadUniform1f(0.5f, "greytone");
			planet.getAtmosphere().render(GL_LINES);
			wireFrameShader.loadUniform1f(0.9f, "greytone");
			planet.getAtmosphere().render(GL_POINTS);
			glDepthFunc(GL_LESS);
		}
	}
	
	private void drawPlanetSurface() {
		Matrix4f.invert(planet.getPlanetSurface().getModelMatrix(), normalMatrix);

		glUseProgram(planetShader.getId());
		planetShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
		planetShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		planetShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		planetShader.loadUniformVec3f(SUN_POSITION, "lightPosition");
		planetShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		planetShader.loadUniform1f(planet.getRadius(), "radius");
		planetShader.loadUniform1f(AMBIENT_LIGHT_STRENGTH, "ambientLight");

		planet.getPlanetSurface().render(GL_TRIANGLES);
		
		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
			wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			wireFrameShader.loadUniform1f(0.5f, "greytone");
			planet.getPlanetSurface().render(GL_LINES);
			wireFrameShader.loadUniform1f(0.8f, "greytone");
			planet.getPlanetSurface().render(GL_POINTS);
			glDepthFunc(GL_LESS);
		}
	}
	
	private void drawWaterSurface() {
		Matrix4f.invert(planet.getWaterSurface().getModelMatrix(), normalMatrix);

		glUseProgram(waterShader.getId());
		waterShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
		waterShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		waterShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		waterShader.loadUniformVec3f(SUN_POSITION, "lightPosition");
		waterShader.loadUniformVec3f(Planet.BLUE, "waterColor");
		waterShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		waterShader.loadUniform1f(AMBIENT_LIGHT_STRENGTH, "ambientLight");

		planet.getWaterSurface().render(GL_TRIANGLES);

		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(CoreEngine.projectionMatrix, "projectionMatrix", false);
			wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			wireFrameShader.loadUniform1f(0.5f, "greytone");
			planet.getWaterSurface().render(GL_LINES);
			wireFrameShader.loadUniform1f(0.8f, "greytone");
			planet.getWaterSurface().render(GL_POINTS);
			glDepthFunc(GL_LESS);
		}
	}

	private void drawHUD() {
		glUseProgram(colorShader.getId());
		{
			colorShader.loadUniformMat4f(CoreEngine.orthographicProjectionMatrix, "projectionMatrix", false);
			colorShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
			hud.getBackgroundMesh().render(GL_TRIANGLES);
		}
		glUseProgram(0);

		glUseProgram(hudShader.getId());
		{
			hudShader.loadUniformMat4f(CoreEngine.orthographicProjectionMatrix, "projectionMatrix", false);
			hudShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
			hud.getTextMesh().render(GL_TRIANGLES);
		}
		glUseProgram(0);
	}
	
	private void updateHud(int mode) {
		switch(mode) {
			case HUD_MODE_OFF: {
				hud.update("");
				return;
			}
			case HUD_MODE_INFO: {
				hud.setBackgroundColor(HUD_BG_PURPLE);
				hud.update(getInfoHUDText());
				return;
			}
		}
		
		throw new IllegalArgumentException();
	}
	
	private String getInfoHUDText() {

		return  String.format(
				"General information\n\n"
				+ "Distance:     %.2f\n"
				+ "Camera FOV: 	 %.2f\n"
				+ "Subdivisions: %d\n"
				+ "FPS:          %d",
				GameUtils.getDistanceBetween(planet.getPosition(), Info.camera.getPosition()), 
				Info.fov,
				planet.getSubdivisions(),
				Info.fps);
	}
			
	private void printVersionInfo() {
		System.out.println("GPU Vendor: " + glGetString(GL_VENDOR));
		System.out.println("OpenGL version: " + glGetString(GL_VERSION));
		System.out.println("GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));
	}
	
	private void reset() {
		planet.resetPlanet();
		wireframe = false;
	}
	
	private void processKeyboardInputs(float deltaTime) {
		
		Info.camera.handleInput(deltaTime);
		
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_5)){ wireframe = !wireframe; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_TAB)){ hudMode = (hudMode + 1) % 2; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_ENTER)){ reset(); }
		
		switch(hudMode) {
			case HUD_MODE_INFO: {
				// if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_O))
				// 	//fovParam = fovParam + 0.005f;
				// else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_L))
				// 	//fovParam = fovParam - 0.005f;
				break;
			}
		}
	}

	private void updateCamera(){
		Vector3f.sub(Info.camera.getPosition(), planet.getPosition(), planetToCam);

		float camPlanetDistance = planetToCam.length() - planet.getRadius();
		adjustCamSpeed(camPlanetDistance);
		handleCollision(planetToCam);
	}

	private void adjustCamSpeed(float camPlanetDistance) {
		float slowDownRadius = planet.getRadius() * 1.15f;
		
		if(camPlanetDistance < slowDownRadius) {
			float camSpeed = FreeCamera.MAX_CAM_SPEED / slowDownRadius * camPlanetDistance;
			
			if(camSpeed < FreeCamera.MIN_CAM_SPEED)
				camSpeed = FreeCamera.MIN_CAM_SPEED;

				Info.camera.setVelocity(camSpeed);
		} 
		else {
			Info.camera.setVelocity(FreeCamera.MAX_CAM_SPEED);
		}
	}
	
	private void handleCollision(Vector3f planetToCam) {
		Vector3f cam = Info.camera.getPosition();

		float planetRadius = planet.getRadius();

		float actualCamDistance = planetToCam.length();
		planetToCam.normalise().scale(planetRadius);

		float minCamDistance = planetRadius * 1.05f;

		if (actualCamDistance < minCamDistance + CAM_COLLISION_OFFSET) {
			planetToCam.normalise();
			planetToCam.scale(-(actualCamDistance - minCamDistance) + CAM_COLLISION_OFFSET);

			Vector3f.add(cam, planetToCam, cam);
		}
	}
}
