#!/bin/bash

# createReservation()
# (The “-i” option prints out headers, including the Location header, which gives us the URL we can use for a subsequent GET)
curl -i -X POST --data '{"hotelId": "NY456", "startDate": "2017-06-08", "endDate": "2017-06-10", "roomNumber": "111", "guestId": "1b4d86f4-ccff-4256-a63d-45c905df2677"}' -H "Content-Type: application/json" http://localhost:8080/reservations/

# retrieveReservation()
# Change the confirmation code as needed
curl http://localhost:8080/reservations/RS2G0Z

# updateReservation()
# Change the confirmation code as needed
curl -X PUT --data '{"confirmationNumber": "RS2G0Z","hotelId": "AZ123", "startDate": "2017-06-08", "endDate": "2017-06-14", "roomNumber": "999", "guestId": "1b4d86f4-ccff-4256-a63d-45c905df2677"}' -H "Content-Type: application/json" http://localhost:8080/reservations/RS2G0Z

# getAllReservations()
curl http://localhost:8080/reservations/

# deleteReservation()
# Change the confirmation code as needed
curl -X DELETE http://localhost:8080/reservations/RS2G0Z
