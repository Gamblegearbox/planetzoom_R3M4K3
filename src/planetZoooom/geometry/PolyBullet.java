package planetZoooom.geometry;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.MeshObject;
import planetZoooom.engine.VertexArray;
import planetZoooom.gameContent.Colors;

public class PolyBullet extends MeshObject
{	
	private static final int VERTS = 2;
	private static final float BULLET_SPEED = 3500f;
	private static final float BULLET_RANGE = -2000f;
	private static final float DROP_RATE_MOD = 250;
	

	private boolean isActive;
	private float dropRate;

	public PolyBullet(float size) {		
		isActive = false;
		dropRate = (float) Math.random() * 500f;
		modelMatrix = new Matrix4f();
		modelMatrix.setIdentity();

		position = new Vector3f(0.0f, 5.0f, 0.0f);
		setPosition(position);

		indices = new int[2];
		vertices = new float[VERTS * 3]; 
		colors = new float[VERTS * 4];

		createVertices(size);
		createColors();

		mesh = new VertexArray(vertices, colors, indices);
	}

	private void createVertices(float size) {

		//0
		vertices[0]  = 0f;
		vertices[1]  = 0f;
		vertices[2]  = 0f;

		//1
		vertices[3]  = 0f;
		vertices[4]  = 0f;
		vertices[5]  = size;

		indices[0] = 0;
		indices[1] = 1;		
	}

	private void createColors() {
		// 0
		colors[0]  = Colors.RED[0];
		colors[1]  = Colors.RED[1];
		colors[2]  = Colors.RED[2];
		colors[3]  = Colors.RED[3];

		// 1
		colors[4]  = Colors.YELLOW[0];
		colors[5]  = Colors.YELLOW[1];
		colors[6]  = Colors.YELLOW[2];
		colors[7]  = Colors.YELLOW[3];
	}
	
	public void update(float deltaTime) {
		if (isActive && position.z > BULLET_RANGE) {
			position.z -= BULLET_SPEED * deltaTime;
			if (position.z < BULLET_RANGE * 0.5){
				dropRate += deltaTime * 0.5;
				position.y -= dropRate * DROP_RATE_MOD * deltaTime;
			}
		}
		else {
			isActive = false;
			dropRate = (float) Math.random();
			position.x = 0f;
			position.y = 5f;
			position.z = 100.0f; 
		}
		
		setPosition(position);
	}

	public void setIsActive(Vector3f startPos) {
		position.set(startPos);
		this.isActive = true;
	}

	public boolean getIsActive() {
		return this.isActive;
	}

	public void render(int mode){
		mesh.render(mode);
	}
	
}
