package com.cassandraguide.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Reservation without the confirmation number.
 * 
 * Help customer avoiding asking questions if they have to provide
 * confirmation number or not. 
 */
@ApiModel(value="ReservationRequest", 
          description="ReservationRequest creation when the confirmation number is not provided yet")
public class ReservationRequest implements Serializable {

    /** Serial */
    private static final long serialVersionUID = -6906140014135798939L;
    
    /** Hotel identifier, as Text not param. */
    @ApiModelProperty(value = "Hotel identifier as plain text", example = "SFO-MAR")
    private String hotelId;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    @ApiModelProperty(value = "Start of the stay in YYYY-MM-DD", example = "2019-06-24")
    private LocalDate startDate;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    @ApiModelProperty(value = "End of the stay in YYYY-MM-DD", example = "2019-06-26")
    private LocalDate endDate;
    
    /** Room number. */
    @ApiModelProperty(value = "Hotel real room number", example = "104")
    private short roomNumber;
    
    /** UUID. */
    @ApiModelProperty(value = "Guest identifier as an uuid", example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366")
    private UUID guestId;

    /**
     * Getter accessor for attribute 'hotelId'.
     *
     * @return
     *       current value of 'hotelId'
     */
    public String getHotelId() {
        return hotelId;
    }

    /**
     * Setter accessor for attribute 'hotelId'.
     * @param hotelId
     * 		new value for 'hotelId '
     */
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * Getter accessor for attribute 'startDate'.
     *
     * @return
     *       current value of 'startDate'
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Setter accessor for attribute 'startDate'.
     * @param startDate
     * 		new value for 'startDate '
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter accessor for attribute 'endDate'.
     *
     * @return
     *       current value of 'endDate'
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Setter accessor for attribute 'endDate'.
     * @param endDate
     * 		new value for 'endDate '
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter accessor for attribute 'roomNumber'.
     *
     * @return
     *       current value of 'roomNumber'
     */
    public short getRoomNumber() {
        return roomNumber;
    }

    /**
     * Setter accessor for attribute 'roomNumber'.
     * @param roomNumber
     * 		new value for 'roomNumber '
     */
    public void setRoomNumber(short roomNumber) {
        this.roomNumber = roomNumber;
    }

    /**
     * Getter accessor for attribute 'guestId'.
     *
     * @return
     *       current value of 'guestId'
     */
    public UUID getGuestId() {
        return guestId;
    }

    /**
     * Setter accessor for attribute 'guestId'.
     * @param guestId
     * 		new value for 'guestId '
     */
    public void setGuestId(UUID guestId) {
        this.guestId = guestId;
    }

}
