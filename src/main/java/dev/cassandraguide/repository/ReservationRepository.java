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
package dev.cassandraguide.repository;


import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createType;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import dev.cassandraguide.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.UserDefinedType;

/**
 * The goal of this project is to provide a minimally functional implementation of a microservice 
 * that uses Apache Cassandra for its data storage. The reservation service is implemented as a 
 * RESTful service using Spring Boot.
 *
 * @author Jeff Carpenter, Cedrick Lunven
 */
@Repository
@Profile("!unit-test") // When I do some 'unit-test' no connectivity to DB
public class ReservationRepository {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationRepository.class);
    
    // Reservation Schema Constants
    public static final CqlIdentifier TYPE_ADDRESS               = CqlIdentifier.fromCql("address");
    public static final CqlIdentifier TABLE_RESERVATION_BY_HOTEL_DATE =
            CqlIdentifier.fromCql("reservations_by_hotel_date");
    public static final CqlIdentifier TABLE_RESERVATION_BY_CONFI = CqlIdentifier.fromCql("reservations_by_confirmation");
    public static final CqlIdentifier TABLE_RESERVATION_BY_GUEST = CqlIdentifier.fromCql("reservations_by_guest");
    public static final CqlIdentifier TABLE_GUESTS               = CqlIdentifier.fromCql("guests");
    public static final CqlIdentifier STREET                     = CqlIdentifier.fromCql("street");
    public static final CqlIdentifier CITY                       = CqlIdentifier.fromCql("city");
    public static final CqlIdentifier STATE_PROVINCE             = CqlIdentifier.fromCql("state_or_province");
    public static final CqlIdentifier POSTAL_CODE                = CqlIdentifier.fromCql("postal_code");
    public static final CqlIdentifier COUNTRY                    = CqlIdentifier.fromCql("country");
    public static final CqlIdentifier HOTEL_ID                   = CqlIdentifier.fromCql("hotel_id");
    public static final CqlIdentifier START_DATE                 = CqlIdentifier.fromCql("start_date");
    public static final CqlIdentifier END_DATE                   = CqlIdentifier.fromCql("end_date");
    public static final CqlIdentifier ROOM_NUMBER                = CqlIdentifier.fromCql("room_number");
    public static final CqlIdentifier CONFIRM_NUMBER             = CqlIdentifier.fromCql("confirm_number");
    public static final CqlIdentifier GUEST_ID                   = CqlIdentifier.fromCql("guest_id");
    public static final CqlIdentifier GUEST_LAST_NAME            = CqlIdentifier.fromCql("guest_last_name");
    public static final CqlIdentifier FIRSTNAME                  = CqlIdentifier.fromCql("first_name");
    public static final CqlIdentifier LASTNAME                   = CqlIdentifier.fromCql("last_name");
    public static final CqlIdentifier TITLE                      = CqlIdentifier.fromCql("title");
    public static final CqlIdentifier EMAILS                     = CqlIdentifier.fromCql("emails");
    public static final CqlIdentifier PHONE_NUMBERS              = CqlIdentifier.fromCql("phone_numbers");
    public static final CqlIdentifier ADDRESSES                  = CqlIdentifier.fromCql("addresses");

    
    /** CqlSession holding metadata to interact with Cassandra. */
    private CqlSession     cqlSession;
    private CqlIdentifier  keyspaceName;

    // TODO: note variable to store Reservation DAO
    /** Data Access Object for reservation data */
    private ReservationDao reservationDao;

    /** External Initialization. */
    public ReservationRepository(
            @NonNull CqlSession cqlSession, 
            @Qualifier("keyspace") @NonNull CqlIdentifier keyspaceName) {
        this.cqlSession   = cqlSession;
        this.keyspaceName = keyspaceName;
        
        // Will create tables (if they do not exist)
        createReservationTables();

        // TODO: create mapper / DAO
        reservationDao = new ReservationMapperBuilder(cqlSession).build().reservationDao();

        logger.info("Application initialized.");
    }
    
    /**
     * CqlSession is a stateful object handling TCP connections to nodes in the cluster.
     * This operation properly closes sockets when the application is stopped
     */
    @PreDestroy
    public void cleanup() {
        if (null != cqlSession) {
            cqlSession.close();
            logger.info("+ CqlSession has been successfully closed");
        }
    }
    
    /**
     * Testing existence is useful for building correct semantics in the RESTful API. To evaluate existence find the
     * table where confirmation number is partition key which is reservations_by_confirmation
     * 
     * @param confirmationNumber
     *      unique identifier for confirmation
     * @return
     *      true if the reservation exists, false if it does not
     */
    public boolean exists(String confirmationNumber) {
        // TODO: note shortcut to implement using other operation
        return findByConfirmationNumber(confirmationNumber).isPresent();
    }
    
    /**
     * Similar to exists() but maps and parses results.
     * 
     * @param confirmationNumber
     *      unique identifier for confirmation
     * @return
     *      reservation if present or empty
     */
    @NonNull
    public Optional<Reservation> findByConfirmationNumber(@NonNull String confirmationNumber) {
        
        // TODO: use DAO to find the reservation
        ReservationsByConfirmation reservationsByConfirmation = null; // WRITE ME
        
        // Hint: an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists
        if (reservationsByConfirmation == null) {
            logger.debug("Unable to load reservation with confirmation number: " + confirmationNumber);
            return Optional.empty();
        }
        
        // Hint: If there is a result, create a new reservation object and set the values
        // Bonus: factor the logic to extract a reservation from a row into a separate method
        // (you will reuse it again later in getAllReservations())
        return Optional.of(mapReservationByConfirmationToReservation(reservationsByConfirmation));
    }
    
    /**
     * Create new entry in multiple tables for this reservation.
     *
     * @param reservation
     *      current reservation object
     * @return
     *      confirmation number for the reservation
     *      
     */
     public String upsert(Reservation reservation) {
        Objects.requireNonNull(reservation);
        if (null == reservation.getConfirmationNumber()) {
            // Generating a new reservation number if none has been provided
            reservation.setConfirmationNumber(UUID.randomUUID().toString());
        }
        // TODO: use DAO to save the reservation
        // Insert into 'reservations_by_hotel_date'

        // Insert into 'reservations_by_confirmation'

        return reservation.getConfirmationNumber();
    }

    /**
     * We pick 'reservations_by_confirmation' table to list reservations
     * BUT we could have used 'reservations_by_hotel_date' (as no key provided in request)
     *  
     * @return
     *      list containing all reservations
     */
    public List<Reservation> findAll() {
        // TODO: review usage of DAO to load all reservations
        return reservationDao.findAll()
                  .all()                          // no paging we retrieve all objects
                  .stream()                       // because we are good people
                  .map(this::mapReservationByConfirmationToReservation) // Mapping row as Reservation
                  .collect(Collectors.toList());  // Back to list objects
    }
      
    /**
     * Deleting a reservation.
     *
     * @param confirmationNumber
     *      unique identifier for confirmation.
     */
    public boolean delete(String confirmationNumber) {

        // Retrieving entire reservation in order to obtain the attributes we will need to delete from
        // reservations_by_hotel_date table
        Optional<Reservation> reservationToDelete = this.findByConfirmationNumber(confirmationNumber);

        if (reservationToDelete.isPresent()) {

            // TODO: Use the DAO to delete the reservation
            // Delete from 'reservations_by_hotel_date'

            // Delete from 'reservations_by_confirmation'

            return true;
        }
        return false;
    }
    
    /**
     * Search all reservation for an hotel id and LocalDate.
     *
     * @param hotelId
     *      hotel identifier
     * @param date
     *      searched Date
     * @return
     *      list of reservations matching the search criteria
     */
    public List<Reservation> findByHotelAndDate(String hotelId, LocalDate date) {
        Objects.requireNonNull(hotelId);
        Objects.requireNonNull(date);

        // TODO: review usage of DAO to find reservations
        return reservationDao.findByHotelDate(hotelId, date)
                         .all()                          // no paging we retrieve all objects
                         .stream()                       // because we are good people
                         .map(this::mapReservationByHotelDateToReservation) // Mapping row as Reservation
                         .collect(Collectors.toList());  // Back to list objects
    }

    /**
     * Utility method to marshal a row as expected Reservation Bean.
     *
     * @param reservationsByConfirmation
     *      entity class returned from mapper
     * @return
     *      reservation object
     */
    private Reservation mapReservationByConfirmationToReservation(ReservationsByConfirmation reservationsByConfirmation) {
        Reservation reservation = new Reservation();
        reservation.setHotelId(reservationsByConfirmation.getHotelId());
        reservation.setConfirmationNumber(reservationsByConfirmation.getConfirmNumber());
        reservation.setGuestId(reservationsByConfirmation.getGuestId());
        reservation.setRoomNumber(reservationsByConfirmation.getRoomNumber());
        reservation.setStartDate(reservationsByConfirmation.getStartDate());
        reservation.setEndDate(reservationsByConfirmation.getEndDate());
        return reservation;
    }

    /**
     * Utility method to marshal a row as expected Reservation Bean.
     *
     * @param reservationsByHotelDate
     *      entity class returned from mapper
     * @return
     *      reservation object
     */
    private Reservation mapReservationByHotelDateToReservation(ReservationsByHotelDate reservationsByHotelDate) {
        Reservation reservation = new Reservation();
        reservation.setHotelId(reservationsByHotelDate.getHotelId());
        reservation.setConfirmationNumber(reservationsByHotelDate.getConfirmNumber());
        reservation.setGuestId(reservationsByHotelDate.getGuestId());
        reservation.setRoomNumber(reservationsByHotelDate.getRoomNumber());
        reservation.setStartDate(reservationsByHotelDate.getStartDate());
        reservation.setEndDate(reservationsByHotelDate.getEndDate());
        return reservation;
    }

    /**
     * Create Keyspace and relevant tables as per defined in 'reservation.cql'
     */
    public void createReservationTables() {
        
        /**
         * Create TYPE 'Address' if not exists
         * 
         * CREATE TYPE reservation.address (
         *   street text,
         *   city text,
         *   state_or_province text,
         *   postal_code text,
         *   country text
         * );
         */
        cqlSession.execute(
                createType(keyspaceName, TYPE_ADDRESS)
                .ifNotExists()
                .withField(STREET, DataTypes.TEXT)
                .withField(CITY, DataTypes.TEXT)
                .withField(STATE_PROVINCE, DataTypes.TEXT)
                .withField(POSTAL_CODE, DataTypes.TEXT)
                .withField(COUNTRY, DataTypes.TEXT)
                .build());
        logger.debug("+ Type '{}' has been created (if needed)", TYPE_ADDRESS.asInternal());
        
        /** 
         * CREATE TABLE reservation.reservations_by_hotel_date (
         *  hotel_id text,
         *  start_date date,
         *  end_date date,
         *  room_number smallint,
         *  confirm_number text,
         *  guest_id uuid,
         *  PRIMARY KEY ((hotel_id, start_date), room_number)
         * );
         */
        cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE)
                        .ifNotExists()
                        .withPartitionKey(HOTEL_ID, DataTypes.TEXT)
                        .withPartitionKey(START_DATE, DataTypes.DATE)
                        .withClusteringColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                        .withColumn(END_DATE, DataTypes.DATE)
                        .withColumn(CONFIRM_NUMBER, DataTypes.TEXT)
                        .withColumn(GUEST_ID, DataTypes.UUID)
                        .withClusteringOrder(ROOM_NUMBER, ClusteringOrder.ASC)
                        .withComment("Q7. Find reservations by hotel and date")
                        .build());
        logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_HOTEL_DATE.asInternal());
        
        /**
         * CREATE TABLE reservation.reservations_by_confirmation (
         *   confirm_number text PRIMARY KEY,
         *   hotel_id text,
         *   start_date date,
         *   end_date date,
         *   room_number smallint,
         *   guest_id uuid
         * );
         */
        cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_CONFI)
                .ifNotExists()
                .withPartitionKey(CONFIRM_NUMBER, DataTypes.TEXT)
                .withColumn(HOTEL_ID, DataTypes.TEXT)
                .withColumn(START_DATE, DataTypes.DATE)
                .withColumn(END_DATE, DataTypes.DATE)
                .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                .withColumn(GUEST_ID, DataTypes.UUID)
                .build());
         logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_CONFI.asInternal());
         
         /**
          * CREATE TABLE reservation.reservations_by_guest (
          *  guest_last_name text,
          *  hotel_id text,
          *  start_date date,
          *  end_date date,
          *  room_number smallint,
          *  confirm_number text,
          *  guest_id uuid,
          *  PRIMARY KEY ((guest_last_name), hotel_id)
          * );
          */
         cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_GUEST)
                 .ifNotExists()
                 .withPartitionKey(GUEST_LAST_NAME, DataTypes.TEXT)
                 .withClusteringColumn(HOTEL_ID, DataTypes.TEXT)
                 .withColumn(START_DATE, DataTypes.DATE)
                 .withColumn(END_DATE, DataTypes.DATE)
                 .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                 .withColumn(CONFIRM_NUMBER, DataTypes.TEXT)
                 .withColumn(GUEST_ID, DataTypes.UUID)
                 .withComment("Q8. Find reservations by guest name")
                 .build());
          logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_GUEST.asInternal());
          
          /**
           * CREATE TABLE reservation.guests (
           *   guest_id uuid PRIMARY KEY,
           *   first_name text,
           *   last_name text,
           *   title text,
           *   emails set<text>,
           *   phone_numbers list<text>,
           *   addresses map<text, frozen<address>>,
           *   confirm_number text
           * );
           */
          UserDefinedType  udtAddressType = 
                  cqlSession.getMetadata().getKeyspace(keyspaceName).get() // Retrieving KeySpaceMetadata
                            .getUserDefinedType(TYPE_ADDRESS).get();        // Looking for UDT (extending DataType)
          cqlSession.execute(createTable(keyspaceName, TABLE_GUESTS)
                  .ifNotExists()
                  .withPartitionKey(GUEST_ID, DataTypes.UUID)
                  .withColumn(FIRSTNAME, DataTypes.TEXT)
                  .withColumn(LASTNAME, DataTypes.TEXT)
                  .withColumn(TITLE, DataTypes.TEXT)
                  .withColumn(EMAILS, DataTypes.setOf(DataTypes.TEXT))
                  .withColumn(PHONE_NUMBERS, DataTypes.listOf(DataTypes.TEXT))
                  .withColumn(ADDRESSES, DataTypes.mapOf(DataTypes.TEXT, udtAddressType, true))
                  .withColumn(CONFIRM_NUMBER, DataTypes.TEXT)
                  .withComment("Q9. Find guest by ID")
                  .build());
           logger.debug("+ Table '{}' has been created (if needed)", TABLE_GUESTS.asInternal());
           logger.info("Schema has been successfully initialized.");
    }
}