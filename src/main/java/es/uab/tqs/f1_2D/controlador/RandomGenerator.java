package es.uab.tqs.f1_2D.controlador;

import java.util.Random;

//Clase mockeada para poder testear
public class RandomGenerator 
{
    private final Random random;
    
    public RandomGenerator() 
    {
        this.random = new Random();
    }
    
    public RandomGenerator(long seed) 
    {
        this.random = new Random(seed);
    }
    
    public double nextDouble() 
    {
        return random.nextDouble();
    }
}