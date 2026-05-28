<p align="center">
  <img width="100%" height="42" alt="{DE4329F6-6EAF-4B82-8324-7937D1818AC9}" src="https://github.com/user-attachments/assets/902f128c-4945-4237-acdc-6cbad420b650" />
</p>

<h1 align="center">CarCharging</h1>

<p align="center">
  EV charging decision backend that analyzes UK grid carbon-intensity data and recommends the cleanest charging window in the next 48 hours.
</p>

<p align="center">
  <a href="https://github.com/qualv13/CarCharging"><img src="https://img.shields.io/badge/GitHub-CarCharging-181717?style=for-the-badge&logo=github" alt="GitHub"></a>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.3.5">
  <img src="https://img.shields.io/badge/REST_API-NESO_Carbon_Intensity-0A66C2?style=for-the-badge" alt="NESO API">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
</p>

<p align="center">
  <a href="https://nextjs-render-fuqh.onrender.com/">Live frontend preview</a>
  ·
  <a href="https://github.com/qualv13/nextjs-render">Frontend repository</a>
</p>

---

## Overview

**CarCharging** is a Spring Boot backend that helps EV owners charge when the grid is cleanest rather than simply when electricity is available.

It integrates with the UK carbon intensity API and exposes a simple REST interface for:
- retrieving forecasted daily energy mix,
- calculating the clean-energy percentage,
- finding the best charging window for a selected duration.

This is a compact but strong portfolio project because it shows:
- external API integration,
- domain-specific data transformation,
- optimization logic over time-series intervals,
- clean REST endpoint design,
- Dockerized deployment.

## Value Proposition

For EV users, charging at the right time can reduce carbon impact without changing hardware. CarCharging turns raw generation-mix data into an actionable recommendation:

- **When should I charge?**
- **How clean is the grid over the next few days?**
- **What is the best 1-6 hour charging window in the next 48 hours?**

---

## Features

- **Best charging window calculation** for a user-selected duration
- **48-hour optimization window** based on forecasted generation data
- **Daily energy mix summaries** for the coming days
- **Clean energy percentage calculation** using selected low-carbon sources
- **Simple REST API** designed for frontend consumption
- **CORS-enabled endpoints** for web integration
- **Dockerized runtime** for easy deployment
- **Frontend-ready backend** with a linked React/TypeScript UI

---

## Tech Stack

| Category | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| API Style | REST |
| External Data Source | NESO / UK Carbon Intensity API |
| Build Tool | Maven |
| Containerization | Docker |
| Frontend Consumer | React + TypeScript frontend repo |
| Testing | Spring Boot Test |

### Stack badges
![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![REST](https://img.shields.io/badge/REST-API-005571?style=flat-square)
![Time Series](https://img.shields.io/badge/Time--Series-Optimization-6A5ACD?style=flat-square)

---

### High-level flow

```text
Frontend / Client
      |
      v
Spring Boot REST Controllers
      |
      v
Service Layer
  |             |
  |             +--> Charging window optimization
  |
  +-----------------> Carbon intensity API client
                         |
                         v
                 UK generation forecast data
```

### Main modules

```text
client/
config/
controller/
model/dto/
model/external/
service/
util/
```

### Main components
- `CarbonIntensityClient` - fetches external generation data
- `EnergyService` - aggregates daily energy mix and clean-energy percentage
- `ChargingService` - computes the best charging window
- `ChargingController` - exposes charging recommendation endpoint
- `EnergyController` - exposes energy mix endpoint

---

## Project Structure

```text
src/main/java/org/qualv13/carcharging/
├── client/
│   └── CarbonIntensityClient.java
├── config/
│   ├── RestClientConfig.java
│   └── WebConfig.java
├── controller/
│   ├── ChargingController.java
│   └── EnergyController.java
├── model/
│   ├── dto/
│   │   ├── ChargingWindowDto.java
│   │   └── DailyMixDto.java
│   └── external/
│       ├── CarbonApiResponse.java
│       ├── FuelMix.java
│       └── GenerationData.java
├── service/
│   ├── ChargingService.java
│   └── EnergyService.java
├── util/
│   └── EnergyConstants.java
└── CarChargingApplication.java
```

---

## Installation and Setup

## Prerequisites

- Java 17
- Maven 3.9+
- Docker

## Run locally with Maven

```bash
git clone https://github.com/qualv13/CarCharging.git
cd CarCharging
mvn spring-boot:run
```

The application starts as a standard Spring Boot service on port `8080` unless overridden.

## Run with Docker

```bash
git clone https://github.com/qualv13/CarCharging.git
cd CarCharging
docker build -t carcharging .
docker run -p 8080:8080 carcharging
```

### Verified Dockerfile behavior
- Builds with `maven:3.9.6-eclipse-temurin-17`
- Runs on `eclipse-temurin:17-jre-alpine`
- Exposes port `8080`

---

## Usage Examples

## Get the best charging window for 3h charging

```bash
curl "http://localhost:8080/api/charging/best-window?hours=3"
```

### Example response

```json
{
  "startTime": "2025-12-01T02:30Z",
  "endTime": "2025-12-01T05:30Z",
  "cleanEnergyPercent": 78.4
}
```

## Get forecasted daily energy mix

```bash
curl "http://localhost:8080/api/energy/mix"
```

### Example response

```json
[
  {
    "date": "2025-11-30",
    "cleanEnergyPercent": 58.69791666666666,
    "dailyMix": {
      "hydro": 0.0,
      "other": 0.0,
      "biomass": 9.78958333333333,
      "imports": 10.422916666666666,
      "gas": 30.872916666666665,
      "solar": 2.3854166666666665,
      "coal": 0.0,
      "nuclear": 13.022916666666665,
      "wind": 33.49999999999999
    }
  }
]
```

## Frontend integration example

```ts
const response = await fetch("http://localhost:8080/api/charging/best-window?hours=2");
const data = await response.json();

console.log(data.startTime, data.endTime, data.cleanEnergyPercent);
```

---

## API Documentation

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/charging/best-window?hours={n}` | Returns the cleanest charging window for `1-6` hours |
| GET | `/api/energy/mix` | Returns forecasted daily energy mix and clean-energy percentage |

## Request constraints

### `GET /api/charging/best-window`
- `hours` must be between **1 and 6**
- the service evaluates the next **48 hours**
- the algorithm uses half-hour forecast slots from the external API

If the requested duration cannot be computed from available future data, the service throws an error.

---

## How the Charging Algorithm Works

The charging recommendation logic is simple

### Verified behavior from the service implementation
- Fetches generation data from **today through the next 3 days**
- Filters intervals to the next **48 hours**
- Converts requested hours into **30-minute slots**
- Computes clean-energy percentage per slot
- Uses a **sliding window** to find the highest average clean-energy period

### Simplified logic

```java
int slotsNeeded = hours * 2;

for (int i = 0; i < sortedData.size(); i++) {
    currentWindowSum += cleanPercentage(sortedData.get(i));

    if (i >= slotsNeeded) {
        currentWindowSum -= cleanPercentage(sortedData.get(i - slotsNeeded));
    }

    if (i >= slotsNeeded - 1 && currentWindowSum > maxCleanSum) {
        maxCleanSum = currentWindowSum;
        bestStartIndex = i - slotsNeeded + 1;
    }
}
```

This is a good example of applying a classic sliding-window optimization pattern to a real-world sustainability use case.

---

## Screenshots

<img width="267" height="301" alt="{462E77EF-FD64-42F9-82F1-5ACA86030080}" src="https://github.com/user-attachments/assets/1bd2d981-0a94-4ebf-b4c6-d5a3d4794e62" />
<img width="1074" height="463" alt="{BB9D0CFD-95A8-4F8A-9424-7E7B1D0BF894}" src="https://github.com/user-attachments/assets/e89e8ccb-c944-42cc-a51d-7907a9b2790e" />
<img width="1086" height="329" alt="{0212E5F0-C149-451D-9DC8-1DD6B8AB1EDD}" src="https://github.com/user-attachments/assets/f33551ba-38d2-415f-a84c-0bed43a51a92" />


---

## Configuration

The verified repository only includes:

```properties
spring.application.name=CarCharging
```

That means the application is intentionally lightweight and relies primarily on code-level defaults and external API access.

### Operational notes
- Default runtime port is Spring Boot's standard `8080`
- Endpoints are annotated with `@CrossOrigin(origins = "*")`
- No database is required

This simplicity is a strength for a focused API utility service.

---

## Testing

The Maven configuration includes Spring Boot test support.

Run tests with:

```bash
mvn test
```

The repository structure also indicates service-level tests such as:
- `ChargingServiceTest`
- `EnergyServiceTest`
- `EnergyServiceIntegrationTest`

---

## Deployment

## Build JAR

```bash
mvn clean package
```

## Run packaged application

```bash
java -jar target/*.jar
```

## Run containerized application

```bash
docker build -t carcharging .
docker run -p 8080:8080 carcharging
```

### Production improvement ideas
- Add response caching for external API calls
- Add OpenAPI/Swagger docs
- Add validation/error response standardization
- Add rate limiting and resilience patterns
- Add CI pipeline and container publishing
- Add observability metrics

---

## Contributing

Contributions are welcome.

---

## Contact and Links

- GitHub profile: [qualv13](https://github.com/qualv13)
- Repository: [qualv13/CarCharging](https://github.com/qualv13/CarCharging)
- Frontend repo: [qualv13/nextjs-render](https://github.com/qualv13/nextjs-render)
- Live preview: [nextjs-render-fuqh.onrender.com](https://nextjs-render-fuqh.onrender.com/)

