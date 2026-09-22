package com.rail.app.railreservation.trainmanagement.dto;

import com.rail.app.railreservation.trainmanagement.enums.Berth;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seat {

    private int seatNo;

    @Enumerated(EnumType.STRING)
    private Berth berth;

    @Enumerated(EnumType.STRING)
    private JourneyClass journeyClass;

}
