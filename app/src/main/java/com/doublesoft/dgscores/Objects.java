package com.doublesoft.dgscores;

import java.util.Date;

/** Käytettävät oliot:
 *
 * Fairway, Course, Player
 *
 * Created by yone on 9.10.2015.
 */
public class Objects {

    public class Fairway {
        int id;
        int par;
        int distance;
        String name;

        public Fairway() {}

        public Fairway(int id, int par, int distance, String name){
            this.id = id;
            this.par = par;
            this.distance = distance;
            this.name = name;
        }

        public int getDistance() {
            return distance;
        }
        public int getPar() {
            return par;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }
        public void setPar(int par) {
            this.par = par;
        }
    }

    public class Course {
        int id;
        String name;
        int holeCount;
        int par;
        Fairway[] holes;

        public Course () {}

        public Course(String name, int holeCount, int par){
            this.name = name;
            this.holeCount = holeCount;
            this.par = par;
            this.holes = new Fairway[holeCount];
        }

        public String getName() {
            return name;
        }
        public int getHoleCount() {
            return holeCount;
        }
        public int getPar(){return par;}
        public Fairway[] getHoles() {
            return holes;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setHoleCount(int holeCount) {
            this.holeCount = holeCount;
        }
        public void setHoles(Fairway[] holes) {
            this.holes = holes;
        }
    }

    public class Player {
        String name;

        public Player() {}

        public Player (String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class Round {
        Course course;
        Player[] players;
        Date date;
        int[][] results;

        public Round(Course course, Player[] players){
            this.course = course;
            this.players = players;
            this.results = new int[players.length][course.getHoleCount()];
            this.date = new Date();
        }
    }



}
