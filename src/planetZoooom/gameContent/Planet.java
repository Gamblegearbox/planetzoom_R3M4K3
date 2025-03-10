package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.geometry.DynamicSphere;

public class Planet //implements GameObjectListener
{
	private final static float MIN_AMPLITUDE = 1;
	private final static float MIN_LAMBDA_BASE_FACTOR = 0.1f;
	private final static int MIN_OCTAVES = 1;
	private final static int MAX_OCTAVES = 10;
	private final static float MIN_MOUNTAIN_HEIGHT = 0.0214f;
	private final static int MIN_TRIANGLES = 5000;

	private Vector3f position;

	private DynamicSphere planetSurface;
	private Atmosphere atmosphere;
	private WaterSurface waterSurface;

	private float amplitude;
	private int octaves;
	private float lambdaBaseFactor;
	private float noiseSeed;
	private float mountainHeight;	
	
	private int shaderMode = 0;
	private boolean hasWater;
	
	public static final int STYLE_EARTH = 0;
	public static final int STYLE_MARS = 1;
	public static final int STYLE_DUNE = 2;
	public static final int STYLE_UNICOLOR = 3;
	
	public Planet(float radius, Vector3f position, Matrix4f modelViewMatrix)
	{
		this.position = position;
		this.planetSurface = new DynamicSphere(radius, MIN_TRIANGLES, this, modelViewMatrix);
		this.atmosphere = new Atmosphere(this);
		this.waterSurface = new WaterSurface(this);

		//sphere.addListener(this);
		resetPlanet();
	}

	public void resetPlanet() {
		lambdaBaseFactor = 0.75f;
		octaves = 3;
		amplitude = 1.77f;
		noiseSeed = 0;
		mountainHeight = MIN_MOUNTAIN_HEIGHT;
	}

	public void update(Matrix4f modelViewMatrix) {
		planetSurface.update(modelViewMatrix);
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

	public float getLambda(float planetRadius)
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