package dev.cassandraguide.repository;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;

import java.time.LocalDate;

/**
 * Data Access Object for manipulating reservation objects to/from various CQL tables
 *
 * @author Jeff Carpenter
 */
// TODO: review DAO interface
@Dao
public interface ReservationDao {

    @Select
    ReservationsByConfirmation findByConfirmationNumber(String confirmationNumber);

    @Select (customWhereClause = "hotel_id = :hotelId AND start_date = :date")
    PagingIterable<ReservationsByHotelDate> findByHotelDate(
            @CqlName("hotel_id") String hotelId,
            @CqlName("start_date") LocalDate date);

    @Query("SELECT * FROM ${tableId}")
    PagingIterable<ReservationsByConfirmation> findAll();

    @Insert
    void save(ReservationsByConfirmation reservationsByConfirmation);

    @Insert
    void save(ReservationsByHotelDate reservationsByHotelDate);

    @Delete
    void delete(ReservationsByConfirmation reservationsByConfirmation);

    @Delete
    void delete(ReservationsByHotelDate reservationsByHotelDate);
}
