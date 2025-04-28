package com.Misbra.Component;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerupsRequest {
    private List<TeamPowerup> team1Powerups;
    private List<TeamPowerup> team2Powerups;

}
