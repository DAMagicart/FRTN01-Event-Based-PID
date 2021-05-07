
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
	private DisturbanceGenerator disturbanceGen;
	
	public long eventTime;
	private int eventFreq;
	
	double eLim = 0.1;
	int factor = 10;
	double hmax;

	public Regul(int pri, ModeMonitor modeMon) {
		priority = pri;
		setPriority(priority);

		Servo = new Plant();
		this.modeMon = modeMon;
	}
	
	public void setEventTimeNFreq(long time) {
		this.eventTime = time;
		eventFreq = 0;
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

	public void setgraphics(graphics GUI) {
		this.GUI = GUI;
	}

	public void setRefGen(ReferenceGenerator refGen) {
		this.refGen = refGen;
	}
	
	public void setDisturbanceGen(DisturbanceGenerator disturbanceGen) {
		this.disturbanceGen = disturbanceGen;
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
	
	public void setHMax(int factor) {
		this.factor = factor;
		hmax = factor*E_PID.getParameters().H;
	}

	public void toggleNoise() {
		Servo.toggleNoise();
	}
	
	public void setNoise(double newNoise) {
		Servo.setNoise(newNoise);
	}
	
	public double getNoise() {
		return Servo.getNoise();
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
	
	private void updateEPeriod(int frequency) {
		double t = (double) (System.currentTimeMillis() - eventTime) / 1000.0;
		GUI.avgPeriod(frequency, t);
	}

	public void run() {

		long duration;
		long t = System.currentTimeMillis();
		starttime = t;

		double hNom = E_PID.getParameters().H;
		double hact = 0;
		hmax = hNom * factor;

		while (shouldRun) {
			double PosRef = refGen.getRef();
			
			double disturbanceSignal = disturbanceGen.getDist();
			
			Servo.setLoadD(disturbanceSignal);
			
			double VelRef = 0;
			double AngVel = Servo.getAngleVel();
			double AngPos = Servo.getAnglePos();
			double uRef = 0;
			double u = 0;
			double eP = AngVel - PosRef;
			

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
					u = limit(T_PID.calculateOutput(AngVel, PosRef));
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
						eventFreq ++;
						u = limit(E_PID.calculateOutput(AngVel, PosRef, hact));
						Servo.setU(u);
						E_PID.updateState(u);
						// timeOld = System.currentTimeMillis();
						hact = 0;
					}
				}
				updateEPeriod(eventFreq);
				break;
			}
			case BOTH: {
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}


			sendDataToOpCom(PosRef, AngVel, u);
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

