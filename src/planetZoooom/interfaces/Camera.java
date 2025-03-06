package planetZoooom.interfaces;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public interface Camera {
	public Matrix4f getViewMatrix();
	public Vector3f getPosition();
	public Vector3f getLookAt();
	public Vector3f getLocalRightVector();
	public Vector3f getLocalUpVector();
	public void setPosition(Vector3f position);
	public void handleInput(float deltaTime);
	public void setVelocity(float velocity);
}
