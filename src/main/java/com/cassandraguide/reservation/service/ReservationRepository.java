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
package com.cassandraguide.reservation.service;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.deleteFrom;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createKeyspace;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createType;
import static com.datastax.oss.driver.api.querybuilder.relation.Relation.column;

import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cassandraguide.reservation.model.Reservation;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;

/**
 * The goal of this project is to provide a minimally functional implementation of a microservice 
 * that uses Apache Cassandra for its data storage. The reservation service is implemented as a 
 * RESTful service using Spring Boot.
 */
@Repository("repository.reservation")
public class ReservationRepository {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationRepository.class);
    
    // Loading Cassandra Configuration fron 'application.properties' file
    
    // Contact point hostname
    @Value("${cassandra.contactpoint.hostname:127.0.0.1}")
    protected String cassandraHost;
    
    // Contact point port
    @Value("${cassandra.contactpoint.port:9042}")
    protected int cassandraPort;
    
    // DataCente name, required from v2.
    @Value("${cassandra.localDataCenterName:datacenter1 }")
    protected String localDataCenterName;  
    
    // KeySpace Name
    @Value("${cassandra.keyspaceName:reservation}")
    public String keyspaceName; 
    
    // Initialized from the injected name
    public static CqlIdentifier KEYSPACE_NAME;
    
    // Reservation Schema Constants
    public static final CqlIdentifier TYPE_ADDRESS               = CqlIdentifier.fromCql("address");
    public static final CqlIdentifier TABLE_RESERVATION_BY_HOTEL = CqlIdentifier.fromCql("reservations_by_hotel_date");
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
    public static final CqlIdentifier CONFIRMATION_NUMBER        = CqlIdentifier.fromCql("confirmation_number");
    public static final CqlIdentifier GUEST_ID                   = CqlIdentifier.fromCql("guest_id");
    public static final CqlIdentifier GUEST_LAST_NAME            = CqlIdentifier.fromCql("guest_last_name");
    public static final CqlIdentifier FIRSTNAME                  = CqlIdentifier.fromCql("first_name");
    public static final CqlIdentifier LASTNAME                   = CqlIdentifier.fromCql("last_name");
    public static final CqlIdentifier TITLE                      = CqlIdentifier.fromCql("title");
    public static final CqlIdentifier EMAILS                     = CqlIdentifier.fromCql("emails");
    public static final CqlIdentifier PHONE_NUMBERS              = CqlIdentifier.fromCql("phone_numbers");
    public static final CqlIdentifier ADDRESSES                  = CqlIdentifier.fromCql("addresses");
    
    /** CqlSession holding metadata to interact with Cassandra. */
    private CqlSession cqlSession;
    
    private PreparedStatement psExistReservation;
    private PreparedStatement psFindReservation;
    private PreparedStatement psInsertReservationByHotel;
    private PreparedStatement psInsertReservationByConfirmation;
    private PreparedStatement psDeleteReservation;
    private PreparedStatement psSearchReservation;
    
    @PostConstruct
    public void init() {
        logger.info("Starting Application...");
        createSchema();
        prepareStatements();
        logger.info("Application initialized.");
    }
    
    /**
     * Create Keyspace and relevant tables as per defined in 'reservation.cql'
     */
    private void createSchema() {
        
        logger.info("Creating Keyspace and expected table in Cassandra if not present:");
        
        // CqlSession is closable
        try (CqlSession cqlInitSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort))
                .withLocalDatacenter(localDataCenterName)
                .build()) {
           cqlInitSession.execute(createKeyspace(keyspaceName).ifNotExists().withSimpleStrategy(1).build());
          logger.info("+ Keyspace '{}' has been created (if needed)", keyspaceName);
        }
        
        // Persistent CqlSession, now logged to expected keyspace
        KEYSPACE_NAME = CqlIdentifier.fromCql(keyspaceName);
        cqlSession = CqlSession.builder().addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort))
                               .withKeyspace(KEYSPACE_NAME)
                               .withLocalDatacenter(localDataCenterName)
                               .build();
        logger.info("+ CqlSession has been initialized with keyspace '{}'", keyspaceName);
        
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
                createType(KEYSPACE_NAME, TYPE_ADDRESS)
                .ifNotExists()
                .withField(STREET, DataTypes.TEXT)
                .withField(CITY, DataTypes.TEXT)
                .withField(STATE_PROVINCE, DataTypes.TEXT)
                .withField(POSTAL_CODE, DataTypes.TEXT)
                .withField(COUNTRY, DataTypes.TEXT)
                .build());
        logger.info("+ Type '{}' has been created (if needed)", TYPE_ADDRESS.asInternal());
        
        /** 
         * CREATE TABLE reservation.reservations_by_hotel_date (
         *  hotel_id text,
         *  start_date date,
         *  end_date date,
         *  room_number smallint,
         *  confirmation_number text,
         *  guest_id uuid,
         *  PRIMARY KEY ((hotel_id, start_date), room_number)
         * ) WITH comment = 'Q7. Find reservations by hotel and date';
         */
        cqlSession.execute(createTable(KEYSPACE_NAME, TABLE_RESERVATION_BY_HOTEL)
                        .ifNotExists()
                        .withPartitionKey(HOTEL_ID, DataTypes.TEXT)
                        .withPartitionKey(START_DATE, DataTypes.DATE)
                        .withClusteringColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                        .withColumn(END_DATE, DataTypes.DATE)
                        .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
                        .withColumn(GUEST_ID, DataTypes.UUID)
                        .withClusteringOrder(ROOM_NUMBER, ClusteringOrder.ASC)
                        .withComment("Q7. Find reservations by hotel and date")
                        .build());
        logger.info("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_HOTEL.asInternal());
        
        /**
         * CREATE TABLE reservation.reservations_by_confirmation (
         *   confirmation_number text PRIMARY KEY,
         *   hotel_id text,
         *   start_date date,
         *   end_date date,
         *   room_number smallint,
         *   guest_id uuid
         * );
         */
        cqlSession.execute(createTable(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI)
                .ifNotExists()
                .withPartitionKey(CONFIRMATION_NUMBER, DataTypes.TEXT)
                .withColumn(HOTEL_ID, DataTypes.TEXT)
                .withColumn(START_DATE, DataTypes.DATE)
                .withColumn(END_DATE, DataTypes.DATE)
                .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                .withColumn(GUEST_ID, DataTypes.UUID)
                .build());
         logger.info("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_CONFI.asInternal());
         
         /**
          * CREATE TABLE reservation.reservations_by_guest (
          *  guest_last_name text,
          *  hotel_id text,
          *  start_date date,
          *  end_date date,
          *  room_number smallint,
          *  confirmation_number text,
          *  guest_id uuid,
          *  PRIMARY KEY ((guest_last_name), hotel_id)
          * ) WITH comment = 'Q8. Find reservations by guest name';
          */
         cqlSession.execute(createTable(KEYSPACE_NAME, TABLE_RESERVATION_BY_GUEST)
                 .ifNotExists()
                 .withPartitionKey(GUEST_LAST_NAME, DataTypes.TEXT)
                 .withClusteringColumn(HOTEL_ID, DataTypes.TEXT)
                 .withColumn(START_DATE, DataTypes.DATE)
                 .withColumn(END_DATE, DataTypes.DATE)
                 .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
                 .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
                 .withColumn(GUEST_ID, DataTypes.UUID)
                 .withComment("Q8. Find reservations by guest name")
                 .build());
          logger.info("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_GUEST.asInternal());
          
          /**
           * CREATE TABLE reservation.guests (
           *   guest_id uuid PRIMARY KEY,
           *   first_name text,
           *   last_name text,
           *   title text,
           *   emails set<text>,
           *   phone_numbers list<text>,
           *   addresses map<text, frozen<address>>,
           *   confirmation_number text
           * ) WITH comment = 'Q9. Find guest by ID';
           */
          UserDefinedType  udtAddressType = 
                  cqlSession.getMetadata().getKeyspace(KEYSPACE_NAME).get() // Retrieving KeySpaceMetadata
                            .getUserDefinedType(TYPE_ADDRESS).get();        // Looking for UDT (extending DataType)
          cqlSession.execute(createTable(KEYSPACE_NAME, TABLE_GUESTS)
                  .ifNotExists()
                  .withPartitionKey(GUEST_ID, DataTypes.UUID)
                  .withColumn(FIRSTNAME, DataTypes.TEXT)
                  .withColumn(LASTNAME, DataTypes.TEXT)
                  .withColumn(TITLE, DataTypes.TEXT)
                  .withColumn(EMAILS, DataTypes.setOf(DataTypes.TEXT))
                  .withColumn(PHONE_NUMBERS, DataTypes.listOf(DataTypes.TEXT))
                  .withColumn(ADDRESSES, DataTypes.mapOf(DataTypes.TEXT, udtAddressType, true))
                  .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
                  .withComment("Q9. Find guest by ID")
                  .build());
           logger.info("+ Table '{}' has been created (if needed)", TABLE_GUESTS.asInternal());
           logger.info("Schema has been successfully initialized.");
    }

    private void prepareStatements() {
        psExistReservation = cqlSession.prepare(
                            selectFrom(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI).column(CONFIRMATION_NUMBER)
                            .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                            .build());
        psFindReservation = cqlSession.prepare(
                            selectFrom(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI).all()
                            .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                            .build());
        psSearchReservation = cqlSession.prepare(
                            selectFrom(KEYSPACE_NAME, TABLE_RESERVATION_BY_HOTEL).all()
                            .where(column(HOTEL_ID).isEqualTo(bindMarker(HOTEL_ID)))
                            .where(column(START_DATE).isEqualTo(bindMarker(START_DATE)))
                            .build());
        psDeleteReservation = cqlSession.prepare(
                            deleteFrom(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI)
                            .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                            .build());
        psInsertReservationByHotel = cqlSession.prepare(QueryBuilder.insertInto(KEYSPACE_NAME, TABLE_RESERVATION_BY_HOTEL)
                .value(HOTEL_ID, bindMarker(HOTEL_ID))
                .value(START_DATE, bindMarker(START_DATE))
                .value(END_DATE, bindMarker(END_DATE))
                .value(ROOM_NUMBER, bindMarker(ROOM_NUMBER))
                .value(CONFIRMATION_NUMBER, bindMarker(CONFIRMATION_NUMBER))
                .value(GUEST_ID, bindMarker(GUEST_ID))
                .build());
        psInsertReservationByConfirmation = cqlSession.prepare(QueryBuilder.insertInto(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI)
                .value(CONFIRMATION_NUMBER, bindMarker(CONFIRMATION_NUMBER))
                .value(HOTEL_ID, bindMarker(HOTEL_ID))
                .value(START_DATE, bindMarker(START_DATE))
                .value(END_DATE, bindMarker(END_DATE))
                .value(ROOM_NUMBER, bindMarker(ROOM_NUMBER))
                .value(GUEST_ID, bindMarker(GUEST_ID))
                .build());
    }
    
    /**
     * CqlSession is a stateful object handling TCP connection.
     * You may want to properly close sockets when you close you application
     */
    @PreDestroy
    public void cleanup() {
        if (null != cqlSession) {
            cqlSession.close();
            logger.info("+ CqlSession has been successfully closed");
        }
    }
    
    /**
     * Testing existence is relevant to avoid mapping. To evaluate existence find the table 
     * where confirnation number is partition key which is reservations_by_confirmation
     * 
     * @param confirmationNumber
     *      unique identifier for confirmation
     * @return
     *      if the reservation exist or not
     */
    public boolean exists(String confirmationNumber) {
        return cqlSession.execute(psExistReservation.bind(CONFIRMATION_NUMBER, confirmationNumber))
                         .getAvailableWithoutFetching() > 0;
    }
    
    /**
     * Close from testing existence with Mapping and parsing of results.
     * 
     * @param confirmationNumber
     *      unique identifier for confirmation
     * @return
     *      reservation if present or empty
     */
    public Optional<Reservation> findByConfirmationNumber(String confirmationNumber) {
        
        ResultSet resultSet = cqlSession.execute(
                psFindReservation.bind(CONFIRMATION_NUMBER, confirmationNumber));
        
        // Hint: an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists
        Row row = resultSet.one();
        if (row == null) {
            logger.debug("Unable to load reservation with confirmation number: " + confirmationNumber);
            return Optional.empty();
        }
        
        // Hint: If there is a result, create a new reservation object and set the values
        // Hint: use provided convenience function convertDataStaxLocalDateToJava for start and end dates
        // Bonus: factor the logic to extract a reservation from a row into a separate method
        // (you will reuse it again later in getAllReservations())
        return Optional.of(mapRowToReservation(row));
    }
    
    /**
     * Create new entry in multiple tables for this reservation.
     *
     * @param reservation
     *      current reservation object
     * @return
     *      
     */
     public void upsert(Reservation r) {
        Objects.requireNonNull(r);
        if (null == r.getConfirmationNumber()) {
            // Generating a new reservation number if none has been provided
            r.setConfirmationNumber(UUID.randomUUID().toString());
        }
        // Insert into 'reservations_by_hotel_date'
        BoundStatement bsInsertReservationByHotel = 
                psInsertReservationByHotel.bind(r.getHotelId(), r.getStartDate(), r.getEndDate(), 
                                                r.getRoomNumber(), r.getConfirmationNumber(), r.getGuestId());
        // Insert into 'reservations_by_confirmationumber'
        BoundStatement bsInsertReservationByConfirmation = 
                psInsertReservationByConfirmation.bind(r.getConfirmationNumber(), r.getHotelId(), r.getStartDate(), 
                                                r.getEndDate(), r.getRoomNumber(), r.getGuestId());
        BatchStatement batchInsertReservation = BatchStatement
                    .builder(DefaultBatchType.LOGGED)
                    .addStatement(bsInsertReservationByHotel)
                    .addStatement(bsInsertReservationByConfirmation)
                    .build();
        cqlSession.execute(batchInsertReservation);     
    }

    /**
     * We pick 'reservations_by_confirmation' table to list reservations
     * BUT we could have used 'reservations_by_hotel_date' (as no key provided in request)
     *  
     * @returns
     *      list all reservations
     */
    public List<Reservation> findAll() {
        return cqlSession.execute(selectFrom(KEYSPACE_NAME, TABLE_RESERVATION_BY_CONFI).all().build())
                  .all()                          // no paging we retrieve all objects
                  .stream()                       // because we are good people
                  .map(this::mapRowToReservation) // Mapping row as Reservation
                  .collect(Collectors.toList());  // Back to list objects
    }
      
    /**
     * Deleting a reservation. As not returned value why not switching to ASYNC.
     *
     * @param confirmationNumber
     *      unique identifier for confirmation.
     */
    public void delete(String confirmationNumber) {
        cqlSession.executeAsync(psDeleteReservation.bind(CONFIRMATION_NUMBER, confirmationNumber));
    }
    
    /**
     * Search all reservation for an hotel id and LocalDate.
     *
     * @param hotelId
     *      hotel identifier
     * @param date
     *      searched Date
     * @return
     */
    public List<Reservation> findByHotelAndDate(String hotelId, LocalDate date) {
        Objects.requireNonNull(hotelId);
        Objects.requireNonNull(date);
        return cqlSession.execute(psSearchReservation.bind(hotelId, date))
                         .all()                          // no paging we retrieve all objects
                         .stream()                       // because we are good people
                         .map(this::mapRowToReservation) // Mapping row as Reservation
                         .collect(Collectors.toList());  // Back to list objects
    }

    /**
     * Utility method to marshall a row as expected Reservation Bean.
     *
     * @param row
     *      current row fron ResultSet
     * @return
     *      object
     */
    private Reservation mapRowToReservation(Row row) {
        Reservation r = new Reservation();
        r.setHotelId(row.getString(HOTEL_ID));
        r.setConfirmationNumber(row.getString(CONFIRMATION_NUMBER));
        r.setGuestId(row.getUuid(GUEST_ID));
        r.setRoomNumber(row.getShort(ROOM_NUMBER));
        r.setStartDate(row.getLocalDate(START_DATE));
        r.setEndDate(row.getLocalDate(END_DATE));
        return r;
    }
}