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

import java.util.UUID;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Entity working with Reservation on Cassandra.
 *
 * @author Cedrick Lunven
 */
@ApiModel(value="ReservationConfirmation", 
description="ReservationConfirmation is used when a confirmation has been generated")

public class ReservationConfirmation extends ReservationRequest {

    /** Serial. */
    private static final long serialVersionUID = -3392237616280919281L;
    
    /** Confirmation for this Reservation. */
    @ApiModelProperty(value = "Confirmation number as a UUID", example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366")
    private UUID confirmationNumber;
    
    /**
     * Default constructor
     */
    public ReservationConfirmation() {
    }
    
    /**
     * Default constructor
     */
    public ReservationConfirmation(ReservationRequest form) {
        setStartDate(form.getStartDate());
        setEndDate(form.getEndDate());
        setHotelId(form.getHotelId());
        setGuestId(form.getGuestId());
        setRoomNumber(form.getRoomNumber());
    }
    
    /**
     * Default constructor
     */
    public ReservationConfirmation(ReservationRequest form, UUID confirmationNumber) {
        this(form);
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

    /**
     * Getter accessor for attribute 'confirmationNumber'.
     *
     * @return
     *       current value of 'confirmationNumber'
     */
    public UUID getConfirmationNumber() {
        return confirmationNumber;
    }

    /**
     * Setter accessor for attribute 'confirmationNumber'.
     * @param confirmationNumber
     * 		new value for 'confirmationNumber '
     */
    public void setConfirmationNumber(UUID confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }
}
