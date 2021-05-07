import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {

		final boolean TimeAndEvent = false;

		// Set thread priorities
		final int regulPriority = 8;
		final int refGenPriority = 7;
		final int plotterPriority = 6;
		final int disturbanceGenPriority = 5;

		ModeMonitor modeMon = new ModeMonitor();

		// Initialise Control system parts

		if (TimeAndEvent) {
			regulBoth regul = new regulBoth(regulPriority, modeMon);
			final graphicsBoth GUI_BOTH = new graphicsBoth(plotterPriority, modeMon);
			ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);
			DisturbanceGenerator disturbanceGen = new DisturbanceGenerator(disturbanceGenPriority);

			// Set dependencies
			regul.setgraphics(GUI_BOTH);
			regul.setRefGen(refgen);
			regul.setDisturbanceGen(disturbanceGen);

			disturbanceGen.setRegul(regul);
			refgen.setRegul(regul);

			GUI_BOTH.setRegul(regul);

			// Run GUI on event thread
			Runnable initializeGUI = new Runnable() {
				public void run() {
					GUI_BOTH.initializeGUI();
					GUI_BOTH.start();
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
			
		} else {
			// Initialise Control system parts
			Regul regul = new Regul(regulPriority, modeMon);
			final graphics GUI = new graphics(plotterPriority, modeMon);
			ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);
			DisturbanceGenerator disturbanceGen = new DisturbanceGenerator(disturbanceGenPriority);

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
