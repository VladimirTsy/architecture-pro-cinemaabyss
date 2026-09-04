package com.cinemaabyss.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieEvent {

    @JsonProperty("movie_id")
    private int movieId;

    private String title;

    private String action;

    @JsonProperty("user_id")
    private int userId;

    public MovieEvent() {
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "MovieEvent{movieId=" + movieId + ", title='" + title + "', action='" + action + "', userId=" + userId + "}";
    }
}
