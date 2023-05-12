package kun;
import javax.microedition.lcdui.Font;

public interface UIConstants {

	public static final Font smallfont = Font.getFont(0, 0, Font.SIZE_SMALL);
	public static final Font mediumfont = Font.getFont(0, 0, Font.SIZE_MEDIUM);
	public static final Font mediumboldfont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
	public static final Font largefont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
	
	public static final int smallfontheight = smallfont.getHeight();
	public static final int mediumfontheight = mediumfont.getHeight();
	public static final int mediumboldfontheight = mediumboldfont.getHeight();
	public static final int largefontheight = largefont.getHeight();
	
	public static final int COLOR_MAINBG = 1;

}
