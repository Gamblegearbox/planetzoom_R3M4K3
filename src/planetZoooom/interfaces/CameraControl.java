package planetZoooom.interfaces;


public interface CameraControl 
{
	public final static float MIN_CAM_SPEED = 0.05f;
	public final static float MAX_CAM_SPEED = 5f;
	
	public Camera handleInput(int deltaTime);
	public void setVelocity(float velocity);
}
