package planetZoooom;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.CoreEngine;
import planetZoooom.gameContent.Atmosphere;
import planetZoooom.gameContent.BillBoard;
import planetZoooom.gameContent.FreeCamera;
import planetZoooom.gameContent.HeadsUpDisplay;
import planetZoooom.gameContent.Planet;
import planetZoooom.gameContent.WaterSurface;
import planetZoooom.graphics.ShaderProgram;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.Game;
import planetZoooom.utils.CustomNoise;
import planetZoooom.utils.GameUtils;
import planetZoooom.utils.Info;
import planetZoooom.utils.MatrixUtils;

public class PlanetZoooom implements Game 
{
	private static CoreEngine coreEngine;
	private float fovParam = 45.0f;

	// GAMEOBJECTS
	private Planet planet;
	private HeadsUpDisplay hud;
	private BillBoard sun;

	// SHADERS
	private ShaderProgram earthShader;
	private ShaderProgram marsShader;
	private ShaderProgram dessertShader;
	private ShaderProgram uniColorPlanetShader;
	private ShaderProgram waterShader;
	private ShaderProgram wireFrameShader;	
	private ShaderProgram hudShader;
	private ShaderProgram sunShader;
	private ShaderProgram atmosphereShader;
	private ShaderProgram colorShader;

	// MATRICES
	private Matrix4f modelViewMatrix;
	private Matrix4f normalMatrix;
	private Matrix4f orthographicProjectionMatrix;
	private Vector3f lightDirection;
	
	// CONTROLS
	private boolean wireframe = false;
	private boolean freezeUpdate = false;

	private static final int HUD_MODE_OFF = 0;
	private static final int HUD_MODE_INFO = 1;
	private static final int HUD_MODE_NOISE = 2;
	private static final int HUD_MODE_ATMOSPHERE = 3;
	
	private static final float[] HUD_BG_YELLOW = new float[] {0.8f, 0.62f, 0.00f, 0.9f};
	private static final float[] HUD_BG_WHITE = new float[] {0.6f, 0.6f, 0.6f, 0.9f};
	private static final float[] HUD_BG_GREY = new float[] {0.6f, 0.6f, 0.6f, 0.9f};
	private static final float[] HUD_BG_PURPLE = new float[] {0.73f, 0.47f, 0.8f, 0.9f};

	private static final Vector3f SUN_POSITION = new Vector3f(-500.0f, 0.0f, 0.0f);
	private static final Vector3f CAM_START_POSITION = new Vector3f(0.0f, 0.0f, 300.0f);
	private static final float CAM_COLLISION_OFFSET = 2.0f;
	private static final float AMBIENT_LIGHT_STRENGTH = 0.1f;

	private float time = 0;
	
	private int hudMode;
	
	public static void main(String[] args) {
		coreEngine = new CoreEngine(new PlanetZoooom());
		coreEngine.start();
	}

	@Override
	public void init() {
		printVersionInfo();

		Info.camera = new FreeCamera(new Vector3f(CAM_START_POSITION.x, CAM_START_POSITION.y, CAM_START_POSITION.z));
		Info.projectionMatrix = planetZoooom.utils.MatrixUtils.perspectiveProjectionMatrix(fovParam, coreEngine.getWindowWidth(), coreEngine.getWindowHeight());
		
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		orthographicProjectionMatrix = MatrixUtils.orthographicProjectionMatrix(0, -coreEngine.getWindowWidth(), -coreEngine.getWindowHeight(), 0.0f, -1.0f, 1.0f);
		lightDirection = new Vector3f();
		
		initOpenGL();
		initShaders();
		initGameObjects();

		Info.planet = planet;
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
		earthShader = new ShaderProgram("earthShader");
		marsShader = new ShaderProgram("marsShader");
		dessertShader = new ShaderProgram("dessertShader");
		uniColorPlanetShader = new ShaderProgram("uniColorPlanetShader");
		waterShader = new ShaderProgram("waterShader");
		wireFrameShader = new ShaderProgram("wireFrameShader");
		sunShader = new ShaderProgram("sunShader");
		atmosphereShader = new ShaderProgram("atmosphereShader");
		colorShader = new ShaderProgram("colorShader");
	}

	private void initGameObjects() {
		planet = new Planet(100.0f, new Vector3f(0f, 0f, 0f), modelViewMatrix);
		hud = new HeadsUpDisplay(10, 10, "arial_nm.png", HUD_BG_WHITE);
		sun = new BillBoard(SUN_POSITION, 100.0f, 100.0f);
	}

	@Override
	public void update(float deltaTime) {
		time += deltaTime;

		this.processKeyboardInputs(deltaTime);
		this.updateCamera();
		
		if(!freezeUpdate) {
			planet.update(modelViewMatrix);
		}

		updateHud(hudMode);	
	}

	@Override
	public void render(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if(freezeUpdate) {
			glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		}
		else {
			glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		}
		
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
		sunShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
		sunShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		sunShader.loadUniform1f(time, "time");

		sun.render(GL_TRIANGLES);
	}
	
	private void drawPlanet() {
		Matrix4f.mul(Info.camera.getViewMatrix(), planet.getPlanetSurface().getModelMatrix(), modelViewMatrix);
	
		drawPlanetSurface();
		
		if(!freezeUpdate) {
			glFrontFace(GL_CW);
			drawAtmosphere();
			glFrontFace(GL_CCW);
			
			if(planet.getHasWater()) {
				drawWaterSurface();
			}
		}
		
	}

	private void drawAtmosphere() {
		Matrix4f.invert(planet.getAtmosphere().getModelMatrix(), normalMatrix);
		Vector3f.sub(SUN_POSITION, planet.getAtmosphere().getPosition(), lightDirection);
		lightDirection.normalise();
		
		glUseProgram(atmosphereShader.getId());
		float cameraHeight = GameUtils.getDistanceBetween(planet.getPosition(), Info.camera.getPosition());
		planet.getAtmosphere().loadSpecificUniforms(atmosphereShader);
		atmosphereShader.loadUniform1f(cameraHeight, "cameraHeight");
		atmosphereShader.loadUniformVec3f(lightDirection, "lightDirection");
		atmosphereShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		atmosphereShader.loadUniform1f(1.0f / (planet.getAtmosphere().getSphere().getRadius() - planet.getRadius()), "fScale");
		atmosphereShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
		atmosphereShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		atmosphereShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		atmosphereShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		atmosphereShader.loadUniform1f(planet.getRadius() + planet.getRadius() * 0.09f, "planetRadius");
			
		planet.getAtmosphere().getSphere().render(GL_TRIANGLES);

		if(wireframe)
		{
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			wireFrameShader.loadUniform1f(0.5f, "greytone");
			planet.getAtmosphere().getSphere().render(GL_LINES);
			wireFrameShader.loadUniform1f(0.9f, "greytone");
			planet.getAtmosphere().getSphere().render(GL_POINTS);
			glDepthFunc(GL_LESS);
		}
		
	}
	
	private void drawPlanetSurface() {
		Matrix4f.invert(planet.getPlanetSurface().getModelMatrix(), normalMatrix);
		
		ShaderProgram shader;
		switch(planet.getShaderMode()) {
			case Planet.STYLE_EARTH:	shader = earthShader;
										planet.setHasWater(true);
										break;
			case Planet.STYLE_MARS:		shader = marsShader;
										planet.setHasWater(false);
										break;
			case Planet.STYLE_DUNE: 	shader = dessertShader;
										planet.setHasWater(false);
										break;
			case Planet.STYLE_UNICOLOR: shader = uniColorPlanetShader;
										planet.setHasWater(true);
										break;
			default: 					throw new IllegalArgumentException();
		}
		
		glUseProgram(shader.getId());
		shader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
		shader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		shader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		shader.loadUniformVec3f(SUN_POSITION, "lightPosition");
		shader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		shader.loadUniform1f(planet.getRadius(), "radius");
		shader.loadUniform1f(AMBIENT_LIGHT_STRENGTH, "ambientLight");

		planet.getPlanetSurface().render(GL_TRIANGLES);
		
		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
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
		waterShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
		waterShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
		waterShader.loadUniformMat4f(normalMatrix, "normalMatrix", true);
		waterShader.loadUniformVec3f(SUN_POSITION, "lightPosition");
		waterShader.loadUniformVec3f(Info.camera.getPosition(), "cameraPosition");
		waterShader.loadUniform1f(AMBIENT_LIGHT_STRENGTH, "ambientLight");

		switch(planet.getShaderMode()) {
			case Planet.STYLE_EARTH:	waterShader.loadUniformVec3f(WaterSurface.BLUE, "waterColor");
			case Planet.STYLE_DUNE: 	waterShader.loadUniformVec3f(WaterSurface.BLUE, "waterColor");		
										break;
			case Planet.STYLE_MARS:		waterShader.loadUniformVec3f(WaterSurface.RED, "waterColor");
			case Planet.STYLE_UNICOLOR: waterShader.loadUniformVec3f(WaterSurface.RED, "waterColor");
										break;
	
			default: 					throw new IllegalArgumentException();
		}
		
		planet.getWaterSurface().getSphere().render(GL_TRIANGLES);

		if(wireframe) {
			glDepthFunc(GL_LEQUAL);
			glUseProgram(wireFrameShader.getId());
			wireFrameShader.loadUniformMat4f(Info.projectionMatrix, "projectionMatrix", false);
			wireFrameShader.loadUniformMat4f(modelViewMatrix, "modelViewMatrix", false);
			wireFrameShader.loadUniform1f(0.5f, "greytone");
			planet.getWaterSurface().getSphere().render(GL_LINES);
			wireFrameShader.loadUniform1f(0.8f, "greytone");
			planet.getWaterSurface().getSphere().render(GL_POINTS);
			glDepthFunc(GL_LESS);
		}
	}

	private void drawHUD() {
		glUseProgram(colorShader.getId());
		{
			colorShader.loadUniformMat4f(orthographicProjectionMatrix, "projectionMatrix", false);
			colorShader.loadUniformMat4f(hud.getModelMatrix(), "modelViewMatrix", false);
			hud.getBackgroundMesh().render(GL_TRIANGLES);
		}
		glUseProgram(0);

		glUseProgram(hudShader.getId());
		{
			hudShader.loadUniformMat4f(orthographicProjectionMatrix, "projectionMatrix", false);
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
				if(freezeUpdate)
					hud.setBackgroundColor(HUD_BG_GREY);
				else
					hud.setBackgroundColor(HUD_BG_WHITE);
				hud.update(getInfoHUDText());
				return;
			}
			case HUD_MODE_NOISE: {
				hud.setBackgroundColor(HUD_BG_YELLOW);
				hud.update(getNoiseHUDText());
				return;
			}
			case HUD_MODE_ATMOSPHERE: {
				hud.setBackgroundColor(HUD_BG_PURPLE);
				hud.update(getAtmosphereHUDText());
				return;
			}
		}
		
		throw new IllegalArgumentException();
	}
	
	private String getInfoHUDText() {
		int triangleCount = planet.getPlanetSurface().getTriangleCount();
		int totalTriangleCount = planet.getPlanetSurface().getTotalTriangleCount();
		double trianglePercentage = triangleCount * 100 / (double) totalTriangleCount;
		
		return  String.format(
				"General information\n\n"
				+ "Distance:     %.2f\n"
				+ "Triangles:    %d / %d (%.2f%%)\n"
				+ "Vertices:     %d\n"
				+ "Subdivisions: %d\n"
				+ "FPS:          %d",
				GameUtils.getDistanceBetween(planet.getPosition(), Info.camera.getPosition()), 
				triangleCount, totalTriangleCount, trianglePercentage,
				planet.getPlanetSurface().getVertexCount(),
				planet.getPlanetSurface().getSubdivisions(),
				coreEngine.timer.getFPS());
	}
	
	private String getAtmosphereHUDText() {
		Atmosphere atmosphere = planet.getAtmosphere();
		return  String.format(
				"Atmosphere properties\n\n"
				+ "Sunbrightness: %.1f\n"
				+ "Scattering:    %.4f\n"
				+ "Wavelength 1:  %.3f\n"
				+ "Wavelength 2:  %.3f\n"
				+ "Wavelength 3:  %.3f\n",
				atmosphere.getSunBrightness(),
				atmosphere.getRayleighScattering(),
				atmosphere.getWaveLengthRed(),
				atmosphere.getWaveLengthGreen(),
				atmosphere.getWaveLengthBlue());
				
	}
	
	private String getNoiseHUDText() {
		return String.format(
				"Noise properties\n\n" +
				"Seed:            %.2f\n" +
				"Wavelength:      %.2f\n" + 
				"Octaves:         %d\n" + 
				"Amplitude:       %.2f\n" 
				,
				planet.getNoiseSeed(),
				planet.getWavelength(),
				planet.getOctaves(), 
				planet.getAmplitude()
				);
	}
	
	private void printVersionInfo() {
		System.out.println("GPU Vendor: " + glGetString(GL_VENDOR));
		System.out.println("OpenGL version: " + glGetString(GL_VERSION));
		System.out.println("GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));
	}
	
	private void reset() {
		planet.setNoiseSeed(0);
		planet.resetPlanet();
		Info.camera.setPosition(CAM_START_POSITION);
		planet.setShaderMode(0);
		wireframe = false;
		freezeUpdate = false;
	}
	
	private void processKeyboardInputs(float deltaTime) {
		
		Info.camera.handleInput(deltaTime);
		
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_5)){ wireframe = !wireframe; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_9)){ freezeUpdate = !freezeUpdate; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_TAB)){ hudMode = (hudMode + 1) % 4; }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_6)){ planet.setShaderMode((planet.getShaderMode() + 1) % 4); }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_ENTER)){ planet.setNoiseSeed((float) (Math.random() * Integer.MAX_VALUE)); }
		if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_BACKSPACE)){ reset(); }
		
		switch(hudMode) {
			case HUD_MODE_NOISE: {
				if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_O))
					planet.setAmplitude(planet.getAmplitude() + 0.005f);
				else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_L))
					planet.setAmplitude(planet.getAmplitude() - 0.005f);
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_I))
					planet.setOctaves(planet.getOctaves() + 1);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_K))
					planet.setOctaves(planet.getOctaves() - 1);
				
				if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_U))
					planet.setWavelength(planet.getWavelength() + 0.005f);
				else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_J))
					planet.setWavelength(planet.getWavelength() - 0.005f);
				break;
			}
			case HUD_MODE_ATMOSPHERE: {
				Atmosphere atmosphere = planet.getAtmosphere();
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_T))
					atmosphere.setSunBrightness(atmosphere.getSunBrightness() + 0.5f);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_G))
					atmosphere.setSunBrightness(atmosphere.getSunBrightness() - 0.5f);
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_Y))
					atmosphere.setRayleighScattering(atmosphere.getRayleighScattering() + 0.0001f);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_H))
					atmosphere.setRayleighScattering(atmosphere.getRayleighScattering() - 0.0001f);
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_U))
					atmosphere.setWaveLengthRed(atmosphere.getWaveLengthRed() + 0.01f);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_J))
					atmosphere.setWaveLengthRed(atmosphere.getWaveLengthRed() - 0.01f);
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_I))
					atmosphere.setWaveLengthGreen(atmosphere.getWaveLengthGreen() + 0.01f);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_K))
					atmosphere.setWaveLengthGreen(atmosphere.getWaveLengthGreen() - 0.01f);
				
				if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_O))
					atmosphere.setWaveLengthBlue(atmosphere.getWaveLengthBlue() + 0.01f);
				else if(Keyboard.isKeyPressedWithReset(GLFW.GLFW_KEY_L))
					atmosphere.setWaveLengthBlue(atmosphere.getWaveLengthBlue() - 0.01f);
				break;
			}
		}
	}

	private void updateCamera(){
		Vector3f planetToCam = new Vector3f();
		Vector3f.sub(Info.camera.getPosition(), planet.getPosition(), planetToCam);

		float camPlanetDistance = planetToCam.length() - planet.getRadius();
		adjustCamSpeed(camPlanetDistance);
		handleCollision(planetToCam);
	}

	private void adjustCamSpeed(float camPlanetDistance) {
//		System.out.printf("%.2f / %.2f\n", slowDownRadius, camSphereDistance);
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

		float noiseSeed = planet.getNoiseSeed();
		float planetRadius = planet.getRadius();

		float actualCamDistance = planetToCam.length();
		planetToCam.normalise().scale(planetRadius);
		double noise = CustomNoise.perlinNoise(planetToCam.x + noiseSeed, planetToCam.y + noiseSeed, planetToCam.z + noiseSeed, planet.getOctaves(), planet.getLambda(planetRadius), planet.getAmplitude());

		if (noise < 0) {
			noise = 0;
		}

		float minCamDistance = (float) (planetRadius + noise);

		if (actualCamDistance < minCamDistance + CAM_COLLISION_OFFSET) {
			planetToCam.normalise();
			planetToCam.scale(-(actualCamDistance - minCamDistance) + CAM_COLLISION_OFFSET);

			Vector3f.add(cam, planetToCam, cam);
		}
	}
}
