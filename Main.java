import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {

		// Set thread priorities
		final int regulPriority = 8;
		final int refGenPriority = 7;
		final int plotterPriority = 6;

		ModeMonitor modeMon = new ModeMonitor();
		
		// Initialise Control system parts
		Regul regul = new Regul(regulPriority, modeMon);
		final graphics GUI = new graphics(plotterPriority, modeMon);
		
        // Set dependencies
        regul.setgraphics(GUI); 
        GUI.setRegul(regul);
        
        // Run GUI on event thread
        Runnable initializeGUI = new Runnable(){
            public void run(){
                GUI.initializeGUI();
                GUI.start();
            }
        };
        try{
            SwingUtilities.invokeAndWait(initializeGUI);
        }catch(Exception e){
            return;
        }

		// Start remaining threads
		regul.start();

	}

}
