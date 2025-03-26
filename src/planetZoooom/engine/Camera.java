package planetZoooom.engine;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import static org.lwjgl.glfw.GLFW.*;

import planetZoooom.input.Cursor;
import planetZoooom.input.Keyboard;


public class Camera
{
	private Vector3f position;
	private Quaternion orientation;
	private boolean invertedYAxis;

	private static final float MOUSE_SENSITIVITY = 1.0f;
	private static final float NORMAL_CAM_SPEED = 50.0f;
	public static final float MAX_CAM_SPEED = 150.0f;

	private static final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
	private static final Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);

	//tmp stuff
	private Vector3f tmpVector1;
	private Vector3f tmpVector2;
	private Vector3f tmpVector3;
	private Vector3f tmpVecMove;
	private Vector4f tmpVecRot;
	private Quaternion tmpQuaternion;
	private Matrix4f tmpMatrix;

	private Matrix4f view;
	
	private float velocity = MAX_CAM_SPEED;
	private final static float rollSpeed = 0.025f * MAX_CAM_SPEED;
	private boolean boostEnabled;


	public Camera(Vector3f _position) {
		position = _position;

		orientation = new Quaternion();
		view = new Matrix4f();

		tmpVector1 = new Vector3f();
		tmpVector2 = new Vector3f();
		tmpVector3 = new Vector3f();
		tmpVecMove = new Vector3f();
		tmpVecRot = new Vector4f();
		tmpQuaternion = new Quaternion();
		tmpMatrix = new Matrix4f();
	}
		
	public Matrix4f getViewMatrix() {
		view = toMatrix4f(orientation);
		tmpVector1.x = -position.x;
		tmpVector1.y = -position.y;
		tmpVector1.z = -position.z;
		view.translate(tmpVector1);

		return view;
	}

	private Matrix4f toMatrix4f(Quaternion q) {
		tmpVector1.set(2.0f * (q.x * q.z - q.w * q.y), 
		2.0f * (q.y * q.z + q.w * q.x), 
		1.0f - 2.0f * (q.x * q.x + q.y * q.y));
		Vector3f forward = tmpVector1;

		tmpVector2.set(2.0f * (q.x * q.y + q.w * q.z), 
		1.0f - 2.0f * (q.x * q.x + q.z * q.z), 
		2.0f * (q.y * q.z - q.w * q.x));
		Vector3f up = tmpVector2;

		tmpVector3.set(1.0f - 2.0f * (q.y * q.y + q.z * q.z), 
		2.0f * (q.x * q.y - q.w * q.z), 
		2.0f * (q.x * q.z + q.w * q.y));
		Vector3f right = tmpVector3;
		
		tmpMatrix.setIdentity();	
		tmpMatrix.m00 = right.x;
		tmpMatrix.m10 = right.y;
		tmpMatrix.m20 = right.z;
		
		tmpMatrix.m01 = up.x;
		tmpMatrix.m11 = up.y;
		tmpMatrix.m21 = up.z;
		
		tmpMatrix.m02 = forward.x;
		tmpMatrix.m12 = forward.y;
		tmpMatrix.m22 = forward.z;

		return tmpMatrix;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void addYaw(float amount)
	{
		amount *= MOUSE_SENSITIVITY;
		
		Quaternion b = tmpQuaternion.setIdentity();
		tmpVecRot.set(0, 1, 0, amount);
		b.setFromAxisAngle(tmpVecRot);
		Quaternion.mul(b, orientation, orientation);

		orientation.normalise();
	}
	
	public void addPitch(float amount)
	{
		amount *= MOUSE_SENSITIVITY;
		
		Quaternion rotationQuaternion = tmpQuaternion.setIdentity();
		if(invertedYAxis) {
			tmpVecRot.set(-1, 0, 0, amount);
			rotationQuaternion.setFromAxisAngle(tmpVecRot);
		}
		else {
			tmpVecRot.set(1, 0, 0, amount);
			rotationQuaternion.setFromAxisAngle(tmpVecRot);
		}
		Quaternion.mul(rotationQuaternion, orientation, orientation);

		orientation.normalise();
	}
	
	public void addRoll(float amount) {
		Quaternion rotationQuaternion = tmpQuaternion.setIdentity();

		tmpVecRot.set(0, 0, 1, amount);
		rotationQuaternion.setFromAxisAngle(tmpVecRot);

		Quaternion.mul(rotationQuaternion, orientation, orientation);

		orientation.normalise();
	}
	
	public Vector3f getLookAt()
	{
		tmpVector1.set(0, 0, -1);
		Vector3f lookAt = calculateMovementVector(tmpVector1);
		lookAt.normalise();
		return lookAt;
	}
	
	public void moveForward(float amount) {		
		tmpVecMove.set(0, 0, -amount);	
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}
	
	public void moveBackward(float amount) {
		tmpVecMove.set(0, 0, amount);
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}
	
	public void strafeLeft(float amount) {
		tmpVecMove.set(-amount, 0f, 0f);
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}
	
	public void strafeRight(float amount) {
		tmpVecMove.set(amount, 0f, 0f);
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}
	
	public void moveUp(float amount) {
		tmpVecMove.set(0f, amount, 0f);
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}
	
	public void moveDown(float amount) {
		tmpVecMove.set(0f, -amount, 0f);
		Vector3f movement = calculateMovementVector(tmpVecMove);
		Vector3f.add(position, movement, position);
	}

	public Vector3f getLocalUpVector() {
		return calculateMovementVector(up);
	}
	
	public Vector3f getLocalRightVector() {
		return calculateMovementVector(right);	
	}
	
	public boolean isInvertedYAxis() {
		return invertedYAxis;
	}

	public void setInvertedYAxis(boolean invertedYAxis) {
		this.invertedYAxis = invertedYAxis;
	}
	
	private Vector3f calculateMovementVector(Vector3f movement) {
		tmpQuaternion.setIdentity();
		Quaternion.negate(orientation, tmpQuaternion);
		
		tmpVector3.set(tmpQuaternion.x, tmpQuaternion.y, tmpQuaternion.z);

		Vector3f.cross(tmpVector3, movement, tmpVector1);
		Vector3f.cross(tmpVector3, tmpVector1, tmpVector2);
		
		tmpVector1.scale(2 * tmpQuaternion.w);
		tmpVector2.scale(2);	
		
		Vector3f.add(movement, tmpVector1, movement);
		Vector3f.add(movement, tmpVector2, movement);

		return movement;
	}


	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void update(float deltaTime) {
		boostEnabled = Keyboard.isKeyPressed(GLFW_KEY_LEFT_SHIFT);

		if(boostEnabled)
			velocity = MAX_CAM_SPEED;
		else
			velocity = NORMAL_CAM_SPEED;
		
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

}