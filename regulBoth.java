
public class regulBoth extends Regul {

	private Event_PID E_PID = new Event_PID();
	private PID T_PID = new PID();

	private Plant Servo;
	private Plant ServoE;

	private int priority;
	private boolean shouldRun = true;
	private long starttime;

	private ModeMonitor modeMon;
	private graphicsBoth GUI;
	private ReferenceGenerator refGen;
	private DisturbanceGenerator disturbanceGen;

	public long eventTime;
	private int eventFreq;

	double eLim = 0.1;

	public regulBoth(int pri, ModeMonitor modeMon) {
		super(pri, modeMon);
		priority = pri;
		setPriority(priority);

		Servo = new Plant();
		ServoE = new Plant();
		this.modeMon = modeMon;

	}

	public void setEventTime(long time) {
		this.eventTime = time;
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
	protected double limit(double v) {
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

	public void setRefGen(ReferenceGenerator refGen) {
		this.refGen = refGen;
	}

	public void setDisturbanceGen(DisturbanceGenerator disturbanceGen) {
		this.disturbanceGen = disturbanceGen;
	}

	public void setgraphics(graphicsBoth GUI) {
		this.GUI = GUI;
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
		ServoE.toggleNoise();
	}

	public void setNoise(double newNoise) {
		Servo.setNoise(newNoise);
		Servo.setNoise(newNoise);
	}

	public double getNoise() {
		return Servo.getNoise();
	}

	public void setLoadD(double newLoad) {
		Servo.setLoadD(newLoad);
		ServoE.setLoadD(newLoad);

	}

	public double getLoadD() {
		return Servo.getLoadD();

	}

	private void sendDataToOpCom(double yRef, double yT, double yE, double uT, double uE) {
		double x = (double) (System.currentTimeMillis() - starttime) / 1000.0;
		GUI.putControlData(x, uT, uE);
		GUI.putMeasurementData(x, yT, yE, yRef);
	}

	private void updateEPeriod(int frequency) {
		double t = (double) (System.currentTimeMillis() - eventTime) / 1000.0;
		GUI.avgPeriod(frequency, t);
	}

	public void run() {

		long duration;
		long t = System.currentTimeMillis();
		starttime = t;
		eventTime = System.currentTimeMillis();

		double hNom = E_PID.getParameters().H;
		double hact = 0;
		double hmax = hNom * 10;

		while (shouldRun) {
			double PosRef = refGen.getRef();

			double disturbanceSignal = disturbanceGen.getDist();

			Servo.setLoadD(disturbanceSignal);
			ServoE.setLoadD(disturbanceSignal);

			// double VelRef = 0;
			// double AngVelT = Servo.getAnglePos();
			double AngVelT = Servo.getAngleVel();
			double AngVelE = Servo.getAngleVel();
			double uRef = 0;
			double uT = 0;
			double uE = 0;
			double eP = AngVelE - PosRef;

			switch (modeMon.getMode()) {
			case OFF: {
				T_PID.reset();
				E_PID.reset();
				uT = 0;
				uE = 0;
				PosRef = 0;
				break;
			}
			case TIME: {
			}
			case EVENT: {
			}
			case BOTH: {
				synchronized (T_PID) {
					uT = limit(T_PID.calculateOutput(AngVelT, PosRef));
					Servo.setU(uT);
					T_PID.updateState(uT);
				}
				synchronized (E_PID) {
					// hact = (double) (System.currentTimeMillis() - timeOld) / 1000.0;
					hact += hNom;
					if ((Math.abs(eP) >= eLim) || (hact >= hmax)) {
						eventFreq++;
						uE = limit(E_PID.calculateOutput(AngVelE, PosRef, hact));
						ServoE.setU(uE);
						E_PID.updateState(uE);
						// timeOld = System.currentTimeMillis();
						hact = 0;
					}
				}
				updateEPeriod(eventFreq);
			}
				break;
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}

			sendDataToOpCom(PosRef, AngVelT, AngVelE, uT, uE);
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
		ServoE.setU(0.0);
	}
}
