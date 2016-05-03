package ar.com.matiasgualino.nfc.model;

import java.io.Serializable;

/**
 * Created by mgualino on 1/5/16.
 */
public class User implements Serializable {

    public static final int NAME_DATA_SECTOR = 27;
    public static final int FIRST_NAME_BLOCK = 0;
    public static final int LAST_NAME_BLOCK = 1;
    public static final int PRIVATE_DATA_SECTOR = 28;
    public static final int IDENTIFICATION_BLOCK = 0;
    public static final int ACCOUNT_MONEY_BLOCK = 1;

    private String firstName;
    private String lastName;
    private String identification;

    public Double getAccountMoney() {
        return accountMoney;
    }

    public void setAccountMoney(Double accountMoney) {
        this.accountMoney = accountMoney;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private Double accountMoney;

}
