package Part2_Report;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FlightSimulatorVersion2 {
    private Aircraft aircraft;
    private Weather weather;
    private Graphics graphics;
    public FlightSimulatorVersion2() {
        this.aircraft = new Aircraft();
        this.weather = new Weather();
        this.graphics = new Graphics();
    }
    public void update() {
        this.aircraft.update(this.weather);
        this.graphics.render(this.aircraft, this.weather);
    }
}
class Graphics {
    private Renderer renderer;
    public Graphics() {
        this.renderer = new Renderer();
    }
    public void render(Aircraft aircraft, Weather weather) {
        // Render the aircraft and weather using multi-threading
    }
}
class Renderer {
    private ThreadPoolExecutor threadPool;
    public Renderer() {
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    }
    public void render(Aircraft aircraft, Weather weather) {
        // Render the aircraft and weather using multi-threading
    }
}


