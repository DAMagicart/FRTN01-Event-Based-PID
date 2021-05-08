//PID in the Event based example is very different! Especially the integrator. Currently only swapped the use of sample time H with h_act.
//And updating backward difference coefficients ad/bd for every output.

public class Event_PID {
	// Current PID parameters
	private PIDParameters p;
	private double I = 0;
	private double v = 0;
	private double e = 0;
	private double D = 0;
	private double ad = 0;
	private double bd = 0;
	private double y = 0;
	private double yold = 0;
	private double h_act = 0;

	// Constructor
	public Event_PID() {
		p = new PIDParameters();
		// Initial PID Variables, Comment AngPos, more sensitive to load disturbance.
		p.Beta = 3;
		p.H = 0.02;
		p.integratorOn = true;
		p.K = 0.2;
		//p.K = 0.02;
		p.N = 0.2;
		p.Td = 0.5;
		p.Ti = 0.1;
		p.Tr = 10;
		setParameters(p);
	}

	// Calculates the control signal v.
	public synchronized double calculateOutput(double y, double yref, double hact) {
		h_act = hact;
		yold = this.y;
		this.y = y;
		e = yref - y;
		if ((p.Td != 0) || (p.N != 0)) {
			ad = p.Td / (p.Td + p.N * h_act);
		}
		bd = p.K * ad * p.N;
		D = ad * D - bd * (y - yold);
		return v = p.K * (p.Beta * yref - y) + I + D;
	}

	// Updates the controller state.
	// Should use tracking-based anti-windup
	public synchronized void updateState(double u) {
		if (p.integratorOn) {
			I = I + (p.K * h_act / p.Ti) * e + (h_act / p.Tr) * (u - v);
		} else {
			I = 0;
		}
	}

	// Returns the sampling interval expressed as a long.
	// Explicit type casting needed.
	public synchronized long getHMillis() {
		return (long) (p.H * 1000);
	}

	// Sets the PIDParameters.
	// Called from PIDGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIDParameters newParameters) {
		p = (PIDParameters) newParameters.clone();
		if (p.Ti == 0) {
			p.integratorOn = false;
		} else {
			p.integratorOn = true;
		}
		if (!p.integratorOn) {
			I = 0;
		}
	}

	// Sets the I-part of the controller to 0.
	// For example needed when changing controller mode.
	public synchronized void reset() {
		I = 0;
		D = 0;
	}

	// Returns the current PIDParameters.
	public synchronized PIDParameters getParameters() {
		return p;
	}
}
