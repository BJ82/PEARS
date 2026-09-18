package com.rail.app.railreservation.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passenger {

    @NotBlank(message = "Please provide passenger name")
    private String name;

    @Positive(message = "Age cannot be negative")
    private int age;

    @NotBlank(message = "Please mention whether male or female")
    private String sex;
}
