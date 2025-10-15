package es.uab.tqs.f1_2D.controlador;
import es.uab.tqs.f1_2D.model.Car;

import java.util.Set;

public class CarController {
    private Car car;

    public CarController(Car car) {
        this.car = car;
    }

    public void processInput(Set<Integer> keys) {
        car.movement(keys);
        car.update();
    }

    public Car getCar() {
        return car;
    }
}
