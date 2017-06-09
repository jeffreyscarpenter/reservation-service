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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// DataStax Java Driver imports
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import com.datastax.driver.mapping.Result;

// TODO: add imports for MappingManager, Mapper, Result

@Component
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    // private variable to hold DataStax Java Driver Session - used for executing queries
    private Session session;

    // TODO: private variable to hold mapper

    public ReservationService() {

        // Create cluster with connection to localhost
        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        // Create session for reservation keyspace
        session = cluster.connect("reservation");

        // TODO: Create Mapping Manager

        // TODO: Create reservation mapper for the mapping class
    }

    public String createReservation(Reservation reservation) {

        /*
         * Business Logic -
         *  you should not need to change this code, although in a real production service
         *  you'd likely do more validation than is done here
         */

        // Validate new reservation doesn't contain a confirmation number
        if (reservation.getConfirmationNumber() != null)
        {
            logger.error("Received new reservation containing confirmation number: " +
                    reservation.getConfirmationNumber());
            throw new IllegalArgumentException("New reservation cannot contain confirmation number");
        }

        // generate and set confirmation number
        reservation.setConfirmationNumber(getUniqueConfirmationNumber());

        /*
         * Data Manipulation Logic
         */

        ReservationByConfirmation reservationByConfirmation = null;

        // TODO: convert the provided Reservation to the mapping class
        // Hint: use convenience function provided below

        // TODO: use mapper to insert the reservation

        // Return the confirmation number that was created
        return reservation.getConfirmationNumber();
    }

    public Reservation retrieveReservation(String confirmationNumber) {

        Reservation reservation = null;

        /*
         * Data Manipulation Logic
         */
        ReservationByConfirmation reservationByConfirmation = null;

        // TODO: use mapper to retrieve the reservation

        // TODO: convert the mapping to a Reservation

        // Process the result
        // an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists
        if (reservationByConfirmation == null) {
            logger.debug("Unable to load reservation with confirmation number: " + confirmationNumber);
        }
        else {
            // TODO: convert result from mapping class to Reservation
            // Hint: use convenience function provided below
        }

        return reservation;
    }

    public void updateReservation(Reservation reservation) {
        /*
         * Business Logic -
         *  you should not need to change this code, although in a real production service
         *  you'd likely do more validation than is done here
         */

        // verify reservation exists
        if (retrieveReservation(reservation.getConfirmationNumber()) == null)
        {
            String errorMsg = "Unable to update unknown reservation with confirmation number: " +
                    reservation.getConfirmationNumber();
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        /*
         * Data Manipulation Logic
         */
        ReservationByConfirmation reservationByConfirmation = null;

        // TODO: convert the provided Reservation to the mapping class
        // Hint: use convenience function provided below

        // TODO: use mapper to update the reservation
        // Hint: this should look the same as the logic in createReservation()
    }

    public List<Reservation> getAllReservations() {

        // for populating return value
        List<Reservation> reservations = new ArrayList<Reservation>();

        /*
         * Data Manipulation Logic
         */
        Result<ReservationByConfirmation> reservationsByConfirmation = null;

        // TODO: use the session to retrieve all reservations
        // Hint: you can do this just by providing a string

        // TODO: use the mapper to convert the ResultSet to ReservationByConfirmation objects

        // Iterate over the results
        for (ReservationByConfirmation reservationByConfirmation : reservationsByConfirmation) {
            // TODO: create a Reservation for each ReservationByConfirmation
            // Hint: use convenience function provided below
            reservations.add(convertFromMappingClass(reservationByConfirmation));
        }

        return reservations;
    }

    public List<Reservation> searchReservationsByHotelDate(String hotelId, LocalDate date) {

        // We will implement this in a later exercise
        throw new UnsupportedOperationException();
    }

    public void deleteReservation(String confirmationNumber) {
        /*
         * Data Manipulation Logic
         */

        // TODO: use mapper to delete the reservation
    }

    // convenience method, you should not need to modify
    private String getUniqueConfirmationNumber()
    {
        String confirmationNumber;

        // Ensure uniqueness of the generated code by searching for a reservation with that code
        do {
            confirmationNumber = ConfirmationNumberGenerator.getConfirmationNumber();
        }
        while (retrieveReservation(confirmationNumber) != null);

        return confirmationNumber;
    }

    // convenience method to be replaced in a later exercise by codecs
    private static com.datastax.driver.core.LocalDate convertJavaLocalDateToDataStax(java.time.LocalDate date)
    {
        if (date == null) return null;
        int year=date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return com.datastax.driver.core.LocalDate.fromYearMonthDay(year, month, day);
    }

    // convenience method to be replaced in a later exercise by codecs
    private static java.time.LocalDate convertDataStaxLocalDateToJava(com.datastax.driver.core.LocalDate date)
    {
        if (date == null) return null;
        return java.time.LocalDate.parse(date.toString());
    }

    //

    // convenience method to convert a ReservationByConfirmation to a Reservation
    private static Reservation convertFromMappingClass(ReservationByConfirmation reservationByConfirmation) {
        Reservation reservation = new Reservation();
        reservation.setConfirmationNumber(reservationByConfirmation.getConfirmationNumber());
        reservation.setHotelId(reservationByConfirmation.getHotelId());
        reservation.setStartDate(convertDataStaxLocalDateToJava(reservationByConfirmation.getStartDate()));
        reservation.setEndDate(convertDataStaxLocalDateToJava(reservationByConfirmation.getEndDate()));
        reservation.setGuestId(reservationByConfirmation.getGuestId());
        reservation.setRoomNumber(reservationByConfirmation.getRoomNumber());
        return reservation;
    }

    // convenience method to convert a Reservation to a ReservationByConfirmation
    private static ReservationByConfirmation convertToMappingClass(Reservation reservation) {
        ReservationByConfirmation reservationByConfirmation = new ReservationByConfirmation();
        reservationByConfirmation.setConfirmationNumber(reservation.getConfirmationNumber());
        reservationByConfirmation.setHotelId(reservation.getHotelId());
        reservationByConfirmation.setStartDate(convertJavaLocalDateToDataStax(reservation.getStartDate()));
        reservationByConfirmation.setEndDate(convertJavaLocalDateToDataStax(reservation.getEndDate()));
        reservationByConfirmation.setGuestId(reservation.getGuestId());
        reservationByConfirmation.setRoomNumber(reservation.getRoomNumber());
        return reservationByConfirmation;
    }
}