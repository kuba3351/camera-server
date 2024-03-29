package com.raspberry.camera.dto;

import com.raspberry.camera.other.ThreadState;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu ustawień czasomierza
 */
public class TimeDTO {

    @NotNull
    private int hours;

    @NotNull
    private int minutes;

    @NotNull
    private int seconds;

    private int reamingHours;
    private int reamingMinutes;
    private int reamingSeconds;

    private ThreadState threadState;

    public TimeDTO() {
    }

    public TimeDTO(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.reamingHours = hours;
        this.reamingMinutes = minutes;
        this.reamingSeconds = seconds;
    }

    public static TimeDTO parseFromString(String string) {
        TimeDTO timeDTO = new TimeDTO();
        timeDTO.setHours(Integer.parseInt(string.substring(0, 2)));
        timeDTO.setMinutes(Integer.parseInt(string.substring(3, 5)));
        timeDTO.setSeconds(Integer.parseInt(string.substring(6, 8)));
        timeDTO.reset();
        return timeDTO;
    }

    public ThreadState getThreadState() {
        return threadState;
    }

    public void setThreadState(ThreadState threadState) {
        this.threadState = threadState;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getReamingHours() {
        return reamingHours;
    }

    public void setReamingHours(int reamingHours) {
        this.reamingHours = reamingHours;
    }

    public int getReamingMinutes() {
        return reamingMinutes;
    }

    public void setReamingMinutes(int reamingMinutes) {
        this.reamingMinutes = reamingMinutes;
    }

    public int getReamingSeconds() {
        return reamingSeconds;
    }

    public void setReamingSeconds(int reamingSeconds) {
        this.reamingSeconds = reamingSeconds;
    }

    public void reset() {
        reamingHours = hours;
        reamingMinutes = minutes;
        reamingSeconds = seconds;
    }

    public boolean tick() {
        if (reamingSeconds == 0) {
            if (reamingMinutes == 0) {
                if (reamingHours == 0)
                    return false;
                reamingHours -= 1;
                reamingMinutes = 59;
            } else
                reamingMinutes -= 1;
            reamingSeconds = 59;
        } else
            reamingSeconds -= 1;
        return true;
    }

    @Override
    public String toString() {
        return ((hours >= 10) ?
                hours : "0" + hours)
                + ":"
                + ((minutes >= 10) ?
                minutes : "0" + minutes)
                + ":"
                + ((seconds >= 10) ?
                seconds : "0" + seconds);
    }
}