# CarCharging 🚗⚡

App (backend) that provides information about percent of clean energy in next 3 days and let you calculate green window for charging your car in next 48 hours!

Do you want a preview? [Here you can check it!](https://nextjs-render-fuqh.onrender.com/)

[Link to frontend repo](https://github.com/qualv13/nextjs-render)

## 🛠️ Technologies used

* **Backend:** [Java 17 + Spring Boot 3.3.5]
* **Frontend:** [React + TypeScript]
* **Container:** [Docker]
* **API:** [NESO API](https://carbon-intensity.github.io/api-definitions/?java#get-generation-from-to)

## 📂 Project structure

```text
src/
├── main/java/org/qualv13/carcharging/
│   ├── client/         # API communication
│   │   └── CarbonIntensityClient    # API from carbonintensity.org.uk
│   │
│   ├── config/         # REST config
│   │   ├── RestClientConfig         # HTTP
│   │   └── WebConfig                # Web
│   │
│   ├── controller/     # Endpoints for frontend
│   │   ├── ChargingController  
│   │   └── EnergyController      
│   │
│   ├── model/          # Data structures
│   │   ├── dto/                 # data transfer objects
│   │   │   ├── ChargingWindowDto
│   │   │   └── DailyMixDto
│   │   └── external/            # data from external sources (API)
│   │       ├── CarbonApiResponse
│   │       ├── FuelMix
│   │       └── GenerationData
│   │
│   ├── service/        # Logic
│   │   └── EnergyService            # calculating mix for upcoming days
│   │   └── ChargingService          # calculating charging window
│   │
│   ├── util/           # Utilities
│   │   └── EnergyConstants          # set of clean energy names
│   │
│   └── CarChargingApplication.java  # app start
│
└── test/               # Tests
    └── .../service/
        ├── ChargingServiceTest      # test of ChargingService.java
        ├── EnergyServiceIntegrationTest
        └── EnergyServiceTest        # test of EnergyService.java
```
## 🚀 How to run it?

In terminal inside project write
```bash
docker build -t carcharging
```
and then
```bash
docker run -p 8080:8080 carcharging
```

_I like Docker, okay?_

if you don't - sure, here is easier version

```bash
mvn spring-boot:run
```

or in IntelliJ

find CarChargingApplication.java in src/main/java/.../carcharging and click "Run" next to class name

## Additional
Feel free to write DMs to me about service and how to improve my work:D