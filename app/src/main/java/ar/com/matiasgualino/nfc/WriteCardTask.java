package ar.com.matiasgualino.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import ar.com.matiasgualino.nfc.activities.MainActivity;
import ar.com.matiasgualino.nfc.model.User;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by mgualino on 1/5/16.
 */
public class WriteCardTask extends AsyncTask<Void, Void, Boolean> {

    public static int CHARGE = 1;
    public static int CONFIGURE = 0;

    User user;
    Tag tag;
    int state;
    Context context;

    public WriteCardTask(User user, Tag tag, int state, Context context) {
        this.user = user;
        this.tag = tag;
        this.state = state;
        this.context = context;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (this.state == CHARGE) {
            final MaterialDialog informationDialog = new MaterialDialog.Builder(context)
                    .title(result ? "Tu carga se realizó con éxito!" : "Ups! La recarga no pudo ser realizada.")
                    .positiveText("ACEPTAR")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                            try {
                                context.startActivity(new Intent(context, MainActivity.class));
                            } catch (Exception ex) {
                                Log.e("TARJETA_VOS", "ERROR = " + ex.getLocalizedMessage());
                            }
                        }
                    })
                    .build();

            informationDialog.show();
        } else {
            final MaterialDialog configurationDialog = new MaterialDialog.Builder(context)
                    .title(result ? "La tarjeta se configuró exitosamente!" : "Ups! La tarjeta no pudo ser configurada.")
                    .positiveText("ACEPTAR")
                    .build();
            configurationDialog .show();
        }

    }


    @Override
    protected Boolean doInBackground(Void... params) {
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            boolean auth = false;
            int bIndex = 0;

            mfc.connect();

            if (state == CONFIGURE) {
                auth = mfc.authenticateSectorWithKeyB(User.NAME_DATA_SECTOR, MifareClassic.KEY_DEFAULT);
                if (auth) {
                    bIndex = mfc.sectorToBlock(User.NAME_DATA_SECTOR);
                    mfc.writeBlock(bIndex + User.FIRST_NAME_BLOCK, Utils.stringToWrite(user.getFirstName()));
                    mfc.writeBlock(bIndex + User.LAST_NAME_BLOCK, Utils.stringToWrite(user.getLastName()));
                } else {
                    Log.e("TARJETA_VOS", "ERROR AUTH");
                    return false;
                }
            }

            auth = mfc.authenticateSectorWithKeyB(User.PRIVATE_DATA_SECTOR, MifareClassic.KEY_DEFAULT);
            if (auth) {
                bIndex = mfc.sectorToBlock(User.PRIVATE_DATA_SECTOR);
                if (state == CONFIGURE) {
                    mfc.writeBlock(bIndex + User.IDENTIFICATION_BLOCK, Utils.stringToWrite(user.getIdentification()));
                }

                DecimalFormat df = new DecimalFormat("####0.00");
                String am = df.format(user.getAccountMoney());
                am = am.replace(".", "");
                am = am.replace(",", "");

                mfc.writeBlock(bIndex + User.ACCOUNT_MONEY_BLOCK, Utils.stringToWrite(am));
            } else {
                Log.e("TARJETA_VOS", "ERROR AUTH");
                return false;
            }

            mfc.close();
        } catch (IOException e) {
            Log.e("TARJETA_VOS", "ERROR = " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }
}