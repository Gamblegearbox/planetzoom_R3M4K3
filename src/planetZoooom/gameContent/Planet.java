package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.geometry.StaticSphere;

public class Planet
{
	public static final Vector3f BLUE = new Vector3f (0.2f, 0.6f, 0.9f);

	private Vector3f position;

	private StaticSphere planetSurface;
	private StaticSphere atmosphere;
	private StaticSphere waterSurface;
	
	private float radius;
	private boolean hasWater;
	private int subdivisions;
	
	
	public Planet(float radius, int subdivisions, Vector3f position, Matrix4f modelViewMatrix) {
		this.subdivisions = subdivisions;
		this.radius = radius;
		this.position = position;
		this.hasWater = (float) (Math.random()) > 0.5f;
		this.planetSurface = new StaticSphere(subdivisions, radius, true);
		this.atmosphere = new StaticSphere(subdivisions - 2, radius * 1.3f, false);
		this.waterSurface = new StaticSphere(subdivisions - 2, radius, false);
		
		float random = (float) (Math.random());
		setHasWater(random > 0.5f);
	}

	public void resetPlanet() {
		float random = (float) (Math.random());

		setHasWater(random > 0.5f);
		planetSurface.applyMeshModifications(radius, true);
		planetSurface.updateMeshData();
	}
	
	private void setHasWater(boolean water) {
		hasWater = water;
	}
	
	public boolean getHasWater() {
		return hasWater;
	}

	public float getRadius() {
		return radius;
	}

	public Vector3f getPosition() {
		return position;
	}

	public StaticSphere getAtmosphere() {
		return atmosphere;
	}

	public StaticSphere getWaterSurface() {
		return waterSurface;
	}

	public StaticSphere getPlanetSurface() {
		return planetSurface;
	}

	public int getSubdivisions(){
		return this.subdivisions;
	}
}