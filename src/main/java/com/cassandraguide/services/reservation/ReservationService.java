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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// DataStax Java Driver imports

import javax.annotation.PostConstruct;

@Component
public class ReservationService {

    @Autowired
    CassandraConfiguration cassandraConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    // private variable to hold DataStax Java Driver Session - used for executing queries
    private Session session;

    // declare variables for prepared statements
    private PreparedStatement reservationsByConfirmationInsertPrepared;
    private PreparedStatement reservationsByConfirmationSelectPrepared;
    private PreparedStatement reservationsByConfirmationUpdatePrepared;
    private PreparedStatement reservationsByConfirmationSelectAllPrepared;
    private PreparedStatement reservationsByConfirmationDeletePrepared;
    private PreparedStatement reservationsByHotelDateInsertPrepared;
    private PreparedStatement reservationsByHotelDateUpdatePrepared;
    private PreparedStatement reservationsByHotelDateDeletePrepared;

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

        // Create session for reservation keyspace
        session = cluster.connect("reservation");

        // Create prepared statements

        // TODO: add lightweight transaction to ensure unique confirmation number
        reservationsByConfirmationInsertPrepared = session.prepare(
                "INSERT INTO reservations_by_confirmation (confirmation_number, hotel_id, start_date, " +
                        "end_date, room_number, guest_id) VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS");

        reservationsByConfirmationSelectPrepared = session.prepare(
                "SELECT * FROM reservations_by_confirmation where confirmation_number=?");

        reservationsByConfirmationSelectAllPrepared = session.prepare(
                "SELECT * FROM reservations_by_confirmation");

        // TODO: add lightweight transaction to ensure no changes made to primary key columns of other table
        reservationsByConfirmationUpdatePrepared = session.prepare(
                "UPDATE reservations_by_confirmation SET end_date=?, guest_id=? WHERE confirmation_number=? " +
                        "IF hotel_id=? AND start_date=? AND room_number=?");

        reservationsByConfirmationDeletePrepared = session.prepare(
                "DELETE FROM reservations_by_confirmation WHERE confirmation_number=?");

        reservationsByHotelDateInsertPrepared = session.prepare(
                "INSERT INTO reservations_by_hotel_date (confirmation_number, hotel_id, start_date, " +
                        "end_date, room_number, guest_id) VALUES (?, ?, ?, ?, ?, ?)");

        // TODO: add lightweight transaction to make sure confirmation number is not changed  IF confirmation_number=?
        reservationsByHotelDateUpdatePrepared = session.prepare(
                "UPDATE reservations_by_hotel_date SET end_date=?, guest_id=? " +
                        "WHERE hotel_id=? AND start_date=? AND room_number=?");

        reservationsByHotelDateDeletePrepared = session.prepare(
                "DELETE FROM reservations_by_hotel_date WHERE hotel_id=? AND start_date=? AND room_number=?");
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

        Statement reservationsByConfirmationInsert = reservationsByConfirmationInsertPrepared.bind(
                reservation.getConfirmationNumber(),
                reservation.getHotelId(),
                convertJavaLocalDateToDataStax(reservation.getStartDate()),
                convertJavaLocalDateToDataStax(reservation.getEndDate()),
                reservation.getRoomNumber(),
                reservation.getGuestId());

        Statement reservationsByHotelDateInsert = reservationsByHotelDateInsertPrepared.bind(
                reservation.getConfirmationNumber(),
                reservation.getHotelId(),
                convertJavaLocalDateToDataStax(reservation.getStartDate()),
                convertJavaLocalDateToDataStax(reservation.getEndDate()),
                reservation.getRoomNumber(),
                reservation.getGuestId());

        BatchStatement reservationBatchInsert = new BatchStatement();
        reservationBatchInsert.add(reservationsByConfirmationInsert);
        reservationBatchInsert.add(reservationsByHotelDateInsert);

        // Execute the batch
        session.execute(reservationBatchInsert);

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

        // TODO: Note that we can't include lightweight transactions in a batch, so we must execute separate statements
        Statement reservationsByConfirmationUpdate = null;

        // TODO: update binding to reference values compared in lightweight transaction
        reservationsByConfirmationUpdate = reservationsByConfirmationUpdatePrepared.bind(
                convertJavaLocalDateToDataStax(reservation.getEndDate()),
                reservation.getGuestId(),
                reservation.getConfirmationNumber(),
                reservation.getHotelId(),
                convertJavaLocalDateToDataStax(reservation.getStartDate()),
                reservation.getRoomNumber());

        session.execute(reservationsByConfirmationUpdate);

        Statement reservationsByHotelDateUpdate = null;

        // TODO: update binding with lightweight transaction to make sure confirmation number is not changed
        reservationsByHotelDateUpdate = reservationsByHotelDateUpdatePrepared.bind(
                convertJavaLocalDateToDataStax(reservation.getEndDate()),
                reservation.getGuestId(),
                reservation.getHotelId(),
                convertJavaLocalDateToDataStax(reservation.getStartDate()),
                reservation.getRoomNumber());

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

        // We will implement this in a later exercise
        throw new UnsupportedOperationException();
    }

    public void deleteReservation(String confirmationNumber) {
        /*
         * Data Manipulation Logic
         */

        // retrieve the reservation so we can get attributes we need to delete from reservations_by_hotel_date
        Reservation reservation = retrieveReservation(confirmationNumber);
        if (reservation == null) return;

        // Use PreparedStatement to create a BoundStatement for deleting the reservation
        // from the reservations_by_confirmation table
        Statement reservationsByConfirmationDelete = reservationsByConfirmationDeletePrepared.bind(confirmationNumber);

        Statement reservationsByHotelDateDelete = reservationsByHotelDateDeletePrepared.bind(
                reservation.getHotelId(),
                convertJavaLocalDateToDataStax(reservation.getStartDate()),
                reservation.getRoomNumber());

        BatchStatement reservationBatchDelete = new BatchStatement();
        reservationBatchDelete.add(reservationsByConfirmationDelete);
        reservationBatchDelete.add(reservationsByHotelDateDelete);

        // Execute the batch
        session.execute(reservationBatchDelete);
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
    private com.datastax.driver.core.LocalDate convertJavaLocalDateToDataStax(java.time.LocalDate date)
    {
        if (date == null) return null;
        int year=date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return com.datastax.driver.core.LocalDate.fromYearMonthDay(year, month, day);
    }

    // convenience method to be replaced in a later exercise by codecs
    private java.time.LocalDate convertDataStaxLocalDateToJava(com.datastax.driver.core.LocalDate date)
    {
        if (date == null) return null;
        return java.time.LocalDate.parse(date.toString());
    }

    // method to extract a Reservation from a row
    private Reservation extractReservationFromRow(Row row) {
        Reservation reservation;
        reservation = new Reservation();
        reservation.setConfirmationNumber(row.getString("confirmation_number"));
        reservation.setHotelId(row.getString("hotel_id"));
        reservation.setStartDate(convertDataStaxLocalDateToJava(row.getDate("start_date")));
        reservation.setEndDate(convertDataStaxLocalDateToJava(row.getDate("end_date")));
        reservation.setGuestId(row.getUUID("guest_id"));
        reservation.setRoomNumber(row.getShort("room_number"));
        return reservation;
    }
}
