package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import static org.lwjgl.glfw.GLFW.*;

import planetZoooom.input.Cursor;
import planetZoooom.input.Keyboard;
import planetZoooom.interfaces.Camera;


public class FreeCamera implements Camera
{
	private Vector3f position;
	private Quaternion orientation;
	private boolean invertedYAxis;

	private static final float MOUSE_SENSITIVITY = 1.0f;
	private static final float CAM_BOOST_FACTOR = 2.5f;
	public static final float MIN_CAM_SPEED = 5.0f;
	public static final float MAX_CAM_SPEED = 50.0f;
	public static final float MAX_CAM_RANGE = 500.0f;

	private Matrix4f view;
	
	//private FreeCamera cam;
	private float velocity = MAX_CAM_SPEED;
	private final static float rollSpeed = 0.025f * MAX_CAM_SPEED;
	private boolean boostEnabled;

	public FreeCamera(Vector3f _position) {
		position = _position;

		orientation = new Quaternion();
		view = new Matrix4f();
		
		//cameraControl = new FreeCameraControl(this);
	}
	
	public void rotate(Vector3f axis, float theta) {
		Quaternion r = new Quaternion();
		
		r.x = (float) (axis.x * Math.sin(theta / 2.0f));
		r.y = (float) (axis.y * Math.sin(theta / 2.0f));
		r.z = (float) (axis.z * Math.sin(theta / 2.0f));
		r.w = (float) Math.cos(theta / 2.0f);	
	}
	
	public Matrix4f getViewMatrix()
	{
		view = toMatrix4f(orientation);
		
		view.translate(new Vector3f(-position.x, -position.y, -position.z));

		return view;
	}

	private Matrix4f toMatrix4f(Quaternion q)
	{
		Vector3f forward =  new Vector3f(
				2.0f * (q.x * q.z - q.w * q.y), 
				2.0f * (q.y * q.z + q.w * q.x), 
				1.0f - 2.0f * (q.x * q.x + q.y * q.y));
		Vector3f up = new Vector3f(
				2.0f * (q.x * q.y + q.w * q.z), 
				1.0f - 2.0f * (q.x * q.x + q.z * q.z), 
				2.0f * (q.y * q.z - q.w * q.x));
		Vector3f right = new Vector3f(
				1.0f - 2.0f * (q.y * q.y + q.z * q.z), 
				2.0f * (q.x * q.y - q.w * q.z), 
				2.0f * (q.x * q.z + q.w * q.y));
		
		Matrix4f matrix = new Matrix4f();
		
		matrix = new Matrix4f();
		matrix.setIdentity();	
		matrix.m00 = right.x;
		matrix.m10 = right.y;
		matrix.m20 = right.z;
		
		matrix.m01 = up.x;
		matrix.m11 = up.y;
		matrix.m21 = up.z;
		
		matrix.m02 = forward.x;
		matrix.m12 = forward.y;
		matrix.m22 = forward.z;

		return matrix;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void addYaw(float amount)
	{
		amount *= MOUSE_SENSITIVITY;
		
		Quaternion b = new Quaternion();
		b.setFromAxisAngle(new Vector4f(0, 1, 0, amount));
		Quaternion.mul(b, orientation, orientation);

		orientation.normalise();
	}
	
	public void addPitch(float amount)
	{
		amount *= MOUSE_SENSITIVITY;
		
		Quaternion rotationQuaternion = new Quaternion();
		if(invertedYAxis)
			rotationQuaternion.setFromAxisAngle(new Vector4f(-1, 0, 0, amount));
		else
			rotationQuaternion.setFromAxisAngle(new Vector4f(1, 0, 0, amount));
		Quaternion.mul(rotationQuaternion, orientation, orientation);

		orientation.normalise();
	}
	
	public void addRoll(float amount)
	{
		Quaternion rotationQuaternion = new Quaternion();

		rotationQuaternion.setFromAxisAngle(new Vector4f(0, 0, 1, amount));

		Quaternion.mul(rotationQuaternion, orientation, orientation);

		orientation.normalise();
	}
	
	public Vector3f getLookAt()
	{
		Vector3f lookAt = calculateMovementVector(new Vector3f(0, 0, -1));
		lookAt.normalise();
		return lookAt;
	}
	
	public void moveForward(float amount) {			
		Vector3f movement = calculateMovementVector(new Vector3f(0, 0, -amount));
		Vector3f.add(position, movement, position);
	}
	
	public void moveBackward(float amount) {
		Vector3f movement = calculateMovementVector(new Vector3f(0, 0, amount));
		Vector3f.add(position, movement, position);
	}
	
	public void strafeLeft(float amount) {
		Vector3f movement = calculateMovementVector(new Vector3f(-amount, 0, 0));
		Vector3f.add(position, movement, position);
	}
	
	public void strafeRight(float amount) {
		Vector3f movement = calculateMovementVector(new Vector3f(amount, 0, -0));
		Vector3f.add(position, movement, position);
	}
	
	public void moveUp(float amount) {
		Vector3f movement = calculateMovementVector(new Vector3f(0, amount, 0));
		Vector3f.add(position, movement, position);
	}
	
	public void moveDown(float amount) {
		Vector3f movement = calculateMovementVector(new Vector3f(0, -amount, 0));
		Vector3f.add(position, movement, position);
	}

	public Vector3f getLocalUpVector() {
		return calculateMovementVector(new Vector3f(0.0f, 1.0f, 0.0f));
	}
	
	public Vector3f getLocalRightVector() {
		return calculateMovementVector(new Vector3f(1.0f, 0.0f, 0.0f));	
	}
	
	public boolean isInvertedYAxis() {
		return invertedYAxis;
	}

	public void setInvertedYAxis(boolean invertedYAxis) {
		this.invertedYAxis = invertedYAxis;
	}
	
	private Vector3f calculateMovementVector(Vector3f movement) {
		Quaternion inverse = new Quaternion();
		Quaternion.negate(orientation, inverse);
		
		Vector3f quatVector = new Vector3f(inverse.x, inverse.y, inverse.z);

		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f.cross(quatVector, movement, v1);
		Vector3f.cross(quatVector, v1, v2);
		
		v1.scale(2 * inverse.w);
		v2.scale(2);	
		
		Vector3f.add(movement, v1, movement);
		Vector3f.add(movement, v2, movement);
		return movement;
	}

	@Override
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	@Override
	public void handleInput(float deltaTime) {
		boostEnabled = Keyboard.isKeyPressed(GLFW_KEY_LEFT_SHIFT);

		if(boostEnabled)
			velocity *= CAM_BOOST_FACTOR;
		
		if(Keyboard.isKeyPressed(GLFW_KEY_W))
			moveForward(velocity * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_S))
			moveBackward(velocity * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_A))
			strafeLeft(velocity * deltaTime);

		if(Keyboard.isKeyPressed(GLFW_KEY_D))
			strafeRight(velocity * deltaTime);
			
		if(Keyboard.isKeyPressed(GLFW_KEY_SPACE))
			moveUp(velocity * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_LEFT_CONTROL))
			moveDown(velocity * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_Q))
			addRoll(-rollSpeed * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_E))
			addRoll(rollSpeed * deltaTime);
		
		addYaw((float) Cursor.getDx() / 250.0f);
		addPitch((float) Cursor.getDy() / 250.0f);
	}

	@Override
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}
}