package com.upt.cti.smartwallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.upt.cti.sartwallet.model.MonthlyExpenses;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private TextView tStatus;
    private EditText eIncome, eExpenses;
    private Spinner monthSpinner;
    private String currentMonth;
    // firebase
    private DatabaseReference databaseReference;
    private ValueEventListener databaseListener;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tStatus = (TextView) findViewById(R.id.tStatus);
        eIncome = (EditText) findViewById(R.id.eIncome);
        eExpenses = (EditText) findViewById(R.id.eExpenses);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        monthSpinner = (Spinner) findViewById(R.id.eMonths);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(arrayAdapter);

        try {
            databaseReference.child("calendar").orderByKey();
            databaseReference.child("calendar").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean changed = false;
                    final List<String> monthList = new ArrayList<String>();
                    arrayAdapter.clear();
                    for (DataSnapshot monthDataSnapshot : dataSnapshot.getChildren()) {
                        String month = monthDataSnapshot.getKey();
                        monthList.add(month);
                    }

                    arrayAdapter.addAll(monthList);
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Could not populate the spinner, please check your internet connection!",
                    Toast.LENGTH_SHORT).show();
        }
        monthSpinner.setOnItemSelectedListener(MainActivity.this);
    }

    private void getMonthData(String newMonth) {
        // remove previous databaseListener
        if (databaseReference != null && currentMonth != null && databaseListener != null) {
            databaseReference.child("calendar").child(currentMonth).removeEventListener(databaseListener);
            Log.d("MainActivityCustom", "deleting listener for " + currentMonth);
        }
        currentMonth = newMonth;

        Log.d("MainActivityCustom", "creating listener for " + currentMonth);
        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MainActivityCustom", "onDataChange for " + currentMonth);
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try {
                    MonthlyExpenses monthlyExpense = dataSnapshot.getValue(MonthlyExpenses.class);
                    // explicit mapping of month name from entry key
                    monthlyExpense.month = dataSnapshot.getKey();

                    eIncome.setText(String.valueOf(monthlyExpense.getIncome()));
                    eExpenses.setText(String.valueOf(monthlyExpense.getExpenses()));
                    tStatus.setText("Found entry for " + currentMonth);
                    Log.d("MainActivityCustom","Found entry for " + currentMonth);
                }
                catch(NullPointerException e) {
                    Toast.makeText(getApplicationContext(),
                            "No data found for the month specified!",
                            Toast.LENGTH_SHORT).show();
                    tStatus.setText("No entry was found for " + currentMonth);
                    Log.d("MainActivityCustom","No entry was found for " + currentMonth);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };

        // set new databaseListener
        databaseReference.child("calendar").child(currentMonth).addValueEventListener(databaseListener);
    }

    private void updateExpensesAndIncome() {

        if (databaseReference != null && currentMonth != null
                && eExpenses != null && eIncome != null ) {
            Log.d("MainActivityCustom", "updateExpensesAndIncome for " + currentMonth);
            try {
                MonthlyExpenses monthlyExpense =  new MonthlyExpenses(null,
                        Float.parseFloat(eIncome.getText().toString()),
                        Float.parseFloat(eExpenses.getText().toString()));
                databaseReference.child("calendar").child(currentMonth).setValue(monthlyExpense)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),
                                        "Data was successfully updated.",
                                        Toast.LENGTH_SHORT).show();
                                tStatus.setText("Data was updated for " + currentMonth);
                                Log.d("MainActivityCustom","Data was updated for " + currentMonth);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),
                                        "Data failed to pe updated.",
                                        Toast.LENGTH_SHORT).show();
                                tStatus.setText("Failed to update data " + currentMonth);
                                Log.d("MainActivityCustom","Failed to update data " + currentMonth);
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Please make sure the values of income and expenses are correct!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "Please make sure you have connection to the internet" +
                            " and the current month is specified!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void clicked(View view) {
            switch (view.getId()) {
                case R.id.bUpdate:
                    tStatus.setText("Updating ...");
                    Log.d("MainActivityCustom","Updating ... " + currentMonth);
                    updateExpensesAndIncome();
                    break;
            }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String newMonth =parent.getItemAtPosition(pos).toString().toLowerCase();
        tStatus.setText("Searching ...");
        Log.d("MainActivityCustom", "Searching ... " + newMonth);
        getMonthData(newMonth);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do nothing
    }
}
