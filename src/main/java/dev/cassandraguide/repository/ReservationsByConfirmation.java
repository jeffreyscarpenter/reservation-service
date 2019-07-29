/*
 * Copyright (C) 2017-2019 Jeff Carpenter
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
package dev.cassandraguide.repository;

import dev.cassandraguide.model.Reservation;

import java.time.LocalDate;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.LOWER_CAMEL_CASE;

/**
 * Entity for mapping to the `reservations_by_confirmation' table.
 *
 * @author Jeff Carpenter
 */
@Entity
@NamingStrategy(convention = LOWER_CAMEL_CASE)
public class ReservationsByConfirmation {

    /** Hotel identifier, as Text not UUID (for simplicity). */
    private String hotelId;

    /** Formated as YYYY-MM-DD in interfaces. */
    private LocalDate startDate;

    /** Formated as YYYY-MM-DD in interfaces. */
    private LocalDate endDate;

    /** Room number. */
    // TODO: remove workaround for https://datastax-oss.atlassian.net/browse/JAVA-2324 when upgrading to 4.2 driver
    private Short roomNumber;

    /** UUID. */
    private UUID guestId;

    /** Confirmation for this Reservation. */
    @PartitionKey
    private String confirmationNumber;

    /**
     * Default constructor
     */
    public ReservationsByConfirmation() {
    }

    /**
     * Default constructor
     */
    public ReservationsByConfirmation(Reservation reservation) {
        setStartDate(reservation.getStartDate());
        setEndDate(reservation.getEndDate());
        setHotelId(reservation.getHotelId());
        setGuestId(reservation.getGuestId());
        setRoomNumber(reservation.getRoomNumber());
        setConfirmationNumber(reservation.getConfirmationNumber());
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
    public Short getRoomNumber() {
        return roomNumber;
    }

    /**
     * Setter accessor for attribute 'roomNumber'.
     * @param roomNumber
     *      new value for 'roomNumber '
     */
    public void setRoomNumber(Short roomNumber) {
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
