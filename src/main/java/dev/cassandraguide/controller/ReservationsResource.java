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
package dev.cassandraguide.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import dev.cassandraguide.model.Reservation;
import dev.cassandraguide.model.ReservationRequest;
import dev.cassandraguide.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.datastax.oss.driver.api.core.DriverException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Resources working with {@link Reservation}.
 * This CRUD resource leverages standard HTTP Codes and patterns.
 * 
 * GET    /                     : Will list all Reservations
 * POST   /                     : Will create a new Reservation, returning a confirmation number
 * GET    /{confirmationNumber} : Will get the reservation if it exists or send not found
 * DELETE /{confirmationNumber} : Will delete the reservation if exists or send not found
 * PUT    /{confirmationNumber} : Will update a reservation
 * GET    /findByHotelAndDate   : Search a list of reservations 
 *
 * @author Jeff Carpenter, Cedrick Lunven
 */
@RestController
@Api(value = "/api/v1/reservations",  
     description = "Reservation Services Rest Resources")
@RequestMapping("/api/v1/reservations")
public class ReservationsResource {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationsResource.class);
    
    /** Service implementation injection. */
    private ReservationRepository reservationService;

    /**
     * Best practice : Inversion of Control through constructor and no More @Inject nor @Autowired
     * 
     * @param reservationService
     *      service implementation
     */
    public ReservationsResource(ReservationRepository reservationService) {
        this.reservationService = reservationService;
    }
    
    /**
     * List all reservations. Please note there is no implementation of paging. As such the results can be
     * quite large. If you query tables with a large number of rows, please use paging.
     *  
     * @return
     *      list of all {@link Reservation} available
     */
    @RequestMapping(
            method = GET,
            value = "/",
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "List all reservations available", response = List.class)
    @ApiResponse(
            code = 200,
            message = "List all reservations available")
    public ResponseEntity<List<Reservation>> findAll() {
        logger.debug("Fetching all reservations");
        // Returning an empty list is better than 204 code (meaning no value expected)
        return ResponseEntity.ok(reservationService.findAll());
    }
    
    /**
     * As no confirmation number has been provided this will create a new reservation
     * and GENERATE the confirmation number. To update a previous reservation, use the PUT resource.
     */
    @RequestMapping(
            method = POST,
            value = "/", 
            consumes = APPLICATION_JSON_VALUE,
            produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a Reservation and generate confirmation number", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Reservation has been created"),
            @ApiResponse(code = 400, message = "Invalid ReservationRequest provided")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(
            name = "ReservationRequest",
            value = "A JSON value representing a reservation.\n"
                    + "An example of the expected schema can be found below. "
                    + "The fields marked with * are required. "
                    + "See the schema of ReservationRequest for more information.",
            required = true, dataType = "ReservationRequest", paramType = "body")
    })
    public ResponseEntity<String> create(
            HttpServletRequest request,
            @RequestBody ReservationRequest reservationRequest) {
        // If reservation cannot be marshalled Spring will throw IllegalArgument catch with badRequestHandler
        // As no reservation number is provided, one has been generated and returned
        String confirmationNumber = reservationService.upsert(new Reservation(reservationRequest));
        // HTTP Created spec, return target resource in 'location' header
        URI location = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/api/v1/reservations/{confirmationNumber}")
                .buildAndExpand(confirmationNumber)
                .toUri();
        // HTTP 201 with confirmation number
        return ResponseEntity.created(location).body(confirmationNumber);
    }
    
    /**
     * Retrieve single reservation by confirmation number.
     *
     * @param confirmationNumber
     *      unique confirmation number
     * @return
     *      reservation if exists
     */
    @RequestMapping(
            value = "/{confirmationNumber}",
            method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Access Reservation information if exists", 
            response = Reservation.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returning Reservation"),
            @ApiResponse(code = 400, message = "ConfirmationNumber is blank or contains invalid characters (expecting alphanumeric)"),
            @ApiResponse(code = 404, message = "No reservation exists for the provided confirmation number ")
    })
    public ResponseEntity<Reservation> findByConfirmationNumber(
            @ApiParam(name="confirmationNumber", 
                     value="confirmation number for a reservation",
                     example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366",
                     required=true )
            @PathVariable(value = "confirmationNumber") String confirmationNumber) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Fetching reservation with confirmation number {}", confirmationNumber);
        // Invoking Service
        Optional<Reservation> reservation = reservationService.findByConfirmationNumber(confirmationNumber);
        // Routing Result
        if (!reservation.isPresent()) {
            logger.warn("Reservation with confirmation number {} not  found", confirmationNumber);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation.get());
    }
    
    /**
     * Update reservation when confirmation number is provided (specify with PUT HTTP Verb)
     *
     * @param confirmationNumber
     *      unique confirmation number
     * @return
     *      true if entity has been created
     */
    @RequestMapping(
            method = PUT, 
            value = "/{confirmationNumber}",
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Create or update a Reservation based on a given confirmation number", 
            response = ResponseEntity.class)
    @ApiResponses({
        @ApiResponse(code = 201, message = "Reservation has been created"),
        @ApiResponse(code = 204, message = "No content, reservation has been updated"),
        @ApiResponse(code = 400, message = "Confirmation number is blank or contains invalid characters (expecting alphanumeric)")
    })
    public ResponseEntity<Void> upsert(
            @ApiParam(name="confirmationNumber", 
                    example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366",
                    value="Confirmation number for a reservation",
                    required=true )
            @PathVariable(value = "confirmationNumber") String confirmationNumber,
            @RequestBody ReservationRequest reservation) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Request to update reservation {}", confirmationNumber);
        HttpStatus returnedStatus = reservationService.exists(confirmationNumber) ? 
                HttpStatus.NO_CONTENT : HttpStatus.CREATED;
        reservationService.upsert(new Reservation(reservation, confirmationNumber));
        return new ResponseEntity<>(returnedStatus);
    }

    @RequestMapping(
            method = DELETE,
            value = "/{confirmationNumber}")
    @ApiOperation(value = "Delete a reservation", response = ResponseEntity.class)
    @ApiResponses({
            @ApiResponse(code = 204, message = "No content, reservation has been deleted"),
            @ApiResponse(code = 400, message = "Confirmation number is blank or contains invalid characters (expecting alphanumeric)"),
            @ApiResponse(code = 404, message = "The reservation does not exist")
    })
    public ResponseEntity<Void> delete(
            @ApiParam(name="confirmationNumber", 
                      value="Confirmation number for a reservation",
                      example = "b9c5a9d8-9781-4de8-a00a-601a9cd6b366",
                      required=true)
            @PathVariable(value = "confirmationNumber") String confirmationNumber) {
        validateConfirmationNumber(confirmationNumber);
        logger.debug("Fetching & deleting reservation with confirmation number " + confirmationNumber);
        if (!reservationService.delete(confirmationNumber)) {
            logger.error("Unable to delete. Reservation with confirmation number " +
                    confirmationNumber + " not found");
            return ResponseEntity.notFound().build();
        }
        reservationService.delete(confirmationNumber);
        return ResponseEntity.noContent().build();
    }

    /**
     * List reservation for a hotel id on a particular date.
     *
     * @param hotelId
     *      uniquement hotel identifier
     * @param date
     *      target date
     * @return
     */
    @RequestMapping(
            value = "/findByHotelAndDate", 
            method = GET, 
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Access Reservation information for a hotel",
            response = Reservation.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Parameter: Hotel id is blank or contains invalid characters "
                    + "(expecting alphanumeric) or invalid date format (expecting yyyy-MM-dd)"),
            @ApiResponse(code = 200, message = "Returnings Reservation")})
    public ResponseEntity<List<Reservation>> findByByHotelAndDate(
            @RequestParam("hotelId") 
            @ApiParam(name="hotelId", value="Unique hotel identifier", required=true)
            String hotelId,
            @RequestParam(name="date", required=false) 
            @ApiParam(
                        name="date", 
                        value="ISO value for date yyyy-MM-dd", 
                        example = "2020-06-20",
                        required=false )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate date) {
        if (null == hotelId || hotelId.isEmpty()) {
            throw new IllegalArgumentException("hotelId may not be null nor empty");
        }
        // Error in date format would be detected on LocalDate Marshalling, no extra controls
        logger.debug("Receive request for hotelId:{}, {}", hotelId, date);
        return ResponseEntity.ok(reservationService.findByHotelAndDate(hotelId, date));
    }
    
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String _errorBadRequestHandler(IllegalArgumentException ex) {
        return "Invalid Parameter: " + ex.getMessage();
    }
    
    /**
     * Converts {@link DriverException}s into HTTP 500 error codes and outputs the error message as
     * the response body.
     *
     * @param e The {@link DriverException}.
     * @return The error message to be used as response body.
     */
    @ExceptionHandler(DriverException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String _errorDriverHandler(DriverException e) {
      return e.getMessage();
    }
    
    /**
     * Utility to validate confirmation number.
     * 
     * @param cf
     *      confirmation number
     */
    private void validateConfirmationNumber(String cf) {
        if (null == cf || cf.isEmpty()) {
            throw new IllegalArgumentException("confirmation number should not be null nor empty");
        }
        // Should be a valid uuid AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE or IllegalArgumentException
        UUID.fromString(cf);
    }

}
