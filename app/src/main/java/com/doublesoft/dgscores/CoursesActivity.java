package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CoursesActivity extends AppCompatActivity {

    Context context;
    Cursor courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();
        courses = db.getCourses();
        courses.moveToFirst();

        LinearLayout layout = (LinearLayout) findViewById(R.id.course_list);
        TextView t = new TextView(context);
        for(int i=0;i<courses.getCount();i++) {
            t.append("Course name: " + courses.getString(courses.getColumnIndex("NAME")) + "\n" +
                    "Course holes: " + courses.getString(courses.getColumnIndex("HOLE_COUNT")) + "\n" +
                    "Course par: " + courses.getString(courses.getColumnIndex("PAR")) + "\n\n");
            courses.moveToNext();
        }
        layout.addView(t);

        FloatingActionButton btnAddCourse = (FloatingActionButton) findViewById(R.id.btn_add_course);

        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CoursesActivity.this, AddCourseActivity.class);
                CoursesActivity.this.startActivity(i);
            }
        });
    }


}
