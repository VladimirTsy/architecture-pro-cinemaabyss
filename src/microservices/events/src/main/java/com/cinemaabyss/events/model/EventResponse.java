package com.cinemaabyss.events.model;

import java.util.Map;

public class EventResponse {

    private String status;

    private int partition;

    private long offset;

    private Object event;

    public EventResponse() {
    }

    public EventResponse(String status, int partition, long offset, Object event) {
        this.status = status;
        this.partition = partition;
        this.offset = offset;
        this.event = event;
    }

    public static EventResponse success(int partition, long offset, Object event) {
        return new EventResponse("success", partition, offset, event);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = event;
    }
}
