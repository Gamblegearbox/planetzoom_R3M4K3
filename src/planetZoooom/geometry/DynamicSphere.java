package planetZoooom.geometry;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import planetZoooom.engine.MeshObject;
import planetZoooom.engine.VertexArray;
import planetZoooom.gameContent.Planet;
import planetZoooom.utils.CustomNoise;
import planetZoooom.utils.Info;

public class DynamicSphere extends MeshObject
{
	private static final int FIRST_CHECK = 3;
	private static final double VIEW_FRUSTUM_OFFSET = 1.5;
	private static final float VIEW_FRUSTUM_CHECK_OFFSET = 0.01f;
	private static final float ANGLE_TOLERANCE = (float) Math.cos((90 + 40) * Math.PI / 180);	// NEW Angle tolerance, belongs to isFacingTowardsCamera() --> OLD: private static final int ANGLE_TOLERANCE = 40;	
		
	private Planet planet;
	private int currentDepth;

	private Vector3f v1,v2,v3,n1,n2,n3;

	private Vector3f finalNormal = new Vector3f();
	private Vector3f orthoVec1 = new Vector3f();
	private Vector3f orthoVec2 = new Vector3f();
	
	private float radius;

	private int positionPointer; 
	private int triangleIndexCount;
	private int minTriangles;

	
	public DynamicSphere(float radius, int minTriangles, Planet _planet, Matrix4f modelViewMatrix) {
		vertices = new float[minTriangles * 3 * 4 * 2];
		normals = new float[vertices.length];
		uvCoords = new float[vertices.length * 2];
		indices = new int[]{};
		planet = _planet; 
		
		modelMatrix = new Matrix4f();
		v1 = new Vector3f();
		v2 = new Vector3f();
		v3 = new Vector3f();
		n1 = new Vector3f();
		n2 = new Vector3f();
		n3 = new Vector3f();
		
		positionPointer = 0;
		this.minTriangles = minTriangles;
		mesh = new VertexArray(vertices, normals, uvCoords, indices);
		this.radius = radius;
		update(modelViewMatrix);
	}
	
	public void update(Matrix4f modelViewMatrix) {						
		positionPointer = 0;

		int[] t = createOctahedron();
		triangleIndexCount = t.length;
		int depth = 0;
		while(triangleIndexCount < minTriangles * 3) {
			t = subdivide(t, triangleIndexCount, depth++, modelViewMatrix);
			if(t.length == 0)
				break;
		}
		currentDepth = depth;
		indices = t;

		mesh.update(vertices, normals, uvCoords, indices);
	}
	
	public void render(int mode) {
		mesh.render(mode);
	}
	
	public int getTriangleCount() {
		return indices.length / 3;
	}
	
	public int getVertexCount() {
		return positionPointer / 3;
	}
	
	private int[] createOctahedron() {
		int[] indices = new int[]
				{
					2,4,1,
					2,0,4,
					4,3,1,
					4,0,3,
					5,2,1,
					5,0,2,
					3,5,1,
					3,0,5
				};
		
		writePosition((Vector3f) Vertex.left().scale(radius));
		writePosition((Vector3f) Vertex.right().scale(radius));
		writePosition((Vector3f) Vertex.up().scale(radius));
		writePosition((Vector3f) Vertex.down().scale(radius));
		writePosition((Vector3f) Vertex.front().scale(radius));
		writePosition((Vector3f) Vertex.back().scale(radius));
	
		return indices;
	}
	
	private int writePosition(Vector3f pos) {
		//Lenz Edition
		createNoise(pos);
				
		orthoVec1.x = -pos.y;
		orthoVec1.y = pos.x;
		orthoVec1.z = 0;
		
		Vector3f.cross(orthoVec1, pos, orthoVec2);
		orthoVec1.normalise(orthoVec1);
		orthoVec2.normalise(orthoVec2);
		orthoVec1.scale(0.1f);
		orthoVec2.scale(0.1f);
		float orthoLength1 = orthoVec1.length();
		float orthoLength2 = orthoVec2.length();
		Vector3f.add(orthoVec1, pos, orthoVec1);
		Vector3f.add(orthoVec2, pos, orthoVec2);
		orthoVec1.normalise(orthoVec1);
		orthoVec2.normalise(orthoVec2);
		orthoVec1.scale(planet.getRadius());
		orthoVec2.scale(planet.getRadius());
		createNoise(orthoVec1);
		createNoise(orthoVec2);
		Vector3f.sub(orthoVec1, pos, orthoVec1);
		Vector3f.sub(orthoVec2, pos, orthoVec2);
		orthoVec1.scale(1/orthoLength1);
		orthoVec2.scale(1/orthoLength2);
		Vector3f.cross(orthoVec2, orthoVec1, finalNormal);
		
		this.normals[positionPointer] = finalNormal.x;
		this.vertices[positionPointer++] = pos.x;
		this.normals[positionPointer] = finalNormal.y;
		this.vertices[positionPointer++] = pos.y;
		this.normals[positionPointer] = finalNormal.z;
		this.vertices[positionPointer++] = pos.z;
		
		return (positionPointer-3) / 3;
	}

	public void createNoise(Vector3f v) {
		double lambda = planet.getWavelength() * planet.getRadius();
		double noiseSeed = planet.getNoiseSeed();
		int octaves = planet.getOctaves();
		double amplitude = planet.getAmplitude();
		
		float noise = (float) CustomNoise.perlinNoise(v.x + noiseSeed, v.y + noiseSeed, v.z + noiseSeed, octaves, lambda, amplitude);

		v.scale(1.0f + noise);
	}
		
	private float[] getPositions(int[] triangleIndices) {
		float[] triangle = new float[9];
		
		for(int i = 0; i < 9; i++)
			triangle[i] = vertices[(triangleIndices[i / 3] * 3) + (i % 3)];
		
		return triangle;
	}
	
	private int[] subdivide(int[] triangles, int triangleCount, int depth, Matrix4f modelViewMatrix) {
		int[] newTriangles = new int[triangleCount * 4];
		int trianglePointer = 0;
		int[] triangleIndices;
		int[][] childIndices;
		
		for (int i = 0; i < triangleCount; i += 3)
		{
			triangleIndices = new int[]{triangles[i], triangles[i+1], triangles[i+2]};
			
			if (depth > FIRST_CHECK)
			{
				if(!isFacingTowardsCamera(triangleIndices))
					continue;
				if (!isInViewFrustum(triangleIndices, modelViewMatrix))
					continue; // clip
			}
	
			childIndices = createChildTriangleIndices(triangleIndices, createChildVertices(triangleIndices));

			for (int j = 0; j < childIndices.length; j++)
			{
				newTriangles[trianglePointer++] = childIndices[j][0];
				newTriangles[trianglePointer++] = childIndices[j][1];
				newTriangles[trianglePointer++] = childIndices[j][2];
			}
		}
		
		this.triangleIndexCount = trianglePointer;
		return newTriangles;
	}
	
	private void setVec(Vector3f v, float x, float y, float z) {
		v.x = x;
		v.y = y;
		v.z = z;
	}

	private int[] createChildVertices(int[] triangleIndices) {	
		float[] positions = getPositions(triangleIndices);
		int[] newIndices = new int[3];
		setVec(v1, positions[0], positions[1], positions[2]);
		setVec(v2, positions[3], positions[4], positions[5]);
		setVec(v3, positions[6], positions[7], positions[8]);

		n1 = Vertex.lerp(v3, v1, 0.5f);
		n2 = Vertex.lerp(v1, v2, 0.5f);
		n3 = Vertex.lerp(v2, v3, 0.5f);
		
		n1.normalise();
		n2.normalise();
		n3.normalise();
		
		n1.scale(radius);
		n2.scale(radius);
		n3.scale(radius);
		
		newIndices[0] = writePosition(n1);
		newIndices[1] = writePosition(n2);
		newIndices[2] = writePosition(n3);
		
		return newIndices;
	} 
	
	private int[][] createChildTriangleIndices(int[] parentIndices, int[] childIndices) {
		return new int[][]
			{
				new int[] {parentIndices[0], childIndices[1], childIndices[0]},
				new int[] {childIndices[1], parentIndices[1], childIndices[2]},
				new int[] {childIndices[0], childIndices[2], parentIndices[2]},
				new int[] {childIndices[0], childIndices[1], childIndices[2]}
			};
	}

	private boolean isInViewFrustum(int[] triangleIndices, Matrix4f modelViewMatrix) {

		float[] position = getPositions(triangleIndices);

		Matrix4f p = Info.projectionMatrix;
		float x,y,z;


		float[] w = new float[3];
		for(int i = 0; i < position.length; i+=3) {
			//Object space -> world space -> camera space
			x = (modelViewMatrix.m00 * position[i]) + (modelViewMatrix.m10 * position[i+1]) + (modelViewMatrix.m20 * position[i+2]) + (modelViewMatrix.m30);
			y = (modelViewMatrix.m01 * position[i]) + (modelViewMatrix.m11 * position[i+1]) + (modelViewMatrix.m21 * position[i+2]) + (modelViewMatrix.m31);
			z = (modelViewMatrix.m02 * position[i]) + (modelViewMatrix.m12 * position[i+1]) + (modelViewMatrix.m22 * position[i+2]) + (modelViewMatrix.m32);
			
			w[i/3] = -z;
			
			//camera space -> clip space
			x = (p.m00 * x) + (p.m10 * y) + (p.m20 * z) + (p.m30);
			y = (p.m01 * x) + (p.m11 * y) + (p.m21 * z) + (p.m31);
			z = (p.m02 * x) + (p.m12 * y) + (p.m22 * z) + (p.m32);

			x/= w[i/3];
			y/= w[i/3];
		
			
			if ((x <= VIEW_FRUSTUM_OFFSET && x >= -VIEW_FRUSTUM_OFFSET) && (y <= VIEW_FRUSTUM_OFFSET && y >= -VIEW_FRUSTUM_OFFSET) && z > 0)
				return true;
								
			position[i] = x;
			position[i+1] = y;
			position[i+2] = z;
		}
		
		if(intersectsNDCPlane(position)) {
			return true;
		}
		
		if(frustumCompletlyInTriangle(position)) {
			if(position[2] > 0 && position[5] > 0 && position[8] > 0)
			return true;
		}
		return false;
	}

	private boolean intersectsNDCPlane(float[] triangle) {
		float[] x = {-1, 1, 1, -1};
		float[] y = {1, 1, -1, -1};
 
		for(int i = 0; i < 3; i++)
		{
			if(lineIntersection(x[i], y[i], x[i+1], y[i+1], triangle[0], triangle[1], triangle[3], triangle[4]))
				return true;
			if(lineIntersection(x[i], y[i], x[i+1], y[i+1], triangle[3], triangle[4], triangle[6], triangle[7]))
				return true;
			if(lineIntersection(x[i], y[i], x[i+1], y[i+1], triangle[6], triangle[7], triangle[0], triangle[1]))
				return true;
		}
		
		if(lineIntersection(x[3], y[3], x[0], y[0], triangle[0], triangle[1], triangle[3], triangle[4]))
			return true;
		if(lineIntersection(x[3], y[3], x[0], y[0], triangle[3], triangle[4], triangle[6], triangle[7]))
			return true;
		if(lineIntersection(x[3], y[3], x[0], y[0], triangle[6], triangle[7], triangle[0], triangle[1]))
			return true;
		
		return false;
	}
	
	private boolean lineIntersection(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		double d = (x0-x1)*(y2-y3) - (y0-y1)*(x2-x3);
		if(d == 0) {//parallel oder kollinear?
			return false;
		}

		double xi = ((x2-x3)*(x0*y1-y0*x1)-(x0-x1)*(x2*y3-y2*x3)) / d;
		double yi = ((y2-y3)*(x0*y1-y0*x1)-(y0-y1)*(x2*y3-y2*x3)) / d;
		
		if(inIntervall(xi, yi, x0, y0, x1, y1)) {
			if(inIntervall(xi, yi, x2, y2, x3, y3)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean inIntervall(double xi, double yi, float x0, float y0, float x1, float y1) {	
		float minX = Math.min(x0, x1);
		float maxX = Math.max(x0, x1);

		float deltaX = maxX - minX;
		
		float minY = Math.min(y0, y1);
		float maxY = Math.max(y0, y1);
		
		float deltaY = maxY - minY;
		
		if(xi <= maxX + deltaX * VIEW_FRUSTUM_CHECK_OFFSET && xi >= minX - deltaX * VIEW_FRUSTUM_CHECK_OFFSET) {
			if(yi <=maxY  + deltaY * VIEW_FRUSTUM_CHECK_OFFSET && yi >= minY - deltaY * VIEW_FRUSTUM_CHECK_OFFSET) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean frustumCompletlyInTriangle(float[] triangle) {
		float minX = Math.min(Math.min(triangle[0], triangle[3]), triangle[6]);
		float minY = Math.min(Math.min(triangle[1], triangle[4]), triangle[7]);
		float maxX = Math.max(Math.max(triangle[0], triangle[3]), triangle[6]);
		float maxY = Math.max(Math.max(triangle[1], triangle[4]), triangle[7]);
			
		return (minX <= -1 && maxX >= 1 && minY <= -1 && maxY >= 1);
	}
	
	//OLD isFacingTowardsCamera
	/*
	private boolean isFacingTowardsCamera(int[] triangleIndices) 
	{
		float[] a = getPositions(triangleIndices);
		setVec(v1, a[0], a[1], a[2]);
		
		Vector3f.sub(Info.camera.getPosition(), v1, n1);
		double angle = Vector3f.angle(n1, v1) * 180 / Math.PI;
		
		if (angle > 180)
			angle = 360 - angle;
		
		return angle < 90 + ANGLE_TOLERANCE;
	}*/
	
	//NEW isFacingTowardsCamera (Prof. Dr. Lenz edition)
	private boolean isFacingTowardsCamera(int[] triangleIndices) {
		float[] a = getPositions(triangleIndices);
		setVec(v1, a[0], a[1], a[2]);

		Vector3f.sub(Info.camera.getPosition(), v1, n1);

		float dot = Vector3f.dot(v1, n1);
		if (dot >= 0) {
			return true;
		}
		dot /= v1.length() * n1.length();
		return dot > ANGLE_TOLERANCE;
	}
		
	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	public int getSubdivisions() {
		return currentDepth;
	}
	
	public int getTotalTriangleCount() {
		int totalTriangles = 8;
		for(int i = 0; i < currentDepth; i++)
			totalTriangles = totalTriangles << 2;
				
		return totalTriangles;
	}
}