package org.qualv13.carcharging.controller;

import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.service.ChargingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging")
@CrossOrigin(origins = "*")
public class ChargingController {

    private final ChargingService chargingService;

    public ChargingController(ChargingService chargingService) {
        this.chargingService = chargingService;
    }

    @GetMapping("/best-window")
    public ChargingWindowDto getBestWindow(@RequestParam int hours) {
        return chargingService.findBestChargingWindow(hours);
    }
}



