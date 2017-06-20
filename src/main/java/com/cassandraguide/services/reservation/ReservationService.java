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

import com.datastax.driver.core.*;

// TODO: Add import for LocalDateCodec

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import javax.annotation.PostConstruct;

@Component
public class ReservationService {

    @Autowired
    CassandraConfiguration cassandraConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    // private variable to hold DataStax Java Driver Session - used for executing queries
    private Session session;

    private PreparedStatement reservationsByConfirmationSelectPrepared;
    private PreparedStatement reservationsByConfirmationSelectAllPrepared;

    private PreparedStatement reservationsByHotelDateInsertPrepared;
    private PreparedStatement reservationsByHotelDateUpdatePrepared;
    private PreparedStatement reservationsByHotelDateDeletePrepared;
    private PreparedStatement reservationsByHotelDateSelectPrepared;

    public ReservationService() {}

    @PostConstruct
    public void init() {

        // Create query options to set default consistency level according to configured value
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.valueOf(
                cassandraConfiguration.getDefaultConsistencyLevel()));

        Cluster cluster = Cluster.builder()
                .addContactPoints(cassandraConfiguration.getCassandraNodes())
                .withQueryOptions(queryOptions)
                .build();

        // TODO: add LocalDateCodec to the Cluster's configuration

        // Create session for reservation keyspace
        session = cluster.connect("reservation");

        // Create prepared statements
        reservationsByConfirmationSelectPrepared = session.prepare(
                "SELECT * FROM reservations_by_confirmation where confirmation_number=?");

        reservationsByConfirmationSelectAllPrepared = session.prepare(
                "SELECT * FROM reservations_by_confirmation");

        reservationsByHotelDateInsertPrepared = session.prepare(
                "INSERT INTO reservations_by_hotel_date (confirmation_number, hotel_id, start_date, " +
                        "end_date, room_number, guest_id) VALUES (?, ?, ?, ?, ?, ?)");

        reservationsByHotelDateUpdatePrepared = session.prepare(
                "UPDATE reservations_by_hotel_date SET end_date=?, guest_id=? " +
                        "WHERE hotel_id=? AND start_date=? AND room_number=?");

        reservationsByHotelDateDeletePrepared = session.prepare(
                "DELETE FROM reservations_by_hotel_date WHERE hotel_id=? AND start_date=? AND room_number=?");

        reservationsByHotelDateSelectPrepared = session.prepare(
                "SELECT * FROM reservations_by_hotel_date WHERE hotel_id=? AND start_date=?");
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

        // TODO: Note we no longer need conversion method on start and end date
        Statement reservationsByHotelDateInsert = reservationsByHotelDateInsertPrepared.bind(
                reservation.getConfirmationNumber(),
                reservation.getHotelId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getRoomNumber(),
                reservation.getGuestId());

        // Execute the statement
        session.execute(reservationsByHotelDateInsert);

        // Return the confirmation number that was created
        return reservation.getConfirmationNumber();
    }

    public Reservation retrieveReservation(String confirmationNumber) {

        Reservation reservation = null;

        /*
         * Data Manipulation Logic
         */

        Statement reservationsByConfirmationSelect = null;

        // Use PreparedStatement to create a BoundStatement for retrieving the reservation
        // from the reservations_by_confirmation table
        reservationsByConfirmationSelect = reservationsByConfirmationSelectPrepared.bind(confirmationNumber);

        // Execute the statement
        ResultSet resultSet = session.execute(reservationsByConfirmationSelect);
        Row row = resultSet.one();

        // Process the results (ResultSet)
        // Hint: an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists
        if (row == null) {
            logger.debug("Unable to load reservation with confirmation number: " + confirmationNumber);
        }
        else {
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

        // TODO: Note we no longer need conversion method on end date
        Statement reservationsByHotelDateUpdate = reservationsByHotelDateUpdatePrepared.bind(
                reservation.getEndDate(),
                reservation.getRoomNumber(),
                reservation.getGuestId());

        // Execute the statement
        session.execute(reservationsByHotelDateUpdate);
    }

    public List<Reservation> getAllReservations() {

        // for populating return value
        List<Reservation> reservations = new ArrayList<Reservation>();

        /*
         * Data Manipulation Logic
         */
        Statement reservationsByConfirmationSelectAll = null;

        // Use PreparedStatement to create a BoundStatement for retrieving all reservations
        // from the reservations_by_confirmation table
        // Hint: there are no parameters to pass to bind
        reservationsByConfirmationSelectAll = reservationsByConfirmationSelectAllPrepared.bind();

        // Override the default consistency level to use consistency level "ONE" for this query
        reservationsByConfirmationSelectAll.setConsistencyLevel(ConsistencyLevel.ONE);
        
        // Execute the statement to get a result set
        ResultSet resultSet = session.execute(reservationsByConfirmationSelectAll);

        // Iterate over the rows in the result set, creating a reservation for each one
        for (Row row : resultSet) {
            reservations.add(extractReservationFromRow(row));
        }

        return reservations;
    }

    public List<Reservation> searchReservationsByHotelDate(String hotelId, LocalDate date) {

        // for populating return value
        List<Reservation> reservations = new ArrayList<Reservation>();

        /*
         * Data Manipulation Logic
         */
        Statement reservationsByHotelDateSelect = null;

        // TODO: Note we no longer need conversion method on (start) date
        reservationsByHotelDateSelect = reservationsByHotelDateSelectPrepared.bind(
                hotelId,
                date
        );

        // Override the default consistency level to use consistency level "ONE" for this query
        reservationsByHotelDateSelect.setConsistencyLevel(ConsistencyLevel.ONE);

        // Execute the statement to get a result set
        ResultSet resultSet = session.execute(reservationsByHotelDateSelect);

        // Iterate over the rows in the result set, creating a reservation for each one
        for (Row row : resultSet) {
            reservations.add(extractReservationFromRow(row));
        }

        return reservations;
    }

    public void deleteReservation(String confirmationNumber) {
        /*
         * Data Manipulation Logic
         */

        // retrieve the reservation so we can get attributes we need to delete from reservations_by_hotel_date
        Reservation reservation = retrieveReservation(confirmationNumber);
        if (reservation == null) return;

        // TODO: Note we no longer need conversion method on start date
        Statement reservationsByHotelDateDelete = reservationsByHotelDateDeletePrepared.bind(
                reservation.getHotelId(),
                reservation.getStartDate(),
                reservation.getRoomNumber());

        // Execute the statement
        session.execute(reservationsByHotelDateDelete);
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

    // TODO: note removal of conversion method we no longer need
    //private com.datastax.driver.core.LocalDate convertJavaLocalDateToDataStax(java.time.LocalDate date)
    //{
    //    if (date == null) return null;
    //    int year=date.getYear();
    //    int month = date.getMonthValue();
    //    int day = date.getDayOfMonth();
    //    return com.datastax.driver.core.LocalDate.fromYearMonthDay(year, month, day);
    //}

    // TODO: note removal of conversion method we no longer need
    //private java.time.LocalDate convertDataStaxLocalDateToJava(com.datastax.driver.core.LocalDate date)
    //{
    //    if (date == null) return null;
    //    return java.time.LocalDate.parse(date.toString());
    //}

    // method to extract a Reservation from a row
    private Reservation extractReservationFromRow(Row row) {
        Reservation reservation;
        reservation = new Reservation();
        reservation.setConfirmationNumber(row.getString("confirmation_number"));
        reservation.setHotelId(row.getString("hotel_id"));

        // TODO: note use of get(String, Class) operation to specify the desired output type
        reservation.setStartDate(row.get("start_date", java.time.LocalDate.class));
        reservation.setEndDate(row.get("end_date", java.time.LocalDate.class));

        reservation.setGuestId(row.getUUID("guest_id"));
        reservation.setRoomNumber(row.getShort("room_number"));
        return reservation;
    }
}
