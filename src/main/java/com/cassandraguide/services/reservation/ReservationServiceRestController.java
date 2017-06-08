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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class ReservationServiceRestController {

    @Autowired
    private ReservationService reservationService;

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceRestController.class);

    // Retrieve single reservation by confirmation number
    @GetMapping("/reservations/{confirmationNumber}")
    public ResponseEntity<Reservation> retrieveReservationByConfirmationNumber(
            @PathVariable String confirmationNumber)
    {
        logger.debug("Fetching reservation with confirmation number " + confirmationNumber);
        Reservation reservation = reservationService.retrieveReservation(confirmationNumber);
        if (reservation == null) {
            logger.error("Reservation with confirmation number " + confirmationNumber + " not found");
            return new ResponseEntity<Reservation>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Reservation>(reservation, HttpStatus.OK);
    }

    // get all reservations
    @GetMapping("/reservations/")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        logger.debug("Fetching all reservations");

        List<Reservation> reservations = reservationService.getAllReservations();
        if(reservations.isEmpty()){
            logger.debug("No reservations found");
            return new ResponseEntity<List<Reservation>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Reservation>>(reservations, HttpStatus.OK);
    }

    // search operation
    @GetMapping("/reservations?{hotelId}&{date}")
    public ResponseEntity<List<Reservation>> searchReservationsByHotelDate(
            @RequestParam("hotelId") String hotelId,
            @RequestParam(name="date", required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        logger.debug("receive request for hotelId: " + hotelId + ", date: " + date);

        List<Reservation> reservations = reservationService.searchReservationsByHotelDate(hotelId, date);
        if(reservations.isEmpty()){
            logger.debug("No reservations found");
            return new ResponseEntity<List<Reservation>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Reservation>>(reservations, HttpStatus.OK);
    }

    @PostMapping("/reservations/")
    public ResponseEntity<Void> createReservation(
            @RequestBody Reservation reservation) {

        logger.debug("Creating reservation " + reservation.toString());
        String confirmationNumber = reservationService.createReservation(reservation);

        logger.debug("Created reservation with confirmation number: " + confirmationNumber);
        if (confirmationNumber == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                "/{confirmationNumber}").buildAndExpand(confirmationNumber).toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/reservations/{confirmationNumber}")
    public ResponseEntity<Void> updateReservation(
            @PathVariable String confirmationNumber,
            @RequestBody Reservation reservationToUpdate) {

        logger.debug("Request to update reservation " + confirmationNumber);

        // Validate reservation matches provided confirmation number
        if (confirmationNumber != reservationToUpdate.getConfirmationNumber())
        {
            logger.error("Request to update reservation - confirmation number doesn't match: " +
                    "request param: " + confirmationNumber +
                    ", request body: " + reservationToUpdate.getConfirmationNumber());
            return ResponseEntity.badRequest().build();
        }
        reservationService.updateReservation(reservationToUpdate);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations/{confirmationNumber}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable String confirmationNumber ) {

        System.out.println("Fetching & Deleting reservation with confirmation number " + confirmationNumber);

        Reservation reservation = reservationService.retrieveReservation(confirmationNumber);
        if (reservation == null) {
            System.out.println("Unable to delete. Reservation with confirmation number " +
                    confirmationNumber + " not found");
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        reservationService.deleteReservation(confirmationNumber);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

}
