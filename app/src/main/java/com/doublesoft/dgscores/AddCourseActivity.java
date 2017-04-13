package com.doublesoft.dgscores;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;

public class AddCourseActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    NumberPicker holeNumberPicker;
    TableLayout holeTable;
    TableLayout.LayoutParams x = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
    int defaultHoleCount = 18;
    int prevCount = defaultHoleCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        holeNumberPicker = (NumberPicker) findViewById(R.id.holeNumberPicker);
        holeTable = (TableLayout) findViewById(R.id.holeTable);

        holeNumberPicker.setMinValue(1);
        holeNumberPicker.setMaxValue(50);
        holeNumberPicker.setWrapSelectorWheel(false);

        // Initialize default number of holes
        holeNumberPicker.setValue(defaultHoleCount);

        for (int i = 1; i <= defaultHoleCount; i++) {
            TableRow row = createHoleRow(i);
            holeTable.addView(row, x);
        }

        holeNumberPicker.setOnValueChangedListener(this);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int j, int j1) {
        int holes = holeNumberPicker.getValue();
        holeTable = (TableLayout) findViewById(R.id.holeTable);

        // If count increased, add hole
        if (prevCount < holes) {
            TableRow row = createHoleRow(holes);
            holeTable.addView(row, x);
        }

        // If count decreased, remove last hole
        else if (prevCount > holes) {
            int count = holeTable.getChildCount();
            holeTable.removeViewAt(count - 1);
        }

        prevCount = holes;
    }



    public TableRow createHoleRow(int holeNumber) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));

        EditText holeName = new EditText(this);
        holeName.setText("Hole " + holeNumber);
        holeName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));

        NumberPicker nb = new NumberPicker(this);
        nb.setValue(3);
        nb.setMinValue(1);
        nb.setMaxValue(50);
        nb.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));

        row.addView(holeName);
        row.addView(nb);

        return row;
    }
}