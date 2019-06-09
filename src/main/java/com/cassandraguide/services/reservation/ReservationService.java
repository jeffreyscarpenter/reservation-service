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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// DataStax Java Driver imports
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

@Component
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    // TODO: private variable to hold DataStax Java Driver Session - used for executing queries
    private CqlSession session;

    public ReservationService() {

        // TODO: Create session with connection to localhost for reservation keyspace
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withKeyspace(CqlIdentifier.fromCql("reservation"))
                .build();
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

        // TODO: Construct SimpleStatement for inserting the reservation
        // For this exercise we will insert only into the reservations_by_confirmation table
        // Hint: use provided convenience function convertJavaLocalDateToDataStax for start and end dates
        SimpleStatement reservationsByConfirmationInsert = SimpleStatement.builder(
                "INSERT INTO reservations_by_confirmation (confirmation_number, hotel_id, start_date, " +
                        "end_date, room_number, guest_id) VALUES (?, ?, ?, ?, ?, ?)")
                .addPositionalValue(reservation.getConfirmationNumber())
                .addPositionalValue(reservation.getHotelId())
                .addPositionalValue(reservation.getStartDate())
                .addPositionalValue(reservation.getEndDate())
                .addPositionalValue(reservation.getRoomNumber())
                .addPositionalValue(reservation.getGuestId())
                .build();

        // TODO: Execute the statement
        session.execute(reservationsByConfirmationInsert);

        // Return the confirmation number that was created
        return reservation.getConfirmationNumber();
    }

    public Reservation retrieveReservation(String confirmationNumber) {

        Reservation reservation = null;

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for retrieving the reservation from the reservations_by_confirmation table
        // Hint: Remember to use parameterization
        SimpleStatement reservationsByConfirmationSelect = SimpleStatement.builder(
                "SELECT * FROM reservations_by_confirmation where confirmation_number=?")
                .addPositionalValue(confirmationNumber)
                .build();

        // TODO: Execute the statement
        ResultSet resultSet = session.execute(reservationsByConfirmationSelect);
        Row row = resultSet.one();

        // TODO: Process the results (ResultSet)
        // Hint: an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists
        if (row == null) {
            logger.debug("Unable to load reservation with confirmation number: " + confirmationNumber);
        }
        else {
            // Hint: If there is a result, create a new reservation object and set the values
            // Hint: use provided convenience function convertDataStaxLocalDateToJava for start and end dates
            // Bonus: factor the logic to extract a reservation from a row into a separate method
            // (you will reuse it again later in getAllReservations())
            reservation = extractReservationFromRow(row);
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

        // TODO: Construct SimpleStatement for updating the reservation
        // For this exercise we will insert only into the reservations_by_confirmation table
        SimpleStatement reservationsByConfirmationUpdate = SimpleStatement.builder(
                "UPDATE reservations_by_confirmation SET hotel_id=?, start_date=?, " +
                        "end_date=?, room_number=?, guest_id=? WHERE confirmation_number=?")
                .addPositionalValue(reservation.getHotelId())
                .addPositionalValue(reservation.getStartDate())
                .addPositionalValue(reservation.getEndDate())
                .addPositionalValue(reservation.getRoomNumber())
                .addPositionalValue(reservation.getGuestId())
                .addPositionalValue(reservation.getConfirmationNumber())
                .build();

        // TODO: Execute the statement
        session.execute(reservationsByConfirmationUpdate);
    }

    public List<Reservation> getAllReservations() {

        // for populating return value
        List<Reservation> reservations = new ArrayList<Reservation>();

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for retrieving the entire contents of the reservations_by_confirmation table
        SimpleStatement reservationsByConfirmationSelectAll = SimpleStatement.newInstance(
                "SELECT * FROM reservations_by_confirmation");

        // TODO: Execute the statement to get a result set
        ResultSet resultSet = session.execute(reservationsByConfirmationSelectAll);

        // TODO: Iterate over the rows in the result set, creating a reservation for each one
        // Hint: find the logic you wrote for retrieveReservation() for processing a single row,
        // and refactor that into a method you can reuse here
        for (Row row : resultSet) {
            reservations.add(extractReservationFromRow(row));
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

        // TODO: Construct SimpleStatement for deleting the selected item from the reservations_by_confirmation table
        SimpleStatement reservationsByConfirmationDelete = SimpleStatement.builder(
                "DELETE * FROM reservations_by_confirmation WHERE confirmation_number=?")
                .addPositionalValue(confirmationNumber)
                .build();

        // TODO: Execute the statement
        session.execute(reservationsByConfirmationDelete);
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

    // TODO: method to extract a Reservation from a row
    private Reservation extractReservationFromRow(Row row) {
        Reservation reservation;
        reservation = new Reservation();
        reservation.setConfirmationNumber(row.getString("confirmation_number"));
        reservation.setHotelId(row.getString("hotel_id"));
        reservation.setStartDate(row.getLocalDate("start_date"));
        reservation.setEndDate(row.getLocalDate("end_date"));
        reservation.setGuestId(row.getUuid("guest_id"));
        reservation.setRoomNumber(row.getShort("room_number"));
        return reservation;
    }
}