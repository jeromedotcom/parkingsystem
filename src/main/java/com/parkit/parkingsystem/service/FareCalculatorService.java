package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        LocalDateTime inHour = ticket.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); // Converting java.util.Date to java.time.LocalDateTime
        LocalDateTime outHour = ticket.getOutTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.between(inHour, outHour);
        long durationInSeconds = duration.getSeconds(); // récupération du nombre de secondes contenus dans duration

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(durationInSeconds * Fare.CAR_RATE_PER_HOUR / 3600);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInSeconds * Fare.BIKE_RATE_PER_HOUR / 3600);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}