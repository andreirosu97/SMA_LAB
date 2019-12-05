package com.upt.cti.smartwallet.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.upt.cti.sartwallet.model.AppState;
import com.upt.cti.sartwallet.model.Payment;
import com.upt.cti.sartwallet.model.PaymentType;
import com.upt.cti.smartwallet.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPayment extends AppCompatActivity {

    private EditText eName;
    private EditText eCost;
    private Spinner sType;
    private TextView tTimestamp;
    private Payment payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        setTitle("Add or edit payment");

// ui
        eName = (EditText) findViewById(R.id.eName);
        eCost = (EditText) findViewById(R.id.eCost);
        sType = (Spinner) findViewById(R.id.sType);
        tTimestamp = (TextView) findViewById(R.id.tTimestamp);

// spinner adapter
        String[] types = PaymentType.getTypes();
        final ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sType.setAdapter(sAdapter);

// initialize UI if editing
        payment = AppState.get().getCurrentPayment();
        if (payment != null) {
            eName.setText(payment.getName());
            eCost.setText(String.valueOf(payment.getCost()));
            tTimestamp.setText("Time of payment: " + payment.timestamp);
            try {
                sType.setSelection(Arrays.asList(types).indexOf(payment.getType()));
            } catch (Exception e) {
            }
        } else {
            tTimestamp.setText("");
        }
    }

    private void delete(String timestamp) {
        AppState.get().getDatabaseReference().child("wallet").child(timestamp).removeValue();

        // finishes the current activity and returns to the last activity on the stack
        finish();
    }

    private void save(String timestamp) {
        Payment payment = new Payment(null, eName.getText().toString(), Double.parseDouble(eCost.getText().toString()),(String)sType.getSelectedItem());
        Log.i("Saving payment", timestamp);

        AppState.get().getDatabaseReference().child("wallet").child(timestamp).setValue(payment);
//        AppState.get().getDatabaseReference().child("wallet").child(timestamp).setValue(map);

    }


    public void clicked(View view) {
        switch (view.getId()) {
            case R.id.bSave:
                if (payment != null)
                    save(payment.timestamp);
                else
                    save(AppState.getCurrentTimeDate());
                break;
            case R.id.bDelete:
                if (payment != null)
                    delete(payment.timestamp);
                else
                    Toast.makeText(this, "Payment does not exist", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
