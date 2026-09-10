package com.rail.app.railreservation.booking.entity;

import com.rail.app.railreservation.booking.enums.BookingStatus;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name="Booking")

@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int pnr;

    private String name;
    private int age;
    private String sex;
    private int trainNo;
    private LocalDate startDt;
    private LocalDate endDt;
    private String startFrom;
    private String endAt;
    private LocalDate dtOfJourny;
    private String bookingType;

    @Enumerated(EnumType.STRING)
    private JourneyClass journeyClass;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    private Timestamp timestamp;
    private int seatNo;

    public Booking(String name, int age, String sex, int trainNo,
                   LocalDate startDt, LocalDate endDt, String startFrom,
                   String to, LocalDate dtOfJourny, JourneyClass journeyClass,
                   BookingStatus bookingStatus, Timestamp timestamp, int seatNo) {

        this.name = name;
        this.age = age;
        this.sex = sex;
        this.trainNo = trainNo;
        this.startDt = startDt;
        this.endDt = endDt;
        this.startFrom = startFrom;
        this.endAt = to;
        this.dtOfJourny = dtOfJourny;
        this.journeyClass = journeyClass;
        this.bookingStatus = bookingStatus;
        this.timestamp = timestamp;
        this.seatNo = seatNo;
    }
}
