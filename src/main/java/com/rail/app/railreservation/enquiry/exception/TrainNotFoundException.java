package com.rail.app.railreservation.enquiry.exception;

import java.io.IOException;

public class TrainNotFoundException extends IOException {
    private String src;
    private String dest;
    private int trainNo;

    @Override
    public String toString() {
        return "TrainNotFoundException{" +
                "src='" + src + '\'' +
                ", dest='" + dest + '\'' +
                ", trainNo=" + trainNo +
                '}';
    }

    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public TrainNotFoundException(String message, String src, String dest){

        super(message);
        this.src = src;
        this.dest = dest;
    }

    public TrainNotFoundException(String message,int trainNo){
        super(message);
        this.trainNo = trainNo;
    }

    public TrainNotFoundException(String message) {
        super(message);
    }
}
