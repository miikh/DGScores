package com.doublesoft.dgscores;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;

public class AddCourseActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    NumberPicker holeNumberPicker;
    LinearLayout holeTable;
    LinearLayout.LayoutParams x = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    int defaultHoleCount = 18;
    int prevCount = defaultHoleCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        holeNumberPicker = (NumberPicker) findViewById(R.id.holeNumberPicker);
        holeTable = (LinearLayout) findViewById(R.id.holeTable);

        holeNumberPicker.setMinValue(1);
        holeNumberPicker.setMaxValue(50);
        holeNumberPicker.setWrapSelectorWheel(false);

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
        EditText holeName = (EditText) row.findViewById(R.id.editText2);
        NumberPicker nb = (NumberPicker) row.findViewById(R.id.numberPicker2);

        holeName.setText("Hole " + holeNumber);

        nb.setValue(3);
        nb.setMinValue(1);
        nb.setMaxValue(50);
        nb.setWrapSelectorWheel(false);

        return row;
    }
}
