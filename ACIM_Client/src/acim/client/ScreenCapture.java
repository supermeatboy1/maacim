package acim.client;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import javax.imageio.*;

public class ScreenCapture {
	// https://stackoverflow.com/a/9417836
	public static byte[] getScreencapBytes() throws HeadlessException, AWTException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Image img;
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension scaled_size = new Dimension((int) (screen_size.width * 0.9f),
												(int) (screen_size.height * 0.9f));
		BufferedImage scaled_img = new BufferedImage(scaled_size.width, scaled_size.height, BufferedImage.TYPE_3BYTE_BGR);
		
		img = new Robot().createScreenCapture(
				new Rectangle(screen_size)
			).getScaledInstance(scaled_size.width, scaled_size.height, Image.SCALE_SMOOTH);
		
		Graphics2D g2d = scaled_img.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		
		try {
			ImageIO.write(scaled_img, "png", baos);
			baos.flush();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
