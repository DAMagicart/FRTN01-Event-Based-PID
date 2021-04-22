import java.util.Timer;
import java.util.TimerTask;

//Notes: Potentially use some form of voltage conversion?)
//Introduce noise here, with adding a random term (RNG(0-1)*MaxAmp)
//Introduce limit ranges for states and AD / DA converter?
//Final swing FX animation, only add if we have time afterwards.

public class Plant {
//Physical constants
	// State matrix coefficients
	private double A11 = -0.12;
	private double A21 = 5;
	private double B11 = 2.25;

//States
	private volatile double omega = 0.0; // Angular velocity state 1
	private volatile double theta = 0.0; // Angular position state 2
	private volatile double u = 0.0; // Control signal

	public Plant() {
		final int period = 5; // task period (in milliseconds)

		// Create periodic timer
		Timer timer = new Timer();
		TimerTask timertask = new TimerTask() {
			@Override
			public void run() {
				updateStates(); // time step the dynamics simulator
			}
		};
		timer.scheduleAtFixedRate(timertask, 0, period);
	}

	public void setU(double u) {
		if (u < -10) {
			this.u = -10;
		} else if (u > 10) {
			this.u = 10;
		} else {
			this.u = u;
		}
	}

	private void updateStates() {
		omega = A11 * omega + B11 * u;
		theta = A21 * omega;
		//System.out.println("omega: " + omega + " theta: " + theta);
	}

	public double getAnglePos() {
		return theta;
	}

	public double getAngleVel() {
		return omega;
	}
}
