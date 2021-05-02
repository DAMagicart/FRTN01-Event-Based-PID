import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;

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
	private Regul regul;

	private class RefGUI {
		private JFrame mFrame;
		private BoxPanel refPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private JPanel sliderPanel = new JPanel();
		private BoxPanel buttonsPanel = new BoxPanel(BoxPanel.VERTICAL);

		// Initierar det som behövs för att ändra variablerna för squareWave:
		private BoxPanel fieldPanel = new BoxPanel(BoxPanel.VERTICAL);
		private BoxPanel labelPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel varPanel = new JPanel();

		private JPanel rightPanel = new JPanel();

		// Skapar knappar som styr över om ref generatorn ger en fyrkantsvåg eller om
		// man justerar den manuellt med en slider.
		private JRadioButton manButton = new JRadioButton("Manual");
		private JRadioButton sqButton = new JRadioButton("Square");

		private boolean noise = false;
		private boolean load = false;
		private JButton noiseButton = new JButton("Measurement Noise OFF");
		private JButton lDistButton = new JButton("Load Disturbance OFF");
		// private JRadioButton toButton = new JRadioButton("Time-optimal"); // Behövs
		// denna?

		// Skapar en slider:
		private JSlider slider = new JSlider(JSlider.VERTICAL, -10, 10, 0);

		// Skapar två variabelfält för att kunna ändra variablerna live.
		private DoubleField periodVar = new DoubleField(5, 3); // Oklart varför det ska vara precis 5,3 här. Taget från
																// Opcom.
		private DoubleField amplitudeVar = new DoubleField(5, 3); // Oklart varför det ska vara precis 5,3 här. Taget
																	// från Opcom.
		private JButton applyVars = new JButton("Apply new parameters");

		private boolean ampChanged = false;
		private boolean periodChanged = false;

		// Constructor
		private RefGUI(double start_amplitude, double start_period) {

			// Bytt ut mainframe mot en JPanel som heter mainFrame
			// MainFrame.showLoading();
			mFrame = new JFrame("Reference Generator");

			// this.amplitude = amplitude;
			// this.period = period;

			// Skapar delen med knappar för att välja om man vill använda slidern
			// eller köra en förutbestämd square wave.
			buttonsPanel.setBorder(BorderFactory.createEtchedBorder());
			buttonsPanel.add(manButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(sqButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(noiseButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(lDistButton);
			ButtonGroup group = new ButtonGroup();
			group.add(manButton);
			group.add(sqButton);
			group.add(noiseButton);
			group.add(lDistButton);
			manButton.setSelected(true);

			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(buttonsPanel, BorderLayout.CENTER);

			// Skapar den vänstra delen av GUIt, alltså slider och grejer.
			slider.setPaintTicks(true);
			slider.setMajorTickSpacing(5);
			slider.setMinorTickSpacing(2);
			slider.setLabelTable(slider.createStandardLabels(10));
			slider.setPaintLabels(true);
			sliderPanel.setBorder(BorderFactory.createEtchedBorder());
			sliderPanel.add(slider);

			// Skapar nytt fält med etiketter och fält för att ändra parametrar i square
			// wave:

			labelPanel.add(new JLabel("Period:"));
			labelPanel.addFixed(5);
			labelPanel.add(new JLabel("Amplitude:"));
			fieldPanel.add(periodVar);
			fieldPanel.addFixed(5);
			fieldPanel.add(amplitudeVar);
			periodVar.setValue(start_period);
			periodVar.setMinimum(1);
			periodVar.setMaximum(50);
			amplitudeVar.setValue(start_amplitude);
			amplitudeVar.setMaximum(10.01);
			amplitudeVar.setMinimum(0);

			periodVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					periodChanged = true;
				}
			});

			amplitudeVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ampChanged = true;
				}
			});

			applyVars.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ampChanged) {
						amplitude = amplitudeVar.getValue();
						ampChanged = false;
					}
					if (periodChanged) {
						period = periodVar.getValue();
						periodChanged = false;
					}

				}

			});

			varPanel.setBorder(BorderFactory.createEtchedBorder());
			varPanel.add(labelPanel);
			varPanel.add(fieldPanel);
			varPanel.add(applyVars);

			// Lägger ihop knappar och slider till en gemensam frame:
			refPanel.add(sliderPanel);
			refPanel.addGlue();
			refPanel.add(rightPanel);
			refPanel.addGlue();
			refPanel.add(varPanel);

			// WindowListener that exits the system if the main window is closed.

			mFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			// Prövar att byta ut mainframe mot en JFrame för att bättre styra vart den ska
			// dyka upp.
			mFrame.getContentPane().add(refPanel, BorderLayout.CENTER);
			mFrame.pack();

			// Position the main window at the screen center.
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension fd = mFrame.getSize();
			mFrame.setLocation((sd.width - fd.width) / 9, (sd.height - fd.height) / 2);

			// Make the window visible.
			mFrame.setVisible(true);

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
			noiseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					noise = !noise;
					if (noise) {
						noiseButton.setText("Measurement Noise ON");
					} else {
						noiseButton.setText("Measurement Noise OFF");
					}
					toggleNoise();
				}
			});
			lDistButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					load = !load;
					if (load) {
						lDistButton.setText("Load Disturbance ON");
					} else {
						lDistButton.setText("Load Disturbance OFF");
					}
					toggleLoadD();
				}
			});
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (!slider.getValueIsAdjusting()) {
						setManual(slider.getValue());
					}
				}
			});

			// MainFrame.setPanel(refPanel, "RefGen");

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

	public void setRegul(Regul regul) {
		this.regul = regul;
	}

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

	private synchronized void toggleNoise() {
		regul.toggleNoise();
	}

	private synchronized void toggleLoadD() {
		regul.toggleLoadD();
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
