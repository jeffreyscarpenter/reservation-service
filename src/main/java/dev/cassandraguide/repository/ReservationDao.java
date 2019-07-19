package dev.cassandraguide.repository;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Query;

import java.time.LocalDate;

/**
 * Data Access Object for manipulating reservation objects to/from various CQL tables
 *
 * @author Jeff Carpenter
 */
@Dao
public interface ReservationDao {

    @Select
    ReservationsByConfirmation findByConfirmationNumber(String confirmationNumber);

    @Select
    PagingIterable<ReservationsByHotelDate> findByHotelDate(String hotelId, LocalDate date);

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
