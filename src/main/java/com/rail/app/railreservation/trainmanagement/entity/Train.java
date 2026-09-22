package com.rail.app.railreservation.trainmanagement.entity;


import com.rail.app.railreservation.trainmanagement.dto.Seat;
import com.rail.app.railreservation.trainmanagement.enums.Day;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="Train")

@Getter
@Setter
@NoArgsConstructor
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer trainNo;

    private String trainName;

    private int routeId;


    @ElementCollection
    @CollectionTable(name = "run_on_days" , joinColumns = @JoinColumn(name = "train_no"))
    @Column(name = "day")
    @Enumerated(EnumType.STRING)
    private List<Day> runOnDays = new ArrayList<>();

    private String deptTime;

    private String arrvTime;

    @ElementCollection
    @CollectionTable(name = "journy_class_types" , joinColumns = @JoinColumn(name = "train_no"))
    @Column(name = "journy_class")
    @Enumerated(EnumType.STRING)
    private List<JourneyClass> avblJournyClass = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "seat_info",joinColumns = @JoinColumn(name = "train_no"))
    private Set<Seat> seats = new LinkedHashSet<>();

}
