package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    @DisplayName("test incoming vehicle process")
    public void testParkingACar(){
        // check that a ticket is actualy saved in DB and Parking table is updated with availability
        //GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        assertNotNull(ticketDAO.getTicket("ABCDEF"));
        assertNotSame(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), ticketDAO.getTicket("ABCDEF").getParkingSpot().getId());
    }

    @Test
    @DisplayName("test exiting vehicle process")
    public void testParkingLotExit(){
        //check that the fare generated and out time are populated in the database
        // GIVEN
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // update inTime with 1h less so that inTime and outTime are different otherwise the test is not repeatable because randomly outTime is few ms after inTime
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date fakeInTime = ticket.getInTime();
        fakeInTime.setTime(fakeInTime.getTime() - (60 * 60 * 1000));
        ticket.setInTime(fakeInTime);
        ticketDAO.addFakeInTime(ticket);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
        assertNotEquals(0, ticketDAO.getTicket("ABCDEF").getPrice());
    }

}
