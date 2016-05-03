package ar.com.matiasgualino.nfc.activities;

import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mercadopago.core.MercadoPago;
import ar.com.matiasgualino.nfc.R;
import ar.com.matiasgualino.nfc.WriteCardTask;
import ar.com.matiasgualino.nfc.model.User;

import java.text.DecimalFormat;

public class ChargeActivity extends AppCompatActivity {

    private User user;
    private Tag tag;

    private EditText amount;
    private TextView name;
    private TextView identification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        user = (User) getIntent().getSerializableExtra("user");
        tag = getIntent().getParcelableExtra("tag");

        amount = (EditText) findViewById(R.id.amount);
        name = (TextView) findViewById(R.id.name);
        identification = (TextView) findViewById(R.id.identification);
        TextView account_money = (TextView) findViewById(R.id.account_money);

        name.setText("Nombre y apellido: " + user.getFirstName() + " " + user.getLastName());
        identification.setText("DNI: " + user.getIdentification());
        DecimalFormat df = new DecimalFormat("####0.00");
        account_money.setText("Saldo actual: $" + df.format(user.getAccountMoney()));

    }

    public void submit(View view) {

        // Iniciar el checkout de MercadoPago
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey("444a9ef5-8a6b-429f-abdf-587639155d88")
                .startGuessingCardActivity();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {

        if (requestCode == MercadoPago.GUESSING_CARD_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    EditText amountET = (EditText) findViewById(R.id.amount);
                    Double amount = Double.valueOf(amountET.getText().toString());
                    user.setAccountMoney(user.getAccountMoney() + amount);
                    new WriteCardTask(user, tag, WriteCardTask.CHARGE, this).execute().get();
                } catch(Exception ex) {
                    Log.e("TARJETA_VOS", ex.getMessage());
                }


            } else if ((data != null) && (data.getSerializableExtra("apiException") != null)) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
