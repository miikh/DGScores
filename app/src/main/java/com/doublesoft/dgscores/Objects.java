package com.doublesoft.dgscores;

import java.util.Date;

/** Käytettävät oliot:
 *
 * Hole, Course, Player
 *
 * Created by yone on 9.10.2015.
 */
public class Objects {

    public class Hole {
        int par;
        int distance;

        public Hole () {}

        public Hole (int par, int distance){
            this.par = par;
            this.distance = distance;
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
        String name;
        int hole_count;
        Hole[] holes;

        public Course () {}

        public Course(String name, int hole_count){
            this.name = name;
            this.hole_count = hole_count;
            this.holes = new Hole[hole_count];
        }

        public String getName() {
            return name;
        }
        public int getHole_count() {
            return hole_count;
        }
        public Hole[] getHoles() {
            return holes;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setHole_count(int hole_count) {
            this.hole_count = hole_count;
        }
        public void setHoles(Hole[] holes) {
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
            this.results = new int[players.length][course.getHole_count()];
            this.date = new Date();
        }
    }



}
