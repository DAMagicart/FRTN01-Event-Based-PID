//Mode monitor with 4 modes, depending on PID used.
public class ModeMonitor {

    private Mode mode = Mode.OFF;

    public synchronized void setMode(Mode newMode) {
        mode = newMode;
    }


    public synchronized Mode getMode() {
        return mode;
    }


    public enum Mode {
        OFF, TIME, EVENT, BOTH;
    }
}