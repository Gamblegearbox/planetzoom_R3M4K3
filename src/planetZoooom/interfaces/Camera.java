package planetZoooom.interfaces;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public interface Camera
{
	public Matrix4f getViewMatrix();
	public CameraControl getCameraControl();
	public Vector3f getPosition();
	public Vector3f getLookAt();
	public Vector3f getLocalRightVector();
	public Vector3f getLocalUpVector();
	public void setPosition(Vector3f position);
}
