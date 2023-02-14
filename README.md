<h1 align="center">Rasp Control Unit</h1>

<div align="center">

![Release](https://img.shields.io/github/v/release/eco-trip/rasp-control-unit?label=Release)
![CI](https://github.com/eco-trip/rasp-control-unit/actions/workflows//build-and-deploy.yml/badge.svg)

Rasp Control Unit (part of Ecotrip) is an exam project for SC and LSS courses in UNIBO, made by
[Alan Mancini](https://github.com/MEBoo),
[Alberto Marfoglia](https://github.com/amarfoglia),
[Matteo Brocca](https://github.com/brteo),

</div>

---
## The idea behind the project

Welcome to the Rasp Control Unit project! This software has been developed to gather and analyze environmental and
consumption data in real-time for a hotel room. Using the [pi4j](https://pi4j.com/) and Gradle technology, 
Rasp Control Unit is able to acquire data through a series of sensors installed on a Raspberry PI.

## Repository content

Modules are organized following the [Hexagonal
architecture](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)) pattern. This way the
components are loosely coupled.

![Hexagonal architecture](https://github.com/eco-trip/LSS-report/blob/main/src/images/cl-architecture.png?raw=true)

A complete overview of the modules and their dependencies is provided by the following diagram.

![Module dependencies diagram](https://github.com/eco-trip/LSS-report/blob/feature/devops/src/images/gradle-multi-build.png?raw=true)

The main modules are the following:

- `utils` is the 0-level module, it holds common data structures or algorithms;
- `domain` is the module which models domain objects;
- `core` is the module that uses domain objects to implement domain use cases;
- `room-monitoring`: is the module that implements the logic for acquiring and sending consumption and environmental factors;
- `authorization`: is the module containing the logic for managing the shared authorization token with the user (guest).

## How to run

```
./gradlew example:run --args='
    --endpoint <Amazon-aws-endpoint> 
    --cert <Path-to-PEM-client-certificate> 
    --key <Path-to-PEM-key>
    --client_id <Client-id> 
    --thing_name <Control-unit-name>
'
```