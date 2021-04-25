
import SimEnvironment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import se.lth.control.*;
import se.lth.control.plot.PlotterPanel;
import se.lth.control.plot.*;

public class graphics {

	// private static final double eps = 0.000001;

	// External parameters:
	private Regul regul;
	private PIDParameters T_parameters;
	private PIDParameters E_parameters;
	private int priority;

	// Declaring the panels:
	private PlotterPanel measPanel, ctrlPanel;
	private BoxPanel EventParametersPanel, TimeParametersPanel, plotterPanel, ParametersPanel, mainPanel,
			modeChangePanel;
	private JPanel TimePIDLabelPanel, TimePIDFieldPanel, EventPIDLabelPanel, EventPIDFieldPanel;

	// Declaring Fields:
	private DoubleField TimeKVal = new DoubleField(5, 3);
	private DoubleField TimeTiVal = new DoubleField(5, 3);
	private DoubleField TimeTdVal = new DoubleField(5, 3);
	private DoubleField TimeTrVal = new DoubleField(5, 3);
	private DoubleField TimeNVal = new DoubleField(5, 3);
	private DoubleField TimeBetaVal = new DoubleField(5, 3);
	private DoubleField TimeHVal = new DoubleField(5, 3);

	private DoubleField EventKVal = new DoubleField(5, 3);
	private DoubleField EventTiVal = new DoubleField(5, 3);
	private DoubleField EventTdVal = new DoubleField(5, 3);
	private DoubleField EventTrVal = new DoubleField(5, 3);
	private DoubleField EventNVal = new DoubleField(5, 3);
	private DoubleField EventBetaVal = new DoubleField(5, 3);
	private DoubleField EventHVal = new DoubleField(5, 3);

	private JButton applyTimeVariables, applyEventVariables;

	private JRadioButton TimeDriven;
	private JRadioButton EventDriven;

	// isInitialized becomes true when the GUI initializes.
	private boolean isInitialized = false;

	// The boolean hChanged is to keep track if the sample rate has changed.
	private boolean hChanged = false;

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
		// Updates how smooth the plotter is:
		measPanel.setUpdateFreq(10);
		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(20.0, -10.0, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		// Updates how smooth the plotter is:
		ctrlPanel.setUpdateFreq(10);

		plotterPanel.add(measPanel);
		plotterPanel.addFixed(10);
		plotterPanel.add(ctrlPanel);

		T_parameters = regul.getTimeBasedParameters();
		E_parameters = regul.getEventBasedParameters();

		// Creates the panel for changing regulator variables:

		// Creates the LabelPanel for T_PID:
		TimePIDLabelPanel = new JPanel();
		TimePIDLabelPanel.add(new Label("K value:"));
		TimePIDLabelPanel.add(new Label("Ti value:"));
		TimePIDLabelPanel.add(new Label("Td value:"));
		TimePIDLabelPanel.add(new Label("Tr value:"));
		TimePIDLabelPanel.add(new Label("N value:"));
		TimePIDLabelPanel.add(new Label("Beta value:"));
		TimePIDLabelPanel.add(new Label("H value:"));

		// Adding the DoubleField parameters for Time_PID:
		TimePIDFieldPanel = new JPanel();
		TimePIDFieldPanel.add(TimeKVal);
		TimePIDFieldPanel.add(TimeTiVal);
		TimePIDFieldPanel.add(TimeTdVal);
		TimePIDFieldPanel.add(TimeTrVal);
		TimePIDFieldPanel.add(TimeNVal);
		TimePIDFieldPanel.add(TimeBetaVal);
		TimePIDFieldPanel.add(TimeHVal);

		// Set initial values for Time_PID:
		TimeKVal.setValue(T_parameters.K);
		TimeTiVal.setValue(T_parameters.Ti);
		TimeTdVal.setValue(T_parameters.Td);
		TimeTrVal.setValue(T_parameters.Tr);
		TimeNVal.setValue(T_parameters.N);
		TimeBetaVal.setValue(T_parameters.Beta);
		TimeHVal.setValue(T_parameters.H);
		TimeHVal.setMinimum(0.00001);
		applyTimeVariables = new JButton();

		// Add action listeners for the Time_Field variables:

		TimeKVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.K = TimeKVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeTiVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.Ti = TimeTiVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeTdVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.Td = TimeTdVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeTrVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.Tr = TimeTrVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeNVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.N = TimeNVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeBetaVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.Beta = TimeBetaVal.getValue();
				applyTimeVariables.setEnabled(true);
			}

		});

		TimeHVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				T_parameters.H = TimeHVal.getValue();
				E_parameters.H = TimeHVal.getValue();
				EventHVal.setValue(TimeHVal.getValue());
				applyTimeVariables.setEnabled(true);
				hChanged = true;
			}

		});

		applyTimeVariables.setEnabled(false);
		applyTimeVariables.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setTimeBasedParameters(T_parameters);
				if (hChanged) {
					regul.setEventBasedParameters(E_parameters);

				}
				hChanged = false;
				applyTimeVariables.setEnabled(false);

			}

		});

		// Adds Field Panel and Label Panel together in a common Panel with the apply
		// button:
		System.out.println("Av någon anledning så kör ej koden längre än hit. Högst oklart varför.");
		TimeParametersPanel.setBorder(BorderFactory.createTitledBorder("Time_PID Parameters:"));
		TimeParametersPanel.add(TimePIDLabelPanel);
		TimeParametersPanel.addGlue();
		TimeParametersPanel.add(TimePIDFieldPanel);
		TimeParametersPanel.addGlue();
		TimeParametersPanel.add(applyTimeVariables);

		// Creates the LabelPanel for E_PID:
		EventPIDLabelPanel = new JPanel();
		EventPIDLabelPanel.add(new Label("K value:"));
		EventPIDLabelPanel.add(new Label("Ti value:"));
		EventPIDLabelPanel.add(new Label("Td value:"));
		EventPIDLabelPanel.add(new Label("Tr value:"));
		EventPIDLabelPanel.add(new Label("N value:"));
		EventPIDLabelPanel.add(new Label("Beta value:"));
		EventPIDLabelPanel.add(new Label("H value:"));

		// Adding the DoubleField parameters for Event_PID:
		EventPIDFieldPanel = new JPanel();
		EventPIDFieldPanel.add(EventKVal);
		EventPIDFieldPanel.add(EventTiVal);
		EventPIDFieldPanel.add(EventTdVal);
		EventPIDFieldPanel.add(EventTrVal);
		EventPIDFieldPanel.add(EventNVal);
		EventPIDFieldPanel.add(EventBetaVal);
		EventPIDFieldPanel.add(EventHVal);

		// Set initial values for Event_PID:
		EventKVal.setValue(E_parameters.K);
		EventTiVal.setValue(E_parameters.Ti);
		EventTdVal.setValue(E_parameters.Td);
		EventTrVal.setValue(E_parameters.Tr);
		EventNVal.setValue(E_parameters.N);
		EventBetaVal.setValue(E_parameters.Beta);
		EventHVal.setValue(E_parameters.H);

		// Add action listeners for the Event_Field variables:

		EventKVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.K = EventKVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});
		EventTiVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.Ti = EventTiVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});

		EventTdVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.Td = EventTdVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});

		EventTrVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.Tr = EventTrVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});

		EventNVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.N = EventNVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});

		EventBetaVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				E_parameters.Beta = EventBetaVal.getValue();
				applyEventVariables.setEnabled(true);
			}
		});

		EventHVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// If the sampling rate is changed then it must be changed in the time PID as
				// well.
				E_parameters.H = EventHVal.getValue();
				T_parameters.H = EventHVal.getValue();
				TimeHVal.setValue(EventHVal.getValue());
				hChanged = true;
				applyEventVariables.setEnabled(true);
			}
		});

		applyEventVariables = new JButton("Apply Event Variables");
		applyEventVariables.setEnabled(false);
		applyEventVariables.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setEventBasedParameters(E_parameters);
				if (hChanged) {
					regul.setTimeBasedParameters(T_parameters);
				}
				hChanged = false;
				applyEventVariables.setEnabled(false);
			}
		});

		EventParametersPanel.setBorder(BorderFactory.createTitledBorder("Event_PID Parameters:"));
		EventParametersPanel.add(EventPIDLabelPanel);
		EventParametersPanel.addGlue();
		EventParametersPanel.add(EventPIDFieldPanel);
		EventParametersPanel.addGlue();
		EventParametersPanel.add(applyEventVariables);

		// Add together Event parameters and Time parameters:
		ParametersPanel.add(TimeParametersPanel);
		ParametersPanel.addGlue();
		ParametersPanel.add(EventParametersPanel);

		// Create a new panel for switching between event driven and time driven
		// regulators:
		modeChangePanel = new BoxPanel(BoxPanel.VERTICAL);
		ButtonGroup group = new ButtonGroup();
		group.add(EventDriven);
		group.add(TimeDriven);
		TimeDriven.setSelected(true);

		TimeDriven.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Lägga in en funktion som ändrar mode till TIME här. 
			}

		});
		
		EventDriven.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Lägga in en funktion som ändrar mode till EVENT här. 
			}

		});
		

		// Add together all Panels into one main Panel:
		mainPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		mainPanel.add(plotterPanel, BorderLayout.CENTER);
		mainPanel.add(ParametersPanel, BorderLayout.WEST);

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
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		// Den lägger frame lite till höger så att den inte ska komma rakt över Reference generator
		frame.setLocation((sd.width - fd.width) * 2 / 3, (sd.height - fd.height) / 2); 

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
