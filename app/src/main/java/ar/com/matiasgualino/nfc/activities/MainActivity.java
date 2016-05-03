package ar.com.matiasgualino.nfc.activities;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import ar.com.matiasgualino.nfc.R;
import ar.com.matiasgualino.nfc.Utils;
import ar.com.matiasgualino.nfc.WriteCardTask;
import ar.com.matiasgualino.nfc.model.User;

public class MainActivity extends AppCompatActivity {

    NfcAdapter mAdapter;
    PendingIntent pendingIntent;

    Context context;

    IntentFilter mFilters[];
    String[][] mTechLists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;


        mAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };

        Intent intent = getIntent();

        resolveIntent(intent);

    }

    void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            try {
                readCardData(mfc, tagFromIntent);
            } catch (IOException e) {
                Log.e("TARJETA_VOS", "ERROR = " + e.getLocalizedMessage());
            }
        }
    }

    private synchronized void writeCardData(User user, Tag tag) throws IOException, ExecutionException, InterruptedException {

        new WriteCardTask(user, tag, WriteCardTask.CONFIGURE, this).execute().get();

    }

    private void readCardData(MifareClassic mfc, Tag tag) throws IOException {
        boolean auth = false;
        int bCount = 0;
        int bIndex = 0;
        byte[] data;
        User user = new User();

        mfc.connect();
        auth = mfc.authenticateSectorWithKeyA(User.NAME_DATA_SECTOR, MifareClassic.KEY_DEFAULT);
        if (auth) {
            bIndex = mfc.sectorToBlock(User.NAME_DATA_SECTOR);

            data = mfc.readBlock(bIndex + User.FIRST_NAME_BLOCK);
            String hexFirstName = Utils.bytesToHex(data);
            if (!hexFirstName.equals("00000000000000000000000000000000")) {
                String firstName = Utils.unHex(hexFirstName);
                Log.d("TARJETA_VOS", "NAME = " + firstName);
                user.setFirstName(firstName);
            }

            data = mfc.readBlock(bIndex + User.LAST_NAME_BLOCK);
            String hexLastName = Utils.bytesToHex(data);
            if (!hexLastName.equals("00000000000000000000000000000000")) {
                String lastName = Utils.unHex(hexLastName);
                Log.d("TARJETA_VOS", "LAST_NAME = " + lastName);
                user.setLastName(lastName);
            }
        } else {
            Log.e("TARJETA_VOS", "ERROR AUTH");
        }

        auth = mfc.authenticateSectorWithKeyA(User.PRIVATE_DATA_SECTOR, MifareClassic.KEY_DEFAULT);
        if (auth) {
            bIndex = mfc.sectorToBlock(User.PRIVATE_DATA_SECTOR);

            data = mfc.readBlock(bIndex + User.IDENTIFICATION_BLOCK);
            String hexIdentification= Utils.bytesToHex(data);
            if (!hexIdentification.equals("00000000000000000000000000000000")) {
                String identification = Utils.unHex(hexIdentification);
                Log.d("TARJETA_VOS", "IDENTIFICATION = " + identification);
                user.setIdentification(identification);
            }

            data = mfc.readBlock(bIndex + User.ACCOUNT_MONEY_BLOCK);
            String hexAccountMoney = Utils.bytesToHex(data);
            if (!hexAccountMoney.equals("00000000000000000000000000000000")) {
                String accountMoneyString = Utils.unHex(hexAccountMoney);
                Log.d("TARJETA_VOS", "ACCOUNT_MONEY = " + accountMoneyString);
                Double accountMoney = new Double(0);
                try {
                    accountMoney = Double.valueOf(accountMoneyString);
                } catch (Exception ex) {
                    Log.d("TARJETA_VOS", "Error con double");
                }
                user.setAccountMoney(accountMoney/100.0);
            }
        } else { // Authentication failed - Handle it
            Log.e("TARJETA_VOS", "ERROR AUTH");
        }

        mfc.close();

        if ((user.getFirstName() == null || user.getFirstName().isEmpty())
                || (user.getLastName() == null || user.getLastName().isEmpty())
                || (user.getIdentification() == null || user.getIdentification().isEmpty())) {
            showInformationDialog(tag);
        } else {
            Intent chargeIntent = new Intent(this, ChargeActivity.class);
            chargeIntent.putExtra("user", user);
            chargeIntent.putExtra("tag", tag);
            startActivity(chargeIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        resolveIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void showInformationDialog(final Tag tag) {
        final MaterialDialog informationDialog = new MaterialDialog.Builder(context)
                .title("Ingresa tus datos personales para configurar la tarjeta")
                .customView(R.layout.dialog_information, true)
                .positiveText("ACEPTAR")
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        EditText firstNameET = (EditText) materialDialog.getCustomView().findViewById(R.id.first_name_edittext);
                        EditText lastNameET = (EditText) materialDialog.getCustomView().findViewById(R.id.last_name_edittext);
                        EditText identificationET = (EditText) materialDialog.getCustomView().findViewById(R.id.identification_edittext);

                        User user = new User();
                        user.setFirstName(firstNameET.getText().toString());
                        user.setLastName(lastNameET.getText().toString());
                        user.setIdentification(identificationET.getText().toString());
                        user.setAccountMoney(new Double(0));

                        try {
                            writeCardData(user, tag);
                        } catch (Exception e) {
                            Log.e("TARJETA_VOS", "ERROR = " + e.getLocalizedMessage());
                        }

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                    }
                })
                .build();
        informationDialog.show();
    }

}
