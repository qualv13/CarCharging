package org.qualv13.carcharging.controller;

import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.service.EnergyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin(origins = "*")
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/mix")
    public List<DailyMixDto> getEnergyMix() {
        return energyService.getEnergyMixForComingDays();
    }

}
