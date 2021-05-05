package github_project;


import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;

import se.lth.control.BoxPanel;
import se.lth.control.MainFrame;

public class DisturbanceGenerator extends Thread {
	private static final int MANUAL = 0, SQUARE = 1;
	private final int priority;
	private double amplitude;
	private double period;
	private double sign = -1;
	private double manual;
	private double dist;
	private double max_ctrl;
	private int mode = MANUAL;
	private Regul regul;

	private class DisturbGUI {
		
		private JFrame mFrame;
		//Initierar de mest grundläggande grafikdelarna:
		private BoxPanel rightPanel = new BoxPanel(BoxPanel.HORIZONTAL); //Till höger, ska innehålla möjligheten att ändra square wave och noise amplituden.
		private BoxPanel buttonsPanel = new BoxPanel(BoxPanel.VERTICAL); // I mitten. Kan ändra läge samt sätta av och på noise. Bör kanske lägga in en off knapp.
		private JPanel sliderPanel = new JPanel();
		
		private JPanel buttonsButtons = new JPanel(new BorderLayout()); //Panel som lägger in knappar i en ny panel av någon anledning.
		
		private BoxPanel bigPanel = new BoxPanel(BoxPanel.VERTICAL); // Panel som allt ska läggas in i på slutet.

		
		// Initierar det som behövs för att ändra variablerna för squareWave:
		private BoxPanel squareFieldPanel = new BoxPanel(BoxPanel.VERTICAL);
		private BoxPanel squareLabelPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel squareVarPanel = new JPanel();

		
		
		private BoxPanel noiseFieldPanel = new BoxPanel(BoxPanel.VERTICAL);
		private BoxPanel noiseLabelPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel noisePanel = new JPanel();

		
		// Skapar en slider:
		private JSlider slider = new JSlider(JSlider.VERTICAL, -10, 10, 0);
		
		// Skapar knappar som styr över om ref generatorn ger en fyrkantsvåg eller om
		// man justerar den manuellt med en slider.
		private JRadioButton manButton = new JRadioButton("Manual");
		private JRadioButton sqButton = new JRadioButton("Square");
		
		//Knapp som stänger av och på noise i systemet, samt en boolean som håller koll:
		private JButton noiseButton = new JButton("Measurement Noise OFF");
		private boolean noise = false;
		

		

		// Skapar två variabelfält för att kunna ändra variablerna live.
		private DoubleField periodVar = new DoubleField(5, 3);
		private DoubleField amplitudeVar = new DoubleField(5, 3);
		// Skapar resten som behövs för att ändra variablerna i square signalen:
		private JButton applyVars = new JButton("Apply new parameters");
		private boolean ampChanged = false;
		private boolean periodChanged = false;
		
		//Saker för att ordna Noise delen:
		private DoubleField noiseField = new DoubleField(5,3);
		private JButton applyNoise = new JButton("Apply new noise");
		private boolean noiseChange = false;
		

		// Constructor
		private DisturbGUI(double start_amplitude, double start_period) {

			// Bytt ut mainframe mot en JPanel som heter mainFrame
			// MainFrame.showLoading();
			mFrame = new JFrame("Disturbance Generator");

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
			ButtonGroup group = new ButtonGroup();
			group.add(manButton);
			group.add(sqButton);
			group.add(noiseButton);
			
			manButton.setSelected(true);
			
			buttonsButtons.add(buttonsPanel,BorderLayout.CENTER);

			//rightPanel.setLayout(new BorderLayout());
			//rightPanel.add(buttonsPanel, BorderLayout.CENTER);

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

			squareLabelPanel.add(new JLabel("Period:"));
			squareLabelPanel.addFixed(5);
			squareLabelPanel.add(new JLabel("Amplitude:"));
			squareFieldPanel.add(periodVar);
			squareFieldPanel.addFixed(5);
			squareFieldPanel.add(amplitudeVar);
			periodVar.setValue(start_period);
			periodVar.setMinimum(1);
			periodVar.setMaximum(Double.POSITIVE_INFINITY);
			amplitudeVar.setValue(start_amplitude);
			amplitudeVar.setMaximum(10.01);
			amplitudeVar.setMinimum(0);

			periodVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					periodChanged = true;
					applyVars.setEnabled(true);
				}
			});

			amplitudeVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ampChanged = true;
					applyVars.setEnabled(true);
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

				applyVars.setEnabled(false);
				}

			});

			squareVarPanel.setBorder(BorderFactory.createEtchedBorder());
			squareVarPanel.add(squareLabelPanel);
			squareVarPanel.add(squareFieldPanel);
			squareVarPanel.add(applyVars);

			
			//Skapar allting för att kunna ändra noise disturbance parametern:
			noiseLabelPanel.add(new Label("Noise disturbance:"));
			noiseFieldPanel.add(noiseField);
			noiseField.setValue(0.1);

			noiseField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					noiseChange = true;
					applyNoise.setEnabled(true);
				}
			});

			applyNoise.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (noiseChange) {
						changeNoise(noiseField.getValue());
					}
					noiseChange = false;
					applyNoise.setEnabled(false);
				}

			});
			noisePanel.setBorder(BorderFactory.createEtchedBorder());
			noisePanel.add(noiseLabelPanel);
			noisePanel.add(noiseFieldPanel);
			noisePanel.add(applyNoise);

			
			rightPanel.add(squareVarPanel, BorderLayout.NORTH);
			rightPanel.add(noisePanel, BorderLayout.SOUTH);

			// Lägger ihop knappar och slider till en gemensam frame:
			bigPanel.add(sliderPanel);
			bigPanel.addGlue();
			bigPanel.add(buttonsButtons);
			bigPanel.addGlue();
			bigPanel.add(rightPanel);

			// WindowListener that exits the system if the main window is closed.

			mFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			// Prövar att byta ut mainframe mot en JFrame för att bättre styra vart den ska
			// dyka upp.
			mFrame.getContentPane().add(bigPanel, BorderLayout.CENTER);
			mFrame.pack();

			// Position the main window at the screen center.
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension fd = mFrame.getSize();
			mFrame.setLocation((sd.width - fd.width) / 12, (sd.height - fd.height) / 5);

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
	public DisturbanceGenerator(int distGenPriority) {
		priority = distGenPriority;
		amplitude = 2;
		period = 5;
		manual = 0.0;
		dist = Math.PI;
		max_ctrl = 10;

		new DisturbGUI(amplitude, period);
	}

	public void setRegul(Regul regul) {
		this.regul = regul;
	}

	private synchronized void setDist(double newDist) {
		dist = newDist;
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
	private synchronized void changeNoise(double noiseAmp) {
		regul.setNoise(noiseAmp);
	}

	
	public synchronized double getDist() {
		return (mode == MANUAL) ? manual : dist;
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
						dist = manual;
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
								dist = setpoint;
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
