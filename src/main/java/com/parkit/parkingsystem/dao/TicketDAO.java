package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Save ticket when a vehicle incoming
     * @param ticket store ParkingSpot Id, inTime and vehicle registration number (price 0 and outTime null)
     */
    public void saveTicket(Ticket ticket){
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            ps.execute();
            //return true;
        } catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);

        }
        //return false;
    }

    /**
     * get ticket while processing exiting vehicle
     * @param vehicleRegNumber the vehicle registration number entered by the user
     * @return Ticket
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }

        } catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
                    }
        return ticket;
    }

    /**
     * Update the ticket with outTime and the fare
     * @param ticket generated after incoming process
     * @return false if update is failing
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        } catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     * Check if the vehicle registration number is already in the DB
     * @param vehicleRegNumber user input
     * @return true if the vehicle registration is already in the DB
     */
    public boolean isRecurringVehicle(String vehicleRegNumber) {
        Connection con = null;
        int result = -1;
        boolean recurringVehicle = false;
        PreparedStatement ps = null ;
        ResultSet rs = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_RECURRING_VEHICLE);
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()){
                result = rs.getInt(1);
                if(result>0){
                    System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
                    recurringVehicle = true;
                }
            }
        } catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            logger.error("Error testing recurring vehicle",ex);
        }finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return recurringVehicle;
    }

    /**
     * Update inTime with 1h less ; used for unit test
     * @param ticket ticket used to store inTime and outTime to calculate fare
     */
    public void addFakeInTime(Ticket ticket){
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_FAKE_IN_TIME);
            ps.setTimestamp(1, (new Timestamp(ticket.getInTime().getTime())));
            ps.setInt(2,ticket.getId());
            ps.execute();
            //return true;
        } catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            logger.error("Error saving fakeInTime",ex);
        }finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);

        }
    }

}
