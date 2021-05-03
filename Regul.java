
public class Regul extends Thread {

	private Event_PID E_PID = new Event_PID();
	private PID T_PID = new PID();

	private Plant Servo;

	private int priority;
	private boolean shouldRun = true;
	private long starttime;

	private ModeMonitor modeMon;
	private graphics GUI;
	private ReferenceGenerator refGen;

	double eLim = 0.1;

	public Regul(int pri, ModeMonitor modeMon) {
		priority = pri;
		setPriority(priority);

		Servo = new Plant();
		this.modeMon = modeMon;
	}

	// Sets the Time controller's parameters
	public void setTimeBasedParameters(PIDParameters p) {
		T_PID.setParameters(p);
	}

	// Gets the Time controller's parameters
	public PIDParameters getTimeBasedParameters() {
		return T_PID.getParameters();
	}

	// Sets the Event controller's parameters
	public void setEventBasedParameters(PIDParameters p) {
		E_PID.setParameters(p);
	}

	// Gets the Event controller's parameters
	public PIDParameters getEventBasedParameters() {
		return E_PID.getParameters();
	}

	// Saturation function
	private double limit(double v) {
		return limit(v, -10, 10);
	}

	// Saturation function
	private double limit(double v, double min, double max) {
		if (v < min)
			v = min;
		else if (v > max)
			v = max;
		return v;
	}

	public void setgraphics(graphics GUI) {
		this.GUI = GUI;
	}

	public void setRefGen(ReferenceGenerator refGen) {
		this.refGen = refGen;
	}

	public void shutDown() {
		shouldRun = false;
	}

	public double geteLim() {
		return eLim;
	}

	public void seteLim(double neweLim) {

		this.eLim = neweLim;
	}

	public void toggleNoise() {
		Servo.toggleNoise();
	}

	public void toggleLoadD() {
		Servo.toggleLoadD();
	}
	
	public void setNoise(double newNoise) {
		
		//TODO skriva in ny noise här.
	}
	
	public void getNoise() {
		//TODO: Implementera noise här.
		
	}
	
	public void setLoadD(double newLoad) {
		Servo.setLoadD(newLoad);
		
	}
	
	public double getLoadD() {
		return Servo.getLoadD();
		
	}
	

	private void sendDataToOpCom(double yRef, double y, double u) {
		double x = (double) (System.currentTimeMillis() - starttime) / 1000.0;
		GUI.putControlData(x, u);
		GUI.putMeasurementData(x, yRef, y);
	}

	public void run() {

		long duration;
		long t = System.currentTimeMillis();
		starttime = t;

		double hNom = E_PID.getParameters().H;
		double hact = 0;
		long timeOld = 0;
		// double hmax = 10;
		double hmax = hNom * 10;

		while (shouldRun) {
			double PosRef = refGen.getRef();
			double VelRef = 0;
			double AngVel = Servo.getAnglePos();
			double AngPos = Servo.getAngleVel();
			double uRef = 0;
			double u = 0;
			double eP = AngPos - PosRef;

			switch (modeMon.getMode()) {
			case OFF: {
				T_PID.reset();
				E_PID.reset();
				u = 0;
				VelRef = 0;
				PosRef = 0;
				break;
			}
			case TIME: {
				synchronized (T_PID) {
					u = limit(T_PID.calculateOutput(AngPos, PosRef));
					Servo.setU(u);
					T_PID.updateState(u);
				}
				break;
			}
			case EVENT: {
				synchronized (E_PID) {
					// hact = (double) (System.currentTimeMillis() - timeOld) / 1000.0;
					hact += hNom;
					if ((Math.abs(eP) >= eLim) || (hact >= hmax)) {
						u = limit(E_PID.calculateOutput(AngPos, PosRef, hact));
						Servo.setU(u);
						E_PID.updateState(u);
						// timeOld = System.currentTimeMillis();
						hact = 0;
					}
				}
				break;
			}
			case BOTH: {
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}

			// System.out.println("U:" + u);
			// System.out.println("Angular Velocity:" + AngVel + " Angular Position:" +
			// AngPos);

			sendDataToOpCom(PosRef, AngPos, u);
			// sleep
			t = t + T_PID.getHMillis();
			duration = t - System.currentTimeMillis();
			if (duration > 0) {
				try {
					sleep(duration);
				} catch (InterruptedException x) {
				}
			} else {
				System.out.println("Lagging behind...");
			}
		}
		Servo.setU(0.0);
	}
}
