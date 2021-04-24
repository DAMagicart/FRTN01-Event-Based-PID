
import javax.swing.JPanel;

import se.lth.control.BoxPanel;
import se.lth.control.MainFrame;

public class ReferenceGenerator extends Thread {
	private static final int MANUAL = 0, SQUARE = 1;
	private final int priority;
	private double amplitude;
	private double period;
	private double sign = -1;
	private double manual;
	
	private class RefGUI{
		private BoxPanel refPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private JPanel sliderPanel = new JPanel();
		private double amplitude;
		
		
		
		private double period; // Behövs denna?
		
		
		//Constructor
		private RefGUI(double amplitude,double period) {
			MainFrame.showLoading();
			this.amplitude = amplitude;
			this.period = period;
			
			
		}
	}
	
	
	//Constructor
	public ReferenceGenerator(int refGenPriority) {
		priority = refGenPriority;
		amplitude = 5;
		period = 15;
		manual = 0.0;
		
		new RefGUI(amplitude,period);
	}
	
}
