# CarCharging 🚗⚡

App that provides information about percent of clean energy in next 3 days and let you calculate green window for charging your car in next 48 hours!

## 🛠️ Technologies used

* **Backend:** [Java 17 + Spring Boot 3.3.5]
* **Frontend:** [React + TypeScript]
* **Container:** [Docker]
* **API:** [NESO API](https://carbon-intensity.github.io/api-definitions/?java#get-generation-from-to)

## 📂 Files

Żebyś nie musiał błądzić jak w labiryncie, oto szybka rozpiska katalogów:

```text
src/
├── main/java/org/qualv13/carcharging/
│   ├── client/         # API communication
│   │   └── CarbonIntensityClient  # API from carbonintensity.org.uk
│   │
│   ├── config/         # REST config
│   │   ├── RestClientConfig       # Konfiguracja klienta HTTP
│   │   └── WebConfig              # Ustawienia sieciowe
│   │
│   ├── controller/     # endpoints for frontend
│   │   ├── ChargingController  
│   │   └── EnergyController      
│   │
│   ├── model/          # Data structures
│   │   ├── dto/                   # data transfer objects
│   │   │   ├── ChargingWindowDto
│   │   │   └── DailyMixDto
│   │   └── external/              # Data from external sources (API)
│   │       ├── CarbonApiResponse
│   │       ├── FuelMix
│   │       └── GenerationData
│   │
│   ├── service/        # Logic
│   │   └── EnergyService          # Calculating energy stuff
│   │
│   └── CarChargingApplication.java # App start
│
└── test/               # Tests
    └── .../service/
        ├── EnergyServiceIntegrationTest
        └── EnergyServiceTest
```
## 🚀 How to run it?
_TBAdded_
