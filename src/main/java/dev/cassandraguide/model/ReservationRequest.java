/*
 * Copyright (C) 2017-2020 Jeff Carpenter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.cassandraguide.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Reservation without the confirmation number.
 * 
 * Used to create a new reservation.
 *
 * @author Cedrick Lunven
 */
@ApiModel(value="ReservationRequest", 
          description="ReservationRequest is used when a confirmation number has not yet been provided")
public class ReservationRequest implements Serializable {

    /** Serial */
    private static final long serialVersionUID = -6906140014135798939L;
    
    /** Hotel identifier, as Text not param. */
    @ApiModelProperty(value = "Hotel identifier as plain text", example = "SFO-MAR")
    private String hotelId;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    @ApiModelProperty(value = "Start of the stay in YYYY-MM-DD", example = "2020-06-24")
    private LocalDate startDate;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    @ApiModelProperty(value = "End of the stay in YYYY-MM-DD", example = "2020-06-26")
    private LocalDate endDate;
    
    /** Room number. */
    @ApiModelProperty(value = "Hotel room number", example = "104")
    private short roomNumber;
    
    /** UUID. */
    @ApiModelProperty(value = "Guest identifier as a UUID", example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366")
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
