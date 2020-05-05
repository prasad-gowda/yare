package com.sabre.csl.perf.model;

import java.time.ZonedDateTime;

public class StayDates {
    ZonedDateTime stayStartDate;

    ZonedDateTime stayEndDate;

    public ZonedDateTime getStayStartDate() {
        return stayStartDate;
    }

    public void setStayStartDate(ZonedDateTime stayStartDate) {
        this.stayStartDate = stayStartDate;
    }

    public ZonedDateTime getStayEndDate() {
        return stayEndDate;
    }

    public void setStayEndDate(ZonedDateTime stayEndDate) {
        this.stayEndDate = stayEndDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StayDates that = (StayDates) o;
        //System.out.println(this.getId() + " -> " + that.id);
        if (stayStartDate != null && that.stayStartDate != null && stayEndDate != null && that.stayEndDate != null) {
            return (stayStartDate.isBefore(that.stayStartDate) || stayStartDate.isEqual(that.stayStartDate))
                    && (stayEndDate.isAfter(that.stayEndDate) || stayEndDate.isEqual(that.stayEndDate));
        }

        return false;
    }
}