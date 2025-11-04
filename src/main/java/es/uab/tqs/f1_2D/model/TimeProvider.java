package es.uab.tqs.f1_2D.model;

//Interface para poder hacer test del cronometro con mock, necessita classe intermitja
@FunctionalInterface
public interface TimeProvider 
{
    long now();
}
