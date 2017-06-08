#!/bin/bash

curl http://localhost:8080/reservations/

curl -i -X DELETE http://localhost:8080/reservations/AI1234

curl -i -X POST --data '{"hotelId": "AZ123", "startDate": "2017-06-08", "endDate": "2017-06-10", "roomNumber": "111", "guestId": ""}' http://localhost:8080/reservations/
 

