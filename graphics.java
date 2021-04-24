
import SimEnvironment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import se.lth.control.*;
import se.lth.control.plot.PlotterPanel;

public class graphics {

	private Regul regul;
	private PIDParameters T_parameters;
	private PIDParameters E_parameters;
	private int priority;
	private PlotterPanel measPanel, ctrlPanel;
	private BoxPanel plotterPanel;
	private boolean isInitialized = false;

	// Monitors
	private ModeMonitor modeMon;

	// Declarartion of main frame.
	private JFrame frame;

	/* Constructor */
	public graphics(int GUIPriority, ModeMonitor modeMon) {
		this.priority = GUIPriority;
		this.modeMon = modeMon;
	}

	public void setRegul(Regul regul) {
		this.regul = regul;
	}

	/* Starts the thread */
	public void start() {
		measPanel.start();
		ctrlPanel.start();
	}

	/* Initialize the GUI */
	public void initializeGUI() {

		frame = new JFrame("Machine modelling");
		// Create a panel for the two plotters.
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create PlotterPanels.
		measPanel = new PlotterPanel(2, priority);
		measPanel.setYAxis(20.0, -10.0, 2, 2);
		measPanel.setXAxis(10, 5, 5);
		measPanel.setUpdateFreq(10);
		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(20.0, -10.0, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		ctrlPanel.setUpdateFreq(10);

		plotterPanel.add(measPanel);
		plotterPanel.addFixed(10);
		plotterPanel.add(ctrlPanel);
		T_parameters = regul.getTimeBasedParameters();
		E_parameters = regul.getEventBasedParameters();

		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				regul.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				System.exit(0);
			}
		});

		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(plotterPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		frame.setLocation((sd.width - fd.width) / 2, (sd.height - fd.height) / 2);

		// Make the window visible.
		frame.setVisible(true);
		// Set the GUI to initialized meaning that it is possible to send data to it.
		isInitialized = true;
	}

	/** Called by Regul to plot a measurement data point. */
	public synchronized void putMeasurementData(double t, double yRef, double y) {
		if (isInitialized) {
			measPanel.putData(t, yRef, y);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putMeasurementData().");
		}
	}
	
	/** Called by Regul to plot a control signal data point. */
	public synchronized void putControlData(double t, double u) {
		if (isInitialized) {
			ctrlPanel.putData(t, u);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putControlData().");
		}
	}

}