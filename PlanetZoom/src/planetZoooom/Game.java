package planetZoooom;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.CoreEngine;
import planetZoooom.engine.Renderer;
import planetZoooom.gameContent.BillBoard;
import planetZoooom.gameContent.FreeCamera;
import planetZoooom.gameContent.HeadsUpDisplay;
import planetZoooom.gameContent.Planet;
import planetZoooom.geometry.MasterSphere;
import planetZoooom.graphics.ShaderProgram;
import planetZoooom.graphics.Texture;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.ICameraControl;
import planetZoooom.interfaces.IGame;
import planetZoooom.utils.GameUtils;
import planetZoooom.utils.Info;
import planetZoooom.utils.MatrixUtils;

public class Game implements IGame 
{
	private static CoreEngine game;
	private Renderer renderer;
	private ICameraControl cameraControl;
	private float fovParam = 45.0f;

	// GAMEOBJECTS
	private Planet planet;
	private HeadsUpDisplay hud;
	private BillBoard sun;
	private BillBoard sunGlow;
	private MasterSphere masterSphere;

	// TEXTURES
	private Texture planetTexture;
	private Texture sunTexture;
	private Texture sunGlowTexture;

	// SHADERS
	private ShaderProgram planetShader;
	private ShaderProgram wireFrameShader;	
	private ShaderProgram hudShader;
	private ShaderProgram sunShader;
	private ShaderProgram sunGlowShader;
	private ShaderProgram atmosphereShader;
	private ShaderProgram testShader;

	//Matrices
	private Matrix4f modelViewMatrix;
	private Matrix4f normalMatrix;
	private Matrix4f orthographicProjectionMatrix;
	
	private Vector3f lightDirection;
	private float planetCamDistance;
	
	//	CONTROLS
	private boolean wireframe = false;
	private boolean updateSphere = true;
	private float flatShading = 0.0f;
	
	public static void main(String[] args) 
	{
		game = new CoreEngine(new Game());
		game.start();
	}

	@Override
	public void init() 
	{
		printVersionInfo();

		Info.camera = new FreeCamera(0.0f, 0.0f, 8);
		Info.projectionMatrix = planetZoooom.utils.MatrixUtils.perspectiveProjectionMatrix(fovParam, game.getWindowWidth(), game.getWindowHeight());
		
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		orthographicProjectionMatrix = MatrixUtils.orthographicProjectionMatrix(0, -game.getWindowWidth(), -game.getWindowHeight(), 0.0f, -1.0f, 1.0f);

		renderer = new Renderer();		
		lightDirection = new Vector3f();
		
		initTextures();
		initShaders();
		initGameObjects();

		Info.planet = planet;
	}

	private void initTextures() 
	{
		planetTexture = new Texture("src/res/textures/uv_test.png");
		sunTexture = new Texture("src/res/textures/sun.png");
		sunGlowTexture = new Texture("src/res/textures/sunGlow3.png");
	}

	private void initShaders() 
	{
		hudShader = new ShaderProgram("HUDShader");
		planetShader = new ShaderProgram("planetShader");
		wireFrameShader = new ShaderProgram("testShader");
		sunShader = new ShaderProgram("sunShader");
		sunGlowShader = new ShaderProgram("sunGlowShader");
		atmosphereShader = new ShaderProgram("atmosphereShader");
	}

	private void initGameObjects() 
	{
//		planet = new Planet(6500.0f, new Vector3f(0f, 0f, 0f));
		masterSphere = new MasterSphere(1, 10000);
		
		hud = new HeadsUpDisplay(0, 0, "arial_nm.png", Info.camera.getPosition(), new Vector3f(0.0f, 0.0f, 0.0f), 0f, 0, 0, 0);
		sun = new BillBoard(new Vector3f(-100000.0f, 0.0f, 0.0f), 100000.0f);
		sun.setTexture(sunTexture);
		sunGlow = new BillBoard(new Vector3f(-99000.0f, 0.0f, 0.0f), 1.0f);
		sunGlow.setTexture(sunGlowTexture);
	}

	
	@Override
	public void update(int deltaTime) 
	{
		this.processKeyboardInputs(deltaTime);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //DO NOT MOVE THIS LINE! ....THERE IS A REASON THAT IT IS NOT IN RENDERER;
		
		glDisable(GL_DEPTH_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);
		
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.setIdentity();
		Matrix4f.mul(Info.camera.getViewMatrix(), modelMatrix, modelViewMatrix);
		
//		Matrix4f.mul(Info.camera.getViewMatrix(), sun.getModelMatrix(), modelViewMatrix);
//		drawSun();		
//		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
//		glFrontFace(GL_CW);
//		Matrix4f.mul(Info.camera.getViewMatrix(), planet.getAtmosphere().getModelMatrix(), modelViewMatrix);
//		Vector3f.sub(sun.getPosition(), planet.getAtmosphere().getPosition(), lightDirection);
//		lightDirection.normalise();
//		drawAtmosphere();
		glFrontFace(GL_CCW);
		glEnable(GL_DEPTH_TEST);
		
		Matrix4f.mul(Info.camera.getViewMatrix(), (Matrix4f) new Matrix4f().setIdentity(), modelViewMatrix);
		Matrix4f.invert(modelViewMatrix, normalMatrix);
		
		if(updateSphere)
			masterSphere.update();
//			planet.update();
		
//		drawPlanet();
//		masterSphere.render(GL_LINES);
		drawMasterSphere();

		hud.update(Info.camera.getPosition(), Info.camera.getLookAt(), 0, masterSphere.getVertexCount(), masterSphere.getTriangleCount(), game.timer.getFPS());
		drawHUD();
	}

	private void drawSun()
	{
		glUseProgram(sunGlowShader.getId());
		{
			sunGlowShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			sunGlowShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			sunGlowShader.loadUniformVec3f(sunGlow.getPosition(), "billboardCenter");
			sunGlow.render(GL_TRIANGLES);
		}

		glUseProgram(sunShader.getId());
		{
			sunShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			sunShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			sunShader.loadUniformVec3f(sun.getPosition(), "billboardCenter");
			sunShader.loadUniformVec3f(Info.camera.getLocalUpVector(), "cameraUp");
			sunShader.loadUniformVec3f(Info.camera.getLocalRightVector(), "cameraRight");	
			sun.render(GL_TRIANGLES);
		}
	}
	
	private void drawAtmosphere()
	{
		glUseProgram(atmosphereShader.getId());
		{
			planet.getAtmosphere().loadSpecificUniforms(atmosphereShader);
			atmosphereShader.loadUniform1f(GameUtils.getDistanceBetween(planet.getPosition(), Info.camera.getPosition()), "cameraHeight");
			atmosphereShader.loadUniformVec3f(lightDirection, "lightDirection");
			atmosphereShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
			atmosphereShader.loadUniform1f(1.0f / (planet.getAtmosphere().getSphere().getRadius() - planet.getRadius()), "fScale");
			//		atmosphereShader.loadUniform1f(planet.getAtmosphere().getSphere().getRadius() * 0.25f, "fScale");
			atmosphereShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			atmosphereShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			atmosphereShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
			atmosphereShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
			atmosphereShader.loadUniform1f(planet.getRadius(), "planetRadius");
			planet.getAtmosphere().getSphere().render(GL_TRIANGLES);
			
//			if(wireframe)
//			{
//				glUseProgram(wireFrameShader.getId());
//				wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
//				wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
//				wireFrameShader.loadUniform1f(0.4f, "greytone");
//				planet.getAtmosphere().getSphere().render(GL_LINE_STRIP);
//				wireFrameShader.loadUniform1f(1.0f, "greytone");
//				planet.getAtmosphere().getSphere().render(GL_POINTS);
//			}
		}
	}
	private void drawMasterSphere()
	{
		glUseProgram(wireFrameShader.getId());
		wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
		wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		wireFrameShader.loadUniform1f(0.4f, "greytone");
		masterSphere.render(GL_LINES);

	}
	
	private void drawPlanet()
	{
		glUseProgram(planetShader.getId());
		{
			planetShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			planetShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			planetShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
			planetShader.loadUniformVec3f(sun.getPosition(), "lightPosition");
			planetShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
			planetShader.loadUniform1f(planet.getRadius(), "radius");
			planetShader.loadUniform1f(flatShading, "flatShading");
			planetShader.loadUniform1f(planet.getMountainHeight(), "mountainHeight");
			planet.getSphere().render(GL_TRIANGLES);
			
			if(wireframe)
			{
				glDepthFunc(GL_LEQUAL);
				glUseProgram(wireFrameShader.getId());
				wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
				wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
				wireFrameShader.loadUniform1f(0.5f, "greytone");
				planet.getSphere().render(GL_LINES);
				wireFrameShader.loadUniform1f(1.0f, "greytone");
				planet.getSphere().render(GL_POINTS);
				glDepthFunc(GL_LESS);
			}
		}
	}
	
	private void drawHUD()
	{
		glUseProgram(hudShader.getId());
		{
			hudShader.loadUniformMat4f(orthographicProjectionMatrix, "projectionMatrix", false);
			hudShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
			hud.getTextMesh().render(GL_TRIANGLES);
		}
		glUseProgram(0);
	}
	
	private void printVersionInfo() 
	{
		System.out.println("GPU Vendor: " + glGetString(GL_VENDOR));
		System.out.println("OpenGL version: " + glGetString(GL_VERSION));
		System.out.println("GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));
	}
	
	private void processKeyboardInputs(int deltaTime) {
		
		cameraControl = Info.camera.getCameraControl();
		Info.camera = cameraControl.handleInput(deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_1)){ wireframe = !wireframe; }
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_2)){ flatShading = 1.0f; }	
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_3)){ flatShading = 0.0f; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_9)){ updateSphere = !updateSphere; }
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_O))
			planet.setAmplitude(planet.getAmplitude() + 0.02f);
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_L))
			planet.setAmplitude(planet.getAmplitude() - 0.02f);
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_I))
			planet.setOctaves(planet.getOctaves() + 1);
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_K))
			planet.setOctaves(planet.getOctaves() - 1);
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_U))
			planet.setLambdaBaseFactor(planet.getLambdaBaseFactor() + 0.001f);
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_J))
			planet.setLambdaBaseFactor(planet.getLambdaBaseFactor() - 0.001f);
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_Y))
			planet.setNoiseSeed(planet.getNoiseSeed() + planet.getRadius() / 1000);
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_H))
			planet.setNoiseSeed(planet.getNoiseSeed() - planet.getRadius() / 1000);
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_T))
			planet.setMountainHeight(planet.getMountainHeight() + 0.0005f);
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_G))
			planet.setMountainHeight(planet.getMountainHeight() - 0.0005f);
	}
}
