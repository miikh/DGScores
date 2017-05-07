package com.doublesoft.dgscores;

import java.util.Calendar;
import java.util.Date;

/** Käytettävät oliot:
 *
 * Fairway, Course, Player
 *
 * Created by yone on 9.10.2015.
 */
public class Objects {

    public class Fairway {
        long _id;
        long courseId;
        int par;
        int distance;
        String name;

        public Fairway() {}

        public Fairway(long id, long courseId, int par, int distance, String name){
            this._id = id;
            this.courseId = courseId;
            this.par = par;
            this.distance = distance;
            this.name = name;
        }

        public long get_id() {
            return _id;
        }
        public long getCourseId() {
            return courseId;
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
        long _id;
        String name;

        public Player() {}

        public Player (long _id,String name){
            this._id = _id;
            this.name = name;
        }

        public long get_id() {
            return _id;
        }
        public String getName(){
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class Scorecards {
        long _id;
        long fairwayId;
        long gameId;
        int ob;
        int throwCount;
        Date date;

        public Scorecards(long _id, long faiwayId, long gameId, int ob, int throwCount){
            this._id = _id;
            this.fairwayId = faiwayId;
            this.gameId = gameId;
            this.ob = ob;
            this.throwCount = throwCount;
            this.date = Calendar.getInstance().getTime();
        }

        public long get_id() {
            return _id;
        }
        public long getFairwayId() {
            return fairwayId;
        }
        public long getGameId() {
            return gameId;
        }
        public int getOb() {
            return ob;
        }
        public int getThrowCount() {
            return throwCount;
        }
        public Date getDate() {
            return date;
        }
    }



}
