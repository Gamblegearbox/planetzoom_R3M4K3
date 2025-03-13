package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.geometry.DynamicSphere;

public class Planet
{
	public static final int STYLE_EARTH = 0;
	public static final int STYLE_MARS = 1;
	public static final int STYLE_DUNE = 2;
	public static final int STYLE_UNICOLOR = 3;
	
	private final static float MIN_AMPLITUDE = 0.01f;
	private final static float MAX_AMPLITUDE = 0.25f;
	private final static float MIN_WAVE_LENGTH = 0.1f;
	private final static int MIN_OCTAVES = 1;
	private final static int MAX_OCTAVES = 10;
	private final static int MIN_TRIANGLES = 5000;

	private Vector3f position;

	private DynamicSphere planetSurface;
	private Atmosphere atmosphere;
	private WaterSurface waterSurface;
	
	private float amplitude;
	private int octaves;
	private float wavelength;
	private float noiseSeed;
	
	private int shaderMode = 0;
	private boolean hasWater;
	private float radius;
	
	
	public Planet(float radius, Vector3f position, Matrix4f modelViewMatrix) {
		this.radius = radius;
		this.position = position;
		this.planetSurface = new DynamicSphere(radius, MIN_TRIANGLES, this, modelViewMatrix);
		this.atmosphere = new Atmosphere(this);
		this.waterSurface = new WaterSurface(this);

		resetPlanet();
	}

	public void resetPlanet() {
		setWavelength(1.0f);
		setOctaves(3);
		setAmplitude(MAX_AMPLITUDE * 0.5f);
		setNoiseSeed((float) (Math.random() * Integer.MAX_VALUE));
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
		else if (amplitude > MAX_AMPLITUDE)
			this.amplitude = MAX_AMPLITUDE;
		else
			this.amplitude = amplitude;
	}

	public int getOctaves() {
		return octaves;
	}

	public void setOctaves(int octaves) {
		if (octaves < MIN_OCTAVES)
			this.octaves = MIN_OCTAVES;
		else if(octaves > MAX_OCTAVES)
			octaves = MAX_OCTAVES;
		else
			this.octaves = octaves;
	}

	public float getWavelength() {
		return wavelength;
	}

	public void setWavelength(float wavelength) {
		if (wavelength < MIN_WAVE_LENGTH)
			this.wavelength = MIN_WAVE_LENGTH;
		else
			this.wavelength = wavelength;
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

	public float getRadius() {
		return radius;
	}

	public Vector3f getPosition() {
		return position;
	}

	public int getTotalTriangleCount() {
		return planetSurface.getTriangleCount();
	}

	public int getVertexCount() {
		return planetSurface.getVertexCount();
	}

	public Atmosphere getAtmosphere() {
		return atmosphere;
	}

	public WaterSurface getWaterSurface() {
		return waterSurface;
	}

	public DynamicSphere getPlanetSurface() {
		return planetSurface;
	}

	public float getLambda(float planetRadius) {
		return wavelength * planetRadius;
	}
			
	public void setShaderMode(int mode){
		this.shaderMode = mode;
		atmosphere.update(mode);
	}
	
	public int getShaderMode(){
		return shaderMode;
	}
}