import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {

		// Set thread priorities
		final int regulPriority = 8;
		final int refGenPriority = 7;
		final int plotterPriority = 6;

		// Initialise Control system parts
		Regul regul = new Regul(regulPriority);

		// Start remaining threads
		regul.start();

	}

}
