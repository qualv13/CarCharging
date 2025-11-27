package org.qualv13.carcharging.util;

import java.util.Set;

public final class EnergyConstants {

    private EnergyConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Set<String> CLEAN_SOURCES = Set.of(
            "biomass",
            "nuclear",
            "hydro",
            "wind",
            "solar"
    );
}