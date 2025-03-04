package planetZoooom.utils;

import org.lwjgl.util.vector.Matrix4f;
import planetZoooom.gameContent.Planet;
import planetZoooom.interfaces.Camera;

public class Info
{
	public static Planet planet; //where is this needed - only one instance?
	public static Camera camera;
	
	//public static Matrix4f viewMatrix;
	public static Matrix4f projectionMatrix;
}