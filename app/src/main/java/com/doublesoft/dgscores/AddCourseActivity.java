package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.Toast;

public class AddCourseActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    NumberPicker holeNumberPicker;
    EditText courseNameView;
    Button saveCourse;
    LinearLayout holeTable;
    Context context;
    LinearLayout.LayoutParams x = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    int defaultHoleCount = 18;
    int prevCount = defaultHoleCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        context = this;
        courseNameView = (EditText)findViewById(R.id.course_name);
        holeNumberPicker = (NumberPicker) findViewById(R.id.holeNumberPicker);
        holeTable = (LinearLayout) findViewById(R.id.holeTable);

        holeNumberPicker.setMinValue(1);
        holeNumberPicker.setMaxValue(50);
        holeNumberPicker.setWrapSelectorWheel(false);

        saveCourse = (Button)findViewById(R.id.button_save_course);
        saveCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSaveCourseClick();
            }
        });
        // Initialize default number of holes
        holeNumberPicker.setValue(defaultHoleCount);

        for (int i = 1; i <= defaultHoleCount; i++) {
            View row = createHoleRow(i);
            holeTable.addView(row, x);
        }

        holeNumberPicker.setOnValueChangedListener(this);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int j, int j1) {
        int holes = holeNumberPicker.getValue();
        holeTable = (LinearLayout) findViewById(R.id.holeTable);

        // If count increased, add hole
        if (prevCount < holes) {
            View row = createHoleRow(holes);
            holeTable.addView(row, x);
        }

        // If count decreased, remove last hole
        else if (prevCount > holes) {
            int count = holeTable.getChildCount();
            holeTable.removeViewAt(count - 1);
        }

        prevCount = holes;
    }

    public View createHoleRow(int holeNumber) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.add_course_hole, null, false);
        EditText holeName = (EditText) row.findViewById(R.id.hole_name);
        NumberPicker nb = (NumberPicker) row.findViewById(R.id.hole_number_picker);
        EditText fairwayDistance = (EditText) row.findViewById(R.id.fairway_distance);

        holeName.setText("Hole " + holeNumber);

        nb.setMinValue(1);
        nb.setMaxValue(50);
        nb.setValue(3);
        nb.setWrapSelectorWheel(false);

        return row;
    }

    private void setSaveCourseClick(){

        int holeCount = holeNumberPicker.getValue();
        ContentValues courseValues = new ContentValues();
        String courseName = courseNameView.getText().toString().trim();
        int coursePar = 0;
        int courseDistance = 0;
        ContentValues[] fairwaysValues = new ContentValues[holeCount];
        for(int i=0;i<holeCount;i++){
            LinearLayout l = (LinearLayout) holeTable.getChildAt(i);
            int par = ((NumberPicker) l.getChildAt(2)).getValue();
            String distanceField = ((EditText) l.getChildAt(3)).getText().toString().trim();
            int distance = 0;
            if(!distanceField.equals("")) distance = Integer.parseInt(distanceField);
            String name = ((EditText) l.getChildAt(0)).getText().toString().trim();
            fairwaysValues[i] = new ContentValues();
            fairwaysValues[i].put("par", par);
            if(distance != 0) fairwaysValues[i].put("distance", distance);
            if(!name.equals("Hole " + (i+1))) fairwaysValues[i].put("name", name);

            coursePar += par;
            courseDistance += distance;

        }
        if(!courseName.equals("")) {
            courseValues.put("name", courseName);
            courseValues.put("hole_count", holeCount);
            courseValues.put("par", coursePar);
            if (courseDistance > 0) courseValues.put("distance", courseDistance);

            DatabaseAdapter db = new DatabaseAdapter(context);
            db.open();
            db.insertCourse(courseValues, fairwaysValues);
            db.close();

            Toast.makeText(context, "Course " + courseName + " added", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        else{
            courseNameView.setError(getString(R.string.course_name_error));
        }
    }
}
