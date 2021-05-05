import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {

		final boolean TimeAndEvent = false;

		if (TimeAndEvent) {

		} else {

			// Set thread priorities
			final int regulPriority = 8;
			final int refGenPriority = 7;
			final int plotterPriority = 6;
			final int disturbanceGenPriority = 5;

			ModeMonitor modeMon = new ModeMonitor();

			// Initialise Control system parts
			Regul regul = new Regul(regulPriority, modeMon);
			ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);
			DisturbanceGenerator disturbanceGen = new DisturbanceGenerator(disturbanceGenPriority);
			final graphics GUI = new graphics(plotterPriority, modeMon);

			// Set dependencies
			regul.setgraphics(GUI);
			regul.setRefGen(refgen);
			regul.setDisturbanceGen(disturbanceGen);
			
			disturbanceGen.setRegul(regul);
			refgen.setRegul(regul);
			
			GUI.setRegul(regul);

			// Run GUI on event thread
			Runnable initializeGUI = new Runnable() {
				public void run() {
					GUI.initializeGUI();
					GUI.start();
				}
			};
			try {
				SwingUtilities.invokeAndWait(initializeGUI);
			} catch (Exception e) {
				return;
			}

			// Start remaining threads
			refgen.start();
			disturbanceGen.start();
			regul.start();

		}

	}
}
