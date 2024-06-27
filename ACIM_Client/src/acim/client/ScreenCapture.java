package acim.client;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import java.io.*;

import javax.imageio.*;

public class ScreenCapture {
	// https://stackoverflow.com/a/9417836
	public static byte[] getScreencapBytes() throws HeadlessException, AWTException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage img;
		
		try {
			// Try taking a screenshot first.
			
			Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
			img = new Robot().createScreenCapture(new Rectangle(screen_size));
		} catch (Exception e) {
			// Display the exception to an image
			// so that the server owner knows the potential cause of the Exception.
			
			String errorMsg = "Unable to capture screenshot.\n\n"
					+ "This might be due to temporary issues or security settings on this computer.\n"
					+ "Please try again later. If the problem persists, contact your IT department\n"
					+ "for assistance.\n\n"
					+ e.getClass().getSimpleName() + ": \n" + e.getLocalizedMessage();
			
			Font font = new Font("Arial", Font.PLAIN, 16);
			int width = 4, height = 4;
			
			// Create a blank BufferedImage for getFontMetrics to work.
			img = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = img.getGraphics();
			
			// Calculate the width and height of the image first.
			for (String line : errorMsg.split("\n")) {
				Rectangle2D rect = g.getFontMetrics(font).getStringBounds(line, g);
				
				width = Math.max(width, (int) rect.getWidth());
				height += rect.getHeight();
			}
			// Add one more to prevent the image to look chopped off.
			height += g.getFontMetrics(font).getHeight();

			img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			g = img.getGraphics();
			
			g.setColor(new Color(126, 239, 0));
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			g.setColor(Color.BLACK);
			g.setFont(font);
			
			int lineNumber = 1;
			for (String line : errorMsg.split("\n")) {
	            g.drawString(line, 2, 2 + lineNumber * g.getFontMetrics().getHeight());
	            lineNumber++;
			}
		}
		try {
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
