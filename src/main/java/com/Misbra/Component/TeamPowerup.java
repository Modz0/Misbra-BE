package com.Misbra.Component;

import com.Misbra.Enum.PowerupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPowerup {
    private PowerupType type;
    private boolean used; // to disable it if true
    private boolean active; // this mean the question they answetd has this powerups active

}