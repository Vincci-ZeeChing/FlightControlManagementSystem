package Part2_Report;

public class FlightSimulatorVersion1 {
    private final Aircraft aircraft;
    private final Weather weather;

    public FlightSimulatorVersion1() {
        this.aircraft = new Aircraft();
        this.weather = new Weather();
    }

    public void update() {
        this.aircraft.update(this.weather);
        this.render();
    }

    public void render() {
        // Render the aircraft and weather
    }
}


class Aircraft {
    public void update(Weather weather) {

    }
}

class Weather {
}



