package com.Misbra.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "teams")
public class Team {
    private String teamId;
    private String name;

}
