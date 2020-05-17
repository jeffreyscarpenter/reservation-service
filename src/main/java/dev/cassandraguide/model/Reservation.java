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

/**
 * Entity working with Reservation on Cassandra.
 *
 * @author Jeff Carpenter
 */
public class Reservation implements Serializable {

    /** Serial. */
    private static final long serialVersionUID = -3392237616280919281L;
    
    /** Hotel identifier, as Text not UUID (for simplicity). */
    private String hotelId;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    private LocalDate startDate;
    
    /** Formated as YYYY-MM-DD in interfaces. */
    private LocalDate endDate;
    
    /** Room number. */
    private short roomNumber;
    
    /** UUID. */
    private UUID guestId;
    
    /** Confirmation for this Reservation. */
    private String confirmationNumber;
    
    /**
     * Default constructor
     */
    public Reservation() {
    }
    
    /**
     * Default constructor
     */
    public Reservation(ReservationRequest form) {
        setStartDate(form.getStartDate());
        setEndDate(form.getEndDate());
        setHotelId(form.getHotelId());
        setGuestId(form.getGuestId());
        setRoomNumber(form.getRoomNumber());
    }
    
    /**
     * Default constructor
     */
    public Reservation(ReservationRequest form, String confirmationNumber) {
        this(form);
        this.confirmationNumber = confirmationNumber;
    }

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
     *      new value for 'hotelId '
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
     *      new value for 'startDate '
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
     *      new value for 'endDate '
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
     *      new value for 'roomNumber '
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
     *      new value for 'guestId '
     */
    public void setGuestId(UUID guestId) {
        this.guestId = guestId;
    }

    /**
     * Getter accessor for attribute 'confirmationNumber'.
     *
     * @return
     *       current value of 'confirmationNumber'
     */
    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    /**
     * Setter accessor for attribute 'confirmationNumber'.
     * @param confirmationNumber
     * 		new value for 'confirmationNumber '
     */
    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Confirmation Number = " + confirmationNumber +
                ", Hotel ID: " + getHotelId() +
                ", Start Date = " + getStartDate() +
                ", End Date = " + getEndDate() +
                ", Room Number = " + getRoomNumber() +
                ", Guest ID = " + getGuestId();
    }
}
