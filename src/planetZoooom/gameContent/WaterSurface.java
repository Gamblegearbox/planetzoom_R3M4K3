package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.geometry.StaticSphere;
import planetZoooom.graphics.ShaderProgram;


public class WaterSurface 
{
	private Matrix4f modelMatrix;
	private StaticSphere sphere;
	private Vector3f position;
	
	
	private static final int SPHERE_SUBDIVISIONS = 4; 

	public static final Vector3f BLUE = new Vector3f (0.2f, 0.6f, 0.9f);
	public static final Vector3f RED = new Vector3f(0.6f, 0.4f, 0.2f);
	
	public WaterSurface(Planet planet) {
		sphere = new StaticSphere(SPHERE_SUBDIVISIONS, planet.getRadius());
		position = planet.getPosition();
		modelMatrix = new Matrix4f().translate(position);
	}
		
	public Vector3f getPosition()
	{
		return position;
	}
	
	public StaticSphere getSphere()
	{
		return sphere;
	}
	
	public Matrix4f getModelMatrix()
	{
		return modelMatrix;
	}

}