package br.com.entrevista.banco.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.entrevista.banco.Model.ContasUsuario;

public class DBLocal extends SQLiteOpenHelper {
    private static final String DATABASE = "dbSatander";
    private static final int VERSION = 1;

    public DBLocal(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String userAccount = "CREATE TABLE userAccount " +
                " ( " +
                " user_id INTEGER PRIMARY KEY NOT NULL," +
                " name TEXT NULL," +
                " bankAccount TEXT NULL," +
                " agency TEXT NULL," +
                " balance TEXT NULL" +
                " );";
        db.execSQL(userAccount);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Em caso de alteração do banco
    }

    public long saveUserAccount(ContasUsuario contasUsuario) {
        ContentValues values = new ContentValues();

        values.put("user_id", contasUsuario.getIdusuario());
        values.put("name", contasUsuario.getNome());
        values.put("bankAccount", contasUsuario.getCodBanco());
        values.put("agency", contasUsuario.getAgencia());
        values.put("balance", contasUsuario.getBalanco());

        return getWritableDatabase().insert("userAccount", null, values);

    }

    public void alterarUserAccount(ContasUsuario contasUsuario) {
        ContentValues values = new ContentValues();

        values.put("user_id", contasUsuario.getIdusuario());
        values.put("name", contasUsuario.getNome());
        values.put("bankAccount", contasUsuario.getCodBanco());
        values.put("agency", contasUsuario.getAgencia());
        values.put("balance", contasUsuario.getBalanco());


        String[] args = {contasUsuario.getIdusuario().toString()};

        try {
            if (getWritableDatabase() != null) {
                getWritableDatabase().update("userAccount", values, "user_id=?", args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUserAccount(ContasUsuario contasUsuario) {

        String[] args = {contasUsuario.getIdusuario().toString()};
        getWritableDatabase().delete("userAccount", "user_id=?", args);

    }

    public void cleanDb() {
        getWritableDatabase().execSQL("delete from userAccount");
    }

    public ContasUsuario getContasUsuario() {

        Cursor cursor = getWritableDatabase()
                .rawQuery("SELECT * from userAccount;", null);

        ContasUsuario u = new ContasUsuario();

        if (cursor.getCount() != 0) {

            cursor.moveToNext();

            u.setIdusuario(cursor.getInt(0));
            u.setNome(cursor.getString(1));
            u.setCodBanco(cursor.getString(2));
            u.setAgencia(cursor.getString(3));
            u.setBalanco(cursor.getString(4));

            getWritableDatabase().close();

            return u;
        }

        getWritableDatabase().close();

        return null;
    }
}
