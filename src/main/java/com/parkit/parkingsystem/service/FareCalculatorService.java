package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.Objects;

public class FareCalculatorService {

    /**
     * Calculate the fare, depending on the vehicle type and applying 5% discount for recurring vehicle
     * first 30 min are free
     * @param ticket used to calculate the fare
     */
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ Objects.requireNonNull(ticket.getOutTime()).toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();
        long duration = outHour - inHour ;

        double discountRate=1;
        if (duration<1800000) {
            discountRate = 0.0;
        } else if(ticket.getRecurringVehicle()) {
            discountRate = 0.95;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * discountRate / 3600000);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * discountRate / 3600000);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

        }
    }
