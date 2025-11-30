# CarCharging 🚗⚡

App (backend) that provides information about percent of clean energy in next 3 days and let you calculate green window for charging your car in next 48 hours!

Do you want a preview? [Here you can check it!](https://nextjs-render-fuqh.onrender.com/)

[Link to frontend repo](https://github.com/qualv13/nextjs-render)

## 🛠️ Technologies used

* **Backend:** [Java 17 + Spring Boot 3.3.5]
* **Frontend:** [React + TypeScript]
* **Container:** [Docker]
* **API:** [NESO API](https://carbon-intensity.github.io/api-definitions/?java#get-generation-from-to)

## 🔚💠 API Endpoints

App provides API as said above from any source.

### Best window to charge car for 3 hours in upcoming 48h window
```http
GET /api/charging/best-window?hours=3
```

returns
```json
{
  "startTime": "2025-12-01T02:30Z",
  "endTime": "2025-12-01T05:30Z",
  "cleanEnergyPercent": 78.39999999999999
}
```

### Forecasted mix of energy an % of clean
```http
GET /api/energy/mix
```

returns
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
  },
  {
    "date": "2025-12-01",
    "cleanEnergyPercent": 66.11956521739131,
    "dailyMix": {
      "hydro": 0.0,
      "other": 0.0,
      "biomass": 7.908695652173911,
      "imports": 10.423913043478262,
      "gas": 23.45,
      "solar": 0.4304347826086956,
      "coal": 0.0,
      "nuclear": 11.652173913043478,
      "wind": 46.12826086956522
    }
  }
]
```



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
