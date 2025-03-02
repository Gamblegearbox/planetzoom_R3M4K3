package planetZoooom.gameContent;

import org.lwjgl.util.vector.Vector3f;

import planetZoooom.geometry.DynamicSphere;
import planetZoooom.interfaces.ICameraControl;
import planetZoooom.utils.CustomNoise;
import planetZoooom.utils.Info;

public class Planet //implements GameObjectListener
{
	private final static float MIN_AMPLITUDE = 1;
	private final static float MIN_LAMBDA_BASE_FACTOR = 0.1f;
	private final static int MIN_OCTAVES = 1;
	private final static int MAX_OCTAVES = 10;
	private final static float MIN_MOUNTAIN_HEIGHT = 0.0214f;
	private final static int MIN_TRIANGLES = 5000;
	final static float CAM_COLLISION_OFFSET = 200;

	private Vector3f position;

	private DynamicSphere planetSurface;
	private Atmosphere atmosphere;
	private WaterSurface waterSurface;

	private float amplitude;
	private int octaves;
	private float lambdaBaseFactor;
	private float noiseSeed;
	private float mountainHeight;	
	private final float slowDownRadius;
	private int shaderMode = 0;
	private boolean hasWater;
	
	public static final int STYLE_EARTH = 0;
	public static final int STYLE_MARS = 1;
	public static final int STYLE_DUNE = 2;
	public static final int STYLE_UNICOLOR = 3;
	
	public Planet(float radius, Vector3f position)
	{
		this.position = position;
		this.planetSurface = new DynamicSphere(radius, MIN_TRIANGLES, this);
		this.atmosphere = new Atmosphere(this);
		this.waterSurface = new WaterSurface(this);

		//sphere.addListener(this);

		resetPlanet();
		
		slowDownRadius = getRadius() * 1.15f;
	}

	public void resetPlanet() {
		lambdaBaseFactor = 0.75f;
		octaves = 3;
		amplitude = 1.77f;
		noiseSeed = 0;
		mountainHeight = MIN_MOUNTAIN_HEIGHT;
	}

	public void update() {
		planetSurface.update();

		Vector3f planetToCam = new Vector3f();
		Vector3f.sub(Info.camera.getPosition(), getPosition(), planetToCam);
		
		float camSphereDistance = planetToCam.length() - getRadius();
		adjustCamSpeed(camSphereDistance);

		handleCollision(planetToCam);
	}
	
	public void setHasWater(boolean water) {
		hasWater = water;
	}
	
	public boolean getHasWater()
	{
		return hasWater;
	}

	public float getAmplitude()
	{
		return amplitude;
	}

	public void setAmplitude(float amplitude)
	{
		if (amplitude < MIN_AMPLITUDE)
			this.amplitude = MIN_AMPLITUDE;
		else
			this.amplitude = amplitude;
	}

	public int getOctaves()
	{
		return octaves;
	}

	public void setOctaves(int octaves)
	{
		if (octaves < MIN_OCTAVES)
			this.octaves = MIN_OCTAVES;
		else if(octaves > MAX_OCTAVES)
			octaves = MAX_OCTAVES;
		else
			this.octaves = octaves;
	}

	public float getLambdaBaseFactor()
	{
		return lambdaBaseFactor;
	}

	public void setLambdaBaseFactor(float lambdaBaseFactor)
	{
		if (lambdaBaseFactor < MIN_LAMBDA_BASE_FACTOR)
			this.lambdaBaseFactor = MIN_LAMBDA_BASE_FACTOR;
		else
			this.lambdaBaseFactor = lambdaBaseFactor;
	}

	public float getNoiseSeed()
	{
		return noiseSeed;
	}

	public void setNoiseSeed(float noiseSeed)
	{
		this.noiseSeed = noiseSeed;
	}

	public void setAtmosphere(Atmosphere atmosphere)
	{
		this.atmosphere = atmosphere;
	}

	public float getMountainHeight()
	{
		return mountainHeight;
	}

	public void setMountainHeight(float mountainHeight)
	{
		if (mountainHeight < MIN_MOUNTAIN_HEIGHT)
			this.mountainHeight = MIN_MOUNTAIN_HEIGHT;
		else
			this.mountainHeight = mountainHeight;
	}

	private void adjustCamSpeed(float camSphereDistance) {
		ICameraControl camControl = Info.camera.getCameraControl();
		
//		System.out.printf("%.2f / %.2f\n", slowDownRadius, camSphereDistance);
		
		if(camSphereDistance < slowDownRadius) {
			float camSpeed = ICameraControl.MAX_CAM_SPEED / slowDownRadius * camSphereDistance;
			
			if(camSpeed < ICameraControl.MIN_CAM_SPEED)
				camSpeed = ICameraControl.MIN_CAM_SPEED;

			camControl.setVelocity(camSpeed);
		} else 
			camControl.setVelocity(ICameraControl.MAX_CAM_SPEED);
	}
	
	private void handleCollision(Vector3f planetToCam) 
	{
		Vector3f cam = Info.camera.getPosition();

		float actualCamDistance = planetToCam.length();
		planetToCam.normalise().scale(this.getRadius());

		double noise = CustomNoise.perlinNoise(planetToCam.x + noiseSeed, planetToCam.y + noiseSeed, planetToCam.z + noiseSeed, octaves, getLambda(getRadius()), amplitude);

		if (noise < 0)
			noise = 0;

		noise *= mountainHeight * getRadius();

		float minCamDistance = (float) (getRadius() + noise);

		if (actualCamDistance < minCamDistance + CAM_COLLISION_OFFSET)
		{
			planetToCam.normalise();
			planetToCam.scale(-(actualCamDistance - minCamDistance) + CAM_COLLISION_OFFSET);

			Vector3f.add(cam, planetToCam, cam);
		}
	}

	public float getRadius()
	{
		return planetSurface.getRadius();
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public int getTotalTriangleCount()
	{
		return planetSurface.getTriangleCount();
	}

	public int getVertexCount() 
	{
		return planetSurface.getVertexCount();
	}

	public Atmosphere getAtmosphere()
	{
		return atmosphere;
	}

	public WaterSurface getWaterSurface(){
		return waterSurface;
	}

	public DynamicSphere getPlanetSurface()
	{
		return planetSurface;
	}

	private float getLambda(float planetRadius)
	{
		return lambdaBaseFactor * planetRadius;
	}

//	@Override
//	public void vertexCreated(Vector3f v)
//	{
//		float planetRadius = this.getRadius();
//
//		final float lambda = lambdaBaseFactor * planetRadius;
//
//		float noise = (float) CustomNoise.perlinNoise(v.x + noiseSeed, v.y + noiseSeed, v.z + noiseSeed, octaves, lambda, amplitude);
//
//		if (noise < 0)
//			noise = 0;
//
//		// 0.14 % = 8 km von 6000 km
//		v.scale(1 + noise * mountainHeight);
//	}
			
	public void setShaderMode(int mode){
		this.shaderMode = mode;
		atmosphere.update(mode);
	}
	
	public int getShaderMode(){
		return shaderMode;
	}
}