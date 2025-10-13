package es.uab.tqs.f1_2D.controlador;

import es.uab.tqs.f1_2D.model.Car;

public class CarController {
    private Car car;

    public CarController(Car car) {
        this.car = car;
    }

    public void processInput(int keyCode) {
        car.movement(keyCode);
        car.update();
    }

    public Car getCar() {
        return car;
    }
}
