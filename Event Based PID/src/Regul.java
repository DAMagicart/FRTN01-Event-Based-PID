public class Regul extends Thread {

	private Event_PID E_PID = new Event_PID();
	private PID T_PID = new PID();

	private Plant Servo;

	private int priority;
	private boolean shouldRun = true;
	private long starttime;

	// private ModeMonitor modeMon;

	public Regul(int pri) {
		priority = pri;
		setPriority(priority);

		Servo = new Plant();
		// this.modeMon = modeMon;
	}

	// Sets the inner controller's parameters
	public void setTimeBasedParameters(PIDParameters p) {
		T_PID.setParameters(p);
	}

	// Gets the inner controller's parameters
	public PIDParameters getInnerParameters() {
		return T_PID.getParameters();
	}

	// Sets the outer controller's parameters
	public void setEventBasedParameters(PIDParameters p) {
		E_PID.setParameters(p);
	}

	// Gets the outer controller's parameters
	public PIDParameters getOuterParameters() {
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

	public void run() {

		long duration;
		long t = System.currentTimeMillis();
		starttime = t;

		while (shouldRun) {
			double VelRef = 3.14;
			double PosRef = 0;
			double AngVel = Servo.getAnglePos();
			double AngPos = Servo.getAngleVel();
			double uRef = 0;
			double u = 0;

			switch (2) {
			case 1: {
				T_PID.reset();
				E_PID.reset();
				u = 0;
				VelRef = 0;
				PosRef = 0;
				break;
			}
			case 2: {
				synchronized (T_PID) {
					u = limit(T_PID.calculateOutput(AngVel, VelRef));
					Servo.setU(u);
					T_PID.updateState(u);
				}
				break;
			}
			case 3: {
			}
			case 4: {
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}

			System.out.println("U:" + u);
			System.out.println("Angular Velocity:" + AngVel + " Angular Position:" + AngPos);

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
