import javax.swing.SwingUtilities;
import javax.swing.*;
import se.lth.control.*;

public class Main {

	public static void main(String[] args) {

		// Set thread priorities
		final int regulPriority = 8;
		final int refGenPriority = 7;
		final int plotterPriority = 6;

		// Initialize monitor:
		ModeMonitor modeMon = new ModeMonitor();

		// Initialize plotter:
		graphics PlotGUI = new graphics(plotterPriority, modeMon);

		// Initialise Control system parts
		Regul regul = new Regul(regulPriority);

		regul.setGUI(PlotGUI);

		// Start remaining threads
		PlotGUI.start();
		regul.start();

	}

}
