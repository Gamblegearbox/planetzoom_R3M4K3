package planetZoooom.geometry;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.MeshObject;
import planetZoooom.engine.VertexArray;
import planetZoooom.gameContent.Colors;

public class Polystrip extends MeshObject
{	
	private static final int VERTS = 4;

	public Polystrip(int subdivisions, float size, boolean deforms) {
		int resolution = 1 << subdivisions;
		
		this.position = new Vector3f(0.0f, 0.0f, 0.0f);
		this.modelMatrix = new Matrix4f().translate(position);

		vertices = new float[VERTS * 3]; 
		colors = new float[VERTS * 4];
		indices = new int[6];
		
		createVertices(size, resolution);
		createNormals();
		//applyMeshModifications(radius, deforms);

		mesh = new VertexArray(vertices, colors, indices);
	}

	private void createVertices(float size, int resolution) {
		
		//0
		vertices[0]  = -size;
		vertices[1]  = 0f;
		vertices[2]  = -size;

		//1
		vertices[3]  = size;
		vertices[4]  = 0f;
		vertices[5]  = -size;

		//2
		vertices[6]  = -size;
		vertices[7]  = 0f;
		vertices[8]  = size;

		//3
		vertices[9]  = size;
		vertices[10] = 0f;
		vertices[11] = size;

		indices[0] = 0;
		indices[1] = 2;
		indices[2] = 1;
		indices[3] = 2;
		indices[4] = 3;
		indices[5] = 1;
		
		for (int i = 0; i < colors.length; i += 4) {
			colors[i+0] = Colors.GREEN[0];
			colors[i+1] = Colors.GREEN[1];
			colors[i+2] = Colors.GREEN[2];
			colors[i+3] = Colors.GREEN[3];
		}

	}

	public void createNormals() {
		normals = new float[4 * 3];

		for (int i = 0; i < 4; i++) {
			normals[i * 3    ] = 0.0f;
			normals[i * 3 + 1] = 1.0f;
			normals[i * 3 + 2] = 0.0f;
		}
	}
	
	private void createUVs() {
		for (int i = 0; i < 4; i++) {
			uvCoords[i * 2    ] = vertices[i * 3];
			uvCoords[i * 2 + 1] = vertices[i * 3 + 1];
		}
	}

	public void applyMeshModifications(float radius, boolean deforms) {

	}

	public void updateMeshData(){
		mesh.update(vertices, normals, uvCoords, indices);
	}
	
	public void render(int mode){
		mesh.render(mode);
	}
	
}
