package com.cassandraguide.services.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jeffreycarpenter on 5/24/17.
 */
@RestController
public class ReservationServiceRestController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/reservations/{confirmationNumber}")
    public Reservation retrieveReservationByConfirmationNumber(@PathVariable String confirmationNumber) {
        return reservationService.retrieveReservation(confirmationNumber);
    }

    @GetMapping("/reservations/")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping("/reservations")
    public ResponseEntity<Void> createReservation(
            @RequestBody Reservation newReservation) {

        UUID reservationId = reservationService.createReservation(newReservation);

        if (reservationId == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                "/{confirmationNumber}").buildAndExpand(newReservation.getConfirmationNumber()).toUri();

        return ResponseEntity.created(location).build();
    }

    // search operation
    @GetMapping("/reservations")
    public List<Reservation> searchReservationsByHotelDate(
            @RequestParam("hotelId") String hotelId,
            @RequestParam(name="date", required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date
            ) {
        System.out.println("receive request for hotelId: " + hotelId + ", date: " + date);

        return reservationService.searchReservationsByHotelDate(hotelId, date);
    }


    // add put (update) operation?
}
