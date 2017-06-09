/*
 * Copyright (C) 2017 Jeff Carpenter
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
package com.cassandraguide.services.reservation;

import java.util.UUID;

import com.datastax.driver.core.LocalDate;

// TODO: Add imports for DataStax Mapper
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

// Class used to map a reservation to the reservations_by_confirmation table.
// TODO: Add Table annotation
@Table(keyspace = "reservation", name = "reservations_by_confirmation")
public class ReservationByConfirmation {


    // TODO: Add PartitionKey and Column annotation
    @PartitionKey
    @Column(name = "confirmation_number")
    private String confirmationNumber;

    // TODO: Add Column annotations
    @Column(name = "hotel_id")
    private String hotelId;

    @Column (name = "start_date")
    private LocalDate startDate;

    @Column (name = "end_date")
    private LocalDate endDate;

    @Column (name = "room_number")
    private short roomNumber;

    @Column (name = "guest_id")
    private UUID guestId;

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public short getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(short roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public UUID getGuestId() {
        return guestId;
    }

    public void setGuestId(UUID guestId) {
        this.guestId = guestId;
    }

    @Override
    public String toString() {
        return "Confirmation Number = " + confirmationNumber +
                ", Hotel ID: " + hotelId +
                ", Start Date = " + startDate +
                ", End Date = " + endDate +
                ", Room Number = " + roomNumber +
                ", Guest ID = " + guestId;
    }

}
