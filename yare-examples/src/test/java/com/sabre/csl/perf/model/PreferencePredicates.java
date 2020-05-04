package com.sabre.csl.perf.model;

import java.time.ZonedDateTime;

public class PreferencePredicates {

    private String id;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreferencePredicates that = (PreferencePredicates) o;
        //System.out.println(this.getId() + " -> " + that.id);
        if (startDate != null && that.startDate != null && endDate != null && that.endDate != null) {
            return id.equals(that.id)
                    && (startDate.isBefore(that.startDate) || startDate.isEqual(that.startDate))
                    && (endDate.isAfter(that.endDate) || endDate.isEqual(that.endDate));
        } else {
            return id.equals(that.id);
        }
    }

}
