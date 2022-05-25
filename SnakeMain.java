import java.awt.Component;
import javax.swing.JFrame;

public class SnakeMain {
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(3);
		f.setTitle("Snake");
		f.setResizable(false);
		f.add(new Snake(), "Center");
		f.pack();
		f.setLocationRelativeTo((Component)null);
		f.setVisible(true);
	}
}
