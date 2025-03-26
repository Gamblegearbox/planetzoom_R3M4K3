package planetZoooom.geometry;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.MeshObject;
import planetZoooom.engine.VertexArray;
import planetZoooom.gameContent.Colors;
import planetZoooom.input.Keyboard;

public class PlayerShip extends MeshObject
{	
	private static final int VERTS = 5;
	private static final float SPEED = 100.0f;

	public PlayerShip() {		
		position = new Vector3f(0.0f, 10.0f, 0.0f);
		modelMatrix = new Matrix4f().translate(position);

		indices = new int[18];
		vertices = new float[VERTS * 3]; 
		colors = new float[VERTS * 4];

		createVertices();
		createColors();

		mesh = new VertexArray(vertices, colors, indices);
	}

	private void createVertices() {
		float size = 50.0f;
		float front = size / 4f * 1f;
		float side = front / 4f;
		float rear = size / 4f * 3f;

		//0
		vertices[0]  = 0f;
		vertices[1]  = 0f;
		vertices[2]  = -front;

		//1
		vertices[3]  = 0f;
		vertices[4]  = side;
		vertices[5]  = 0f;

		//2
		vertices[6]  = side;
		vertices[7]  = -side;
		vertices[8]  = 0f;

		//3
		vertices[9]  = -side;
		vertices[10] = -side;
		vertices[11] = 0f;

		//4
		vertices[12] = 0f;
		vertices[13] = 0f;
		vertices[14] = rear;

		// front
		indices[0] = 0;
		indices[1] = 2;
		indices[2] = 1;
		
		indices[3] = 0;
		indices[4] = 1;
		indices[5] = 3;

		indices[6] = 0;
		indices[7] = 3;
		indices[8] = 2;

		// rear
		indices[9] = 1;
		indices[10] = 2;
		indices[11] = 4;

		indices[12] = 2;
		indices[13] = 3;
		indices[14] = 4;

		indices[15] = 1;
		indices[16] = 4;
		indices[17] = 3;
		
	}

	private void createColors() {
		// 0
		colors[0]  = Colors.RED[0];
		colors[1]  = Colors.RED[1];
		colors[2]  = Colors.RED[2];
		colors[3]  = Colors.RED[3];

		// 1
		colors[4]  = Colors.WHITE[0];
		colors[5]  = Colors.WHITE[1];
		colors[6]  = Colors.WHITE[2];
		colors[7]  = Colors.WHITE[3];

		// 2
		colors[8]  = Colors.WHITE[0];
		colors[9]  = Colors.WHITE[1];
		colors[10] = Colors.WHITE[2];
		colors[11] = Colors.WHITE[3];

		// 3
		colors[12] = Colors.WHITE[0];
		colors[13] = Colors.WHITE[1];
		colors[14] = Colors.WHITE[2];
		colors[15] = Colors.WHITE[3];

		// 4
		colors[16] = Colors.WHITE[0];
		colors[17] = Colors.WHITE[1];
		colors[18] = Colors.WHITE[2];
		colors[19] = Colors.WHITE[3];
	}
	
	public void update(float deltaTime) {

		if(Keyboard.isKeyPressed(GLFW_KEY_LEFT))
			position.x -= (SPEED * deltaTime);

		if(Keyboard.isKeyPressed(GLFW_KEY_RIGHT))
			position.x += (SPEED * deltaTime);

		if(Keyboard.isKeyPressed(GLFW_KEY_UP))
			position.y += (SPEED * deltaTime);
		
		if(Keyboard.isKeyPressed(GLFW_KEY_DOWN))
			position.y -= (SPEED * deltaTime);

		setPosition(position);
	}

	public void render(int mode){
		mesh.render(mode);
	}
	
}
