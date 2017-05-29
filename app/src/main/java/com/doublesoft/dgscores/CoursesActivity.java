package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CoursesActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();
        Cursor courses = db.getCourses();
        courses.moveToFirst();

        // kerätään tarvittavat tiedot radoista
        ArrayList<String[]> courseTexts = new ArrayList<>();
        for(int i=0;i<courses.getCount();i++) {
            String[] s = new String[3];
            s[0] = courses.getString(courses.getColumnIndex("NAME"));
            s[1] = courses.getString(courses.getColumnIndex("HOLE_COUNT"));
            s[2] = courses.getString(courses.getColumnIndex("PAR"));
            courseTexts.add(s);
            courses.moveToNext();
        }
        courses.close();
        db.close();

        ListView listView = (ListView) findViewById(R.id.listView_courses);
        CourseAdapter adapter = new CourseAdapter(context, R.layout.scorecards_game, courseTexts);
        listView.setAdapter(adapter);

        FloatingActionButton btnAddCourse = (FloatingActionButton) findViewById(R.id.btn_add_course);

        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CoursesActivity.this, AddCourseActivity.class);
                CoursesActivity.this.startActivity(i);
            }
        });
    }

    private class CourseAdapter extends ArrayAdapter<String[]> {

        ArrayList<String[]> rows;
        int layoutResource;

        public CourseAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String[]> objects) {
            super(context, resource, objects);
            rows = (ArrayList<String[]>) objects;
            layoutResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v = convertView;
            if(v == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                v = inflater.inflate(layoutResource, null);
            }

            String[] row = getItem(position);

            if (row != null){
                TextView name = (TextView) v.findViewById(R.id.textView_course);
                TextView holes = (TextView) v.findViewById(R.id.textView_players);
                TextView par = (TextView) v.findViewById(R.id.textView_date);

                name.setText("Name: " + row[0]);
                holes.setText("Holes: " + row[1]);
                par.setText("Par: " + row[2]);
            }

            return v;
        }
    }

}
