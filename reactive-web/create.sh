#!/bin/bash 

curl -H"content-type: application/json" -d'{"email":"random"}' http://localhost:8080/profiles
