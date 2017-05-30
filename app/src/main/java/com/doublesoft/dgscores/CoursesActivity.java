package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CoursesActivity extends AppCompatActivity {

    Context context;
    ArrayList<String[]> courseData;
    CourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        courseData = getCourseData();

        ListView listView = (ListView) findViewById(R.id.listView_courses);
        adapter = new CourseAdapter(context, R.layout.scorecards_game, courseData);
        listView.setAdapter(adapter);

        FloatingActionButton btnAddCourse = (FloatingActionButton) findViewById(R.id.btn_add_course);

        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CoursesActivity.this, AddCourseActivity.class);
                CoursesActivity.this.startActivityForResult(i, 1);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(CoursesActivity.this, AddCourseActivity.class);
                i.putExtra("courseId", (String)view.getTag());
                startActivity(i);
            }
        });

        registerForContextMenu(listView);
    }

    private ArrayList<String[]> getCourseData(){
        DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();
        Cursor courses = db.getCourses();
        courses.moveToFirst();

        // kerätään tarvittavat tiedot radoista
        ArrayList<String[]> courseTexts = new ArrayList<>();
        for(int i=0;i<courses.getCount();i++) {
            String[] s = new String[4];
            s[0] = courses.getString(courses.getColumnIndex("NAME"));
            s[1] = courses.getString(courses.getColumnIndex("HOLE_COUNT"));
            s[2] = courses.getString(courses.getColumnIndex("PAR"));
            s[3] = courses.getString(0); // _id
            courseTexts.add(s);
            courses.moveToNext();
        }
        courses.close();
        db.close();

        return courseTexts;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                courseData = getCourseData();
                String[] newRow = new String[4];
                newRow[0] = data.getStringExtra("name");
                newRow[1] = data.getStringExtra("holeCount");
                newRow[2] = data.getStringExtra("par");
                newRow[3] = data.getStringExtra("id");
                adapter.add(newRow);
                adapter.notifyDataSetChanged();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if(item.getItemId() ==  R.id.delete)
        {
            long id = Long.parseLong(menuInfo.targetView.getTag().toString());
            DatabaseAdapter db = new DatabaseAdapter(context);
            db.open();
            db.deleteCourse(id);
            db.close();
            adapter.remove(adapter.getItem(menuInfo.position));
            adapter.notifyDataSetChanged();
        }
        else return false;
        return true;
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

            if(row != null) {
                // laitetaan tagiksi courseId
                v.setTag(row[3]);

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
