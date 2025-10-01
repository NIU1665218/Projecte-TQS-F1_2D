package es.uab.tqs.f1_2D.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CarTest
{
    Car car;

    @BeforeEach
    public void setUp()
    {
        car = new Car(0,0,0f,0,200,0,400,400);
    }

    @Test
    public void VelocitatTest()
    {
        assertTrue(car.getVelocity()<car.getMaxVelocity());
        car.setVelocity(400);
        assertTrue(car.getVelocity()<car.getMaxVelocity());


    }


}