package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class AddCourseActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    NumberPicker holeNumberPicker;
    EditText courseNameView;
    Button saveCourse;
    LinearLayout holeTable;
    Context context;
    TextView coursePar;
    LinearLayout.LayoutParams x = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    int defaultHoleCount = 18;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        context = this;
        courseNameView = (EditText)findViewById(R.id.course_name);
        holeNumberPicker = (NumberPicker) findViewById(R.id.holeNumberPicker);
        holeTable = (LinearLayout) findViewById(R.id.holeTable);
        coursePar = (TextView) findViewById(R.id.textView_coursePar);

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

        // Initialize hole rows
        for (int i = 1; i <= defaultHoleCount; i++) {
            View row = createHoleRow(i);
            holeTable.addView(row, x);
        }

        holeNumberPicker.setOnValueChangedListener(this);

        updateTotalPar();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        int holes = holeNumberPicker.getValue();
        holeTable = (LinearLayout) findViewById(R.id.holeTable);

        // If count increased, add hole
        if (oldVal < newVal) {
            View row = createHoleRow(holes);
            holeTable.addView(row, x);
        }

        // If count decreased, remove last hole
        else if (oldVal > newVal) {
            int count = holeTable.getChildCount();
            holeTable.removeViewAt(count - 1);
        }
        updateTotalPar();
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

        nb.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                int par = Integer.parseInt(coursePar.getText().toString());

                if (oldVal < newVal) {
                    par++;
                }

                else if (oldVal > newVal) {
                    par--;
                }
                coursePar.setText(Integer.toString(par));
            }
        });


        return row;
    }

    // Loops values from hole row par pickers and sets total course par
    private void updateTotalPar() {
        int holeCount = holeNumberPicker.getValue();
        int _coursePar = 0;
        for(int i=0;i<holeCount;i++) {
            LinearLayout l = (LinearLayout) holeTable.getChildAt(i);
            int par = ((NumberPicker) l.getChildAt(2)).getValue();
            _coursePar += par;
        }
        coursePar.setText(Integer.toString(_coursePar));
    }

    private void setSaveCourseClick(){

        int holeCount = holeNumberPicker.getValue();
        ContentValues courseValues = new ContentValues();
        String courseName = courseNameView.getText().toString().trim();
        int courseDistance = 0;
        ContentValues[] fairwaysValues = new ContentValues[holeCount];
        int par = 0;
        for(int i=0;i<holeCount;i++){
            LinearLayout l = (LinearLayout) holeTable.getChildAt(i);
            par = ((NumberPicker) l.getChildAt(2)).getValue();
            String distanceField = ((EditText) l.getChildAt(3)).getText().toString().trim();
            int distance = 0;
            if(!distanceField.equals("")) distance = Integer.parseInt(distanceField);
            String name = ((EditText) l.getChildAt(0)).getText().toString().trim();
            fairwaysValues[i] = new ContentValues();
            fairwaysValues[i].put("par", par);
            if(distance != 0) fairwaysValues[i].put("distance", distance);
            if(!name.equals("Hole " + (i+1))) fairwaysValues[i].put("name", name);

            courseDistance += distance;

        }
        if(!courseName.equals("")) {
            courseValues.put("name", courseName);
            courseValues.put("hole_count", holeCount);
            courseValues.put("par", Integer.parseInt(coursePar.getText().toString()));
            if (courseDistance > 0) courseValues.put("distance", courseDistance);

            DatabaseAdapter db = new DatabaseAdapter(context);
            db.open();
            long id = db.insertCourse(courseValues, fairwaysValues);
            db.close();
            Intent intent = new Intent();
            intent.putExtra("name", courseName);
            intent.putExtra("holeCount", String.valueOf(holeCount));
            intent.putExtra("par", String.valueOf(par));
            intent.putExtra("id", String.valueOf(id));
            Toast.makeText(context, "Course " + courseName + " added", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK, intent);
            this.finish();
        }
        else{
            courseNameView.setError(getString(R.string.course_name_error));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("requestCode", 1);
        super.onBackPressed();
    }
}
