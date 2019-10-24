package dev.cassandraguide.repository;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createType;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createKeyspace;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropKeyspace;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropTable;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.truncate;

/**
 * Utility class to help manage the reservation schema including creating tables
 * and clearing table contents.
 *
 * @author Jeff Carpenter, Cedrick Lunven
 */
public class ReservationSchemaUtility {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationSchemaUtility.class);

    /**
     * Create reservation keyspace as per defined in 'reservation.cql'
     * @param cqlSession
     * @param keyspaceName
     */
    public static void createReservationKeyspace(CqlSession cqlSession, CqlIdentifier keyspaceName) {
        /**
         * Create KEYSPACE 'reservation' if not exists
         *
         * CREATE KEYSPACE reservation
         *   WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
         */
        try {
            cqlSession.execute(
                    createKeyspace(keyspaceName)
                            .ifNotExists()
                            .withSimpleStrategy(1)
                            .build());
        } catch (Exception e) {
            logger.debug("+ Error creating keyspace '{}'", e.toString());
        }
        logger.debug("+ Keyspace '{}' has been created (if needed)", keyspaceName);
    }

    public static void dropReservationKeyspace(CqlSession cqlSession, CqlIdentifier keyspaceName) {
        /**
         * Drop KEYSPACE 'reservation'
         *
         * DROP KEYSPACE reservation
         */
        cqlSession.execute(
                dropKeyspace(keyspaceName)
                        .ifExists()
                        .build());
        logger.debug("+ Keyspace '{}' has been dropped (if needed)", keyspaceName);
    }

    /**
     * Create user defined types and relevant tables as per defined in 'reservation.cql'
     * @param cqlSession
     * @param keyspaceName
     */
    public static void createReservationTables(CqlSession cqlSession, CqlIdentifier keyspaceName) {

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
                createType(keyspaceName, ReservationRepository.TYPE_ADDRESS)
                .ifNotExists()
                .withField(ReservationRepository.STREET, DataTypes.TEXT)
                .withField(ReservationRepository.CITY, DataTypes.TEXT)
                .withField(ReservationRepository.STATE_PROVINCE, DataTypes.TEXT)
                .withField(ReservationRepository.POSTAL_CODE, DataTypes.TEXT)
                .withField(ReservationRepository.COUNTRY, DataTypes.TEXT)
                .build());
        logger.debug("+ Type '{}' has been created (if needed)", ReservationRepository.TYPE_ADDRESS.asInternal());

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
        cqlSession.execute(createTable(keyspaceName, ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE)
                        .ifNotExists()
                        .withPartitionKey(ReservationRepository.HOTEL_ID, DataTypes.TEXT)
                        .withPartitionKey(ReservationRepository.START_DATE, DataTypes.DATE)
                        .withClusteringColumn(ReservationRepository.ROOM_NUMBER, DataTypes.SMALLINT)
                        .withColumn(ReservationRepository.END_DATE, DataTypes.DATE)
                        .withColumn(ReservationRepository.CONFIRMATION_NUMBER, DataTypes.TEXT)
                        .withColumn(ReservationRepository.GUEST_ID, DataTypes.UUID)
                        .withClusteringOrder(ReservationRepository.ROOM_NUMBER, ClusteringOrder.ASC)
                        //.withComment("Q7. Find reservations by hotel and date")
                        .build());
        logger.debug("+ Table '{}' has been created (if needed)", ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE.asInternal());

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
        cqlSession.execute(createTable(keyspaceName, ReservationRepository.TABLE_RESERVATION_BY_CONFI)
                .ifNotExists()
                .withPartitionKey(ReservationRepository.CONFIRMATION_NUMBER, DataTypes.TEXT)
                .withColumn(ReservationRepository.HOTEL_ID, DataTypes.TEXT)
                .withColumn(ReservationRepository.START_DATE, DataTypes.DATE)
                .withColumn(ReservationRepository.END_DATE, DataTypes.DATE)
                .withColumn(ReservationRepository.ROOM_NUMBER, DataTypes.SMALLINT)
                .withColumn(ReservationRepository.GUEST_ID, DataTypes.UUID)
                .build());
         logger.debug("+ Table '{}' has been created (if needed)", ReservationRepository.TABLE_RESERVATION_BY_CONFI.asInternal());

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
         cqlSession.execute(createTable(keyspaceName, ReservationRepository.TABLE_RESERVATION_BY_GUEST)
                 .ifNotExists()
                 .withPartitionKey(ReservationRepository.GUEST_LAST_NAME, DataTypes.TEXT)
                 .withClusteringColumn(ReservationRepository.HOTEL_ID, DataTypes.TEXT)
                 .withColumn(ReservationRepository.START_DATE, DataTypes.DATE)
                 .withColumn(ReservationRepository.END_DATE, DataTypes.DATE)
                 .withColumn(ReservationRepository.ROOM_NUMBER, DataTypes.SMALLINT)
                 .withColumn(ReservationRepository.CONFIRMATION_NUMBER, DataTypes.TEXT)
                 .withColumn(ReservationRepository.GUEST_ID, DataTypes.UUID)
                 //.withComment("Q8. Find reservations by guest name")
                 .build());
          logger.debug("+ Table '{}' has been created (if needed)", ReservationRepository.TABLE_RESERVATION_BY_GUEST.asInternal());

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
          UserDefinedType udtAddressType =
                  cqlSession.getMetadata().getKeyspace(keyspaceName).get() // Retrieving KeySpaceMetadata
                            .getUserDefinedType(ReservationRepository.TYPE_ADDRESS).get();        // Looking for UDT (extending DataType)
          cqlSession.execute(createTable(keyspaceName, ReservationRepository.TABLE_GUESTS)
                  .ifNotExists()
                  .withPartitionKey(ReservationRepository.GUEST_ID, DataTypes.UUID)
                  .withColumn(ReservationRepository.FIRSTNAME, DataTypes.TEXT)
                  .withColumn(ReservationRepository.LASTNAME, DataTypes.TEXT)
                  .withColumn(ReservationRepository.TITLE, DataTypes.TEXT)
                  .withColumn(ReservationRepository.EMAILS, DataTypes.setOf(DataTypes.TEXT))
                  .withColumn(ReservationRepository.PHONE_NUMBERS, DataTypes.listOf(DataTypes.TEXT))
                  .withColumn(ReservationRepository.ADDRESSES, DataTypes.mapOf(DataTypes.TEXT, udtAddressType, true))
                  .withColumn(ReservationRepository.CONFIRMATION_NUMBER, DataTypes.TEXT)
                  //.withComment("Q9. Find guest by ID")
                  .build());
           logger.debug("+ Table '{}' has been created (if needed)", ReservationRepository.TABLE_GUESTS.asInternal());
           logger.info("Schema has been successfully initialized.");
    }

    /**
     * Drop tables as defined in 'reservation.cql'
     * @param cqlSession
     * @param keyspaceName
     */
    public static void dropReservationTables(CqlSession cqlSession, CqlIdentifier keyspaceName) {

        cqlSession.execute(dropTable(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE).build());
        logger.debug("+ Table '{}' has been dropped", ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE.asInternal());

        cqlSession.execute(dropTable(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_CONFI)
                .build());
        logger.debug("+ Table '{}' has been dropped", ReservationRepository.TABLE_RESERVATION_BY_CONFI.asInternal());

        cqlSession.execute(dropTable(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_GUEST)
                .build());
        logger.debug("+ Table '{}' has been dropped", ReservationRepository.TABLE_RESERVATION_BY_GUEST.asInternal());

        cqlSession.execute(dropTable(keyspaceName, ReservationRepository.TABLE_GUESTS)
                .build());
        logger.debug("+ Table '{}' has been dropped", ReservationRepository.TABLE_GUESTS.asInternal());
    }

    /**
     * Truncate tables as defined in 'reservation.cql'
     * @param cqlSession
     * @param keyspaceName
     */
    public static void truncateReservationTables(CqlSession cqlSession, CqlIdentifier keyspaceName) {

        cqlSession.execute(truncate(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE).build());
        logger.debug("+ Table '{}' has been truncated", ReservationRepository.TABLE_RESERVATION_BY_HOTEL_DATE.asInternal());

        cqlSession.execute(truncate(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_CONFI)
                .build());
        logger.debug("+ Table '{}' has been truncated", ReservationRepository.TABLE_RESERVATION_BY_CONFI.asInternal());

        cqlSession.execute(truncate(keyspaceName,
                ReservationRepository.TABLE_RESERVATION_BY_GUEST)
                .build());
        logger.debug("+ Table '{}' has been truncated", ReservationRepository.TABLE_RESERVATION_BY_GUEST.asInternal());

        cqlSession.execute(truncate(keyspaceName, ReservationRepository.TABLE_GUESTS)
                .build());
        logger.debug("+ Table '{}' has been truncated", ReservationRepository.TABLE_GUESTS.asInternal());
    }

}
