package planetZoooom.gameContent;

import org.lwjgl.util.vector.Matrix4f;

import planetZoooom.geometry.Rectangle2D;
import planetZoooom.geometry.HUDText;

public class HeadsUpDisplay
{
	private Matrix4f modelMatrix = new Matrix4f();
	
	private static final int STANDARD_POSITION_X = 0;
	private static final int STANDARD_POSITION_Y = 0;
	private static final String  STANDARD_FONT = "arial_nm.png";
	private static final int CONTENT_MARGIN = 20;
	
	private int position_x;
	private int position_y;
	private String font;
	private HUDText text;
	private Rectangle2D background;
	
	public HeadsUpDisplay() {
		position_x = STANDARD_POSITION_X;
		position_y = STANDARD_POSITION_Y;
		font = STANDARD_FONT;
		this.text = new HUDText("", font, position_x, position_y, 16);
	}
	
	public HeadsUpDisplay(int x, int y, String font) {
		position_x = x;
		position_y = y;
		this.font = font;
		this.text = new HUDText("", font, position_x + CONTENT_MARGIN, position_y + CONTENT_MARGIN, 16);

		float[][] bgColors = new float[][] {
			Colors.WHITE,
			Colors.BLUE,
			Colors.GREEN,
			Colors.YELLOW
		};
		this.background = new Rectangle2D(position_x, position_y, 200, 40, bgColors);
	}
	
	public void update(String text) {
		this.text.update(text);
	}
	
	public HUDText getTextMesh() {
	    return text;
	}
	
	public Rectangle2D getBackgroundMesh() {
		return background;
	}
	
	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}
}