package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChooseCourseActivity extends AppCompatActivity {

    Context context;
    Cursor courses;
    DatabaseAdapter db;
    ListView listView;
    ArrayList<String> players;
    FloatingActionButton addCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_course);
        context = this;
        //TODO: otsikko ei näy supportactionbarissa?
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        players = intent.getStringArrayListExtra("players");
        String courseName = intent.getStringExtra("courseName");
        getSupportActionBar().setTitle(courseName);
        db = new DatabaseAdapter(context);
        db.open();

        courses = db.getCourses();
        listView = (ListView) findViewById(R.id.choose_course_listview);

        addCourse = (FloatingActionButton) findViewById(R.id.btnAddCoursePlayCourse);
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChooseCourseActivity.this, AddCourseActivity.class);
                ChooseCourseActivity.this.startActivity(i);
            }
        });

        CourseCursorAdapter adapter = new CourseCursorAdapter(context, courses, 0);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout l = (LinearLayout) view;
                String[] s = ((TextView) l.getChildAt(0)).getText().toString().split(":");
                String courseName = s[1].trim();
                Intent i = new Intent(ChooseCourseActivity.this, PlayCourse.class);
                i.putExtra("courseName", courseName);
                i.putStringArrayListExtra("players", players);
                context.startActivity(i);
            }
        });


    }

    private class CourseCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;

        CourseCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(R.layout.choose_course_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LinearLayout l = (LinearLayout) view;
            String name = cursor.getString(cursor.getColumnIndex("NAME"));
            String holeCount = cursor.getString(cursor.getColumnIndex("HOLE_COUNT"));
            String par = cursor.getString(cursor.getColumnIndex("PAR"));
            ((TextView) l.getChildAt(0)).setText("Name: " + name);
            ((TextView) l.getChildAt(1)).setText("Holes: " + holeCount);
            ((TextView) l.getChildAt(2)).setText("Par: " + par);
        }
    }
}
