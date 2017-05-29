package com.cassandraguide.services.reservation;

/**
 * Created by jeffreycarpenter on 5/24/17.
 */

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationService {

    public UUID createReservation(Reservation newReservation) {

        // TODO
        return null;
    }

    public Reservation retrieveReservation(String reservationId) {

        // TODO
        return null;
    }

    public List<Reservation> getAllReservations() {

        // TODO
        return null;
    }

    public List<Reservation> searchReservationsByHotelDate(String hotelId, Date date) {

        // TODO
        return null;
    }
}
