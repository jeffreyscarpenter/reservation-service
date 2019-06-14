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
package com.cassandraguide.reservation.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cassandraguide.reservation.model.Reservation;
import com.cassandraguide.reservation.service.ReservationRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Exposing single Reservation operations as a RESTful API.
 */
@RestController
@Api(value = "/api/v1/reservations/{confirmationNumber}",  
     description = "Reservation Services Rest Resources")
@RequestMapping("/api/v1/reservations/{confirmationNumber}")
public class ReservationResource {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationResource.class);
    
    /** Service implementation Injection. */
    private ReservationRepository reservationService;

    /**
     * Best practice : Inversion of Control through constructor and no More @Inject nor @Autowired
     * 
     * @param reservationService
     *      service implementation
     */
    public ReservationResource(ReservationRepository reservationService) {
        this.reservationService = reservationService;
    }
    
    /**
     * Retrieve single reservation by confirmation number.
     *
     * @param confirmationNumber
     *      unique confirmation number
     * @return
     *      reservation if exists
     */
    @RequestMapping(method = GET,produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Access Reservation information if exists", 
            response = Reservation.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returnings Reservation"),
            @ApiResponse(code = 400, message = "ConfirmationNumber is blank or contains invalid characters (expecting AlphaNumeric)"),
            @ApiResponse(code = 404, message = "No reservation exists for the provided confirmation number ")
    })
    public ResponseEntity<Reservation> findByConfirmationNumber(
            @ApiParam(name="confirmationNumber", value="confirmation number for a reservation", required=true )
            @PathVariable(value = "confirmationNumber") String confirmationNumber) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Fetching reservation with confirmation number {}", confirmationNumber);
        // Invoking Service
        Optional<Reservation> reservation = reservationService.findByConfirmationNumber(confirmationNumber);
        // Routing Result
        if (!reservation.isPresent()) {
            logger.warn("Reservation with confirmation number {} has not been found", confirmationNumber);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation.get());
    }
    
    /**
     * Upsert reservation when confirmation number is provided (specify with PUT HTTP Verb)
     *
     * @param confirmationNumber
     *      unique confirmation number
     * @return
     *      true if entity has been created
     */
    @RequestMapping(method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Create or update a Reservation based on a given confirmation number", 
            response = ResponseEntity.class)
    @ApiResponses({
        @ApiResponse(code = 201, message = "Reservation has been created"),
        @ApiResponse(code = 204, message = "No content, reservation has been updated"),
        @ApiResponse(code = 400, message = "ConfirmationNumber is blank or contains invalid characters (expecting AlphaNumeric)")
    })
    public ResponseEntity<Void> upsert(
            @ApiParam(name="confirmationNumber", value="confirmation number for a reservation", required=true )
            @PathVariable(value = "confirmationNumber") String confirmationNumber,
            @RequestBody Reservation reservation) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Request to update reservation {}", confirmationNumber);
        HttpStatus returnedStatus = reservationService.exists(confirmationNumber) ? HttpStatus.CREATED : HttpStatus.NO_CONTENT;
        reservationService.upsert(reservation);
        return new ResponseEntity<>(returnedStatus);
    }

    @RequestMapping(method = DELETE)
    @ApiOperation(value = "Delete a reservation", response = ResponseEntity.class)
    @ApiResponses({
            @ApiResponse(code = 204, message = "No content, reservation has been deleted"),
            @ApiResponse(code = 400, message = "ConfirmationNumber is blank or contains invalid characters (expecting AlphaNumeric)"),
            @ApiResponse(code = 404, message = "The reservation does not exist")
    })
    public ResponseEntity<Void> delete(
            @ApiParam(name="confirmationNumber", value="confirmation number for a reservation", required=true )
            @PathVariable(value = "confirmationNumber") String confirmationNumber) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Fetching & Deleting reservation with confirmation number " + confirmationNumber);
        if (!reservationService.exists(confirmationNumber)) {
            logger.error("Unable to delete. Reservation with confirmation number " +
                    confirmationNumber + " not found");
            return ResponseEntity.notFound().build();
        }
        reservationService.delete(confirmationNumber);
        return ResponseEntity.noContent().build();
    }
   
    /**
     * Utility to validate confirmation Number.
     * 
     * @param cf
     *      confirmation number
     */
    private void validateConfirmationNumber(String cf) {
        if (null == cf || cf.isEmpty()) {
            throw new IllegalArgumentException("confirmationNumber should not be null nor empty");
        }
        // Should be a valid uuid AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE or IllegalArgumentException
        UUID.fromString(cf);
    }

}
