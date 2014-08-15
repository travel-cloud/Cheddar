/*
 * Copyright 2014 Click Travel Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import java.util.List;

import org.joda.time.DateTime;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;

public class ImdbDocument implements Document {

    private String id;
    private List<String> actors;
    private List<String> directors;
    private List<String> genres;
    private String image_url;
    private String plot;
    private Integer rank;
    private Double rating;
    private DateTime release_date;
    private Integer running_time_secs;
    private String title;
    private Integer year;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(final List<String> actors) {
        this.actors = actors;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public void setDirectors(final List<String> directors) {
        this.directors = directors;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(final List<String> genres) {
        this.genres = genres;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(final String image_url) {
        this.image_url = image_url;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(final String plot) {
        this.plot = plot;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(final Integer rank) {
        this.rank = rank;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(final Double rating) {
        this.rating = rating;
    }

    public DateTime getRelease_date() {
        return release_date;
    }

    public void setRelease_date(final DateTime release_date) {
        this.release_date = release_date;
    }

    public Integer getRunning_time_secs() {
        return running_time_secs;
    }

    public void setRunning_time_secs(final Integer running_time_secs) {
        this.running_time_secs = running_time_secs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

}
