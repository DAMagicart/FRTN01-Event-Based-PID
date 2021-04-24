import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import se.lth.control.BoxPanel;
import se.lth.control.MainFrame;

public class ReferenceGenerator extends Thread {
	private static final int MANUAL = 0, SQUARE = 1;
	private final int priority;
	private double amplitude;
	private double period;
	private double sign = -1;
	private double manual;
	private double ref;
	private double max_ctrl;
	private int mode = MANUAL;

	private class RefGUI {
		private BoxPanel refPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private JPanel sliderPanel = new JPanel();
		private BoxPanel buttonsPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel rightPanel = new JPanel();

		private JRadioButton manButton = new JRadioButton("Manual");
		private JRadioButton sqButton = new JRadioButton("Square");
		private JRadioButton toButton = new JRadioButton("Time-optimal");
		private JSlider slider = new JSlider(JSlider.VERTICAL, -10, 10, 0);

		// Constructor
		private RefGUI(double amplitude, double period) {
			MainFrame.showLoading();
			// this.amplitude = amplitude;
			// this.period = period;
			buttonsPanel.setBorder(BorderFactory.createEtchedBorder());
			buttonsPanel.add(manButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(sqButton);
			ButtonGroup group = new ButtonGroup();
			group.add(manButton);
			group.add(sqButton);
			manButton.setSelected(true);

			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(buttonsPanel, BorderLayout.CENTER);

			slider.setPaintTicks(true);
			slider.setMajorTickSpacing(5);
			slider.setMinorTickSpacing(2);
			slider.setLabelTable(slider.createStandardLabels(10));
			slider.setPaintLabels(true);
			sliderPanel.setBorder(BorderFactory.createEtchedBorder());
			sliderPanel.add(slider);

			refPanel.add(sliderPanel);
			refPanel.addGlue();
			refPanel.add(rightPanel);

			manButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setManMode();
				}
			});
			sqButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSqMode();
				}
			});
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (!slider.getValueIsAdjusting()) {
						setManual(slider.getValue());
					}
				}
			});

			MainFrame.setPanel(refPanel, "RefGen");

		}
	}

	// Constructor
	public ReferenceGenerator(int refGenPriority) {
		priority = refGenPriority;
		amplitude = 5;
		period = 15;
		manual = 0.0;
		ref = Math.PI;
		max_ctrl = 10;

		new RefGUI(amplitude, period);
	}

	//?
	private synchronized void setRef(double newRef) {
		ref = newRef;
	}

	private synchronized void setManual(double newManual) {
		manual = newManual;
	}

	private synchronized void setSqMode() {
		mode = SQUARE;
	}

	private synchronized void setManMode() {
		mode = MANUAL;
	}

	public synchronized double getRef() {
		return (mode == MANUAL) ? manual : ref;
	}

	public void run() {
		long h = 10;
		long timebase = System.currentTimeMillis();
		long timeleft = 0;
		long duration;

		double setpoint = 0.0;
		double new_setpoint;
		double now;

		setPriority(priority);

		try {
			while (!isInterrupted()) {
				now = 0.001 * (double) timebase;
				synchronized (this) {
					if (mode == MANUAL) {
						setpoint = manual;
						ref = manual;
					} else {
						timeleft -= h;
						//if (getParChanged()) {
							//timeleft = 0;
						//}
						
						if (timeleft <= 0) {
							timeleft += (long) (500.0 * period);
							sign = -sign;
						}
						new_setpoint = amplitude * sign;
						if (new_setpoint != setpoint) {
							if (mode == SQUARE) {
								setpoint = new_setpoint;
								ref = setpoint;
							}
						}
					}
				}
				timebase += h;
				duration = timebase - System.currentTimeMillis();
				if (duration > 0) {
					sleep(duration);
				}
			}
		} catch (InterruptedException e) {
			// Requested to stop
		}
	}

}