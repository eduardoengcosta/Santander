package br.com.entrevista.banco;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.security.keystore.UserPresenceUnavailableException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.entrevista.banco.Model.ContasUsuario;
import br.com.entrevista.banco.dao.DBLocal;
import br.com.entrevista.banco.utils.MaskEditUtil;
import br.com.entrevista.banco.utils.RequestsAdm;
import br.com.entrevista.banco.utils.Utilidades;

public class MainActivity extends AppCompatActivity {

    private Button btnLogar;
    private EditText edtEmail;
    private EditText edtSenha;

    public static final String Ref_login = "INFORMACOES_LOGIN_AUTOMATICO";

    //Loader
    private RelativeLayout mLoad;

    private ContasUsuario mContasUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciaViews();

        SharedPreferences prefs = getSharedPreferences(Ref_login, MODE_PRIVATE);
        String login= prefs.getString("login", null);
        if (login!= null) {
            edtEmail.setText(login);
            // existe configuração salvar
        } else {
            // não existe configuração salvar
        }
        iniciaAcoes();
    }

    private void iniciaAcoes() {
        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Validando o login
                String error = validateLogin(edtEmail.getText().toString(), edtSenha.getText().toString());
                if (error.equals("")) {
                    doLogin(edtEmail.getText().toString(), edtEmail.getText().toString());
                } else {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);

    public static boolean validarEmail(String email){
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private String validateLogin(String usuario, String senha) {
        //Verificando user
        if((usuario.matches("^[0-9]*$")) || (usuario.matches("^[0-9]*[.]{0,1}[0-9]*$"))){
            if(usuario.length() != 11){
                return "O CPF deve ter 11 dígitos";
            }else {
              edtEmail.addTextChangedListener(MaskEditUtil.mask(edtEmail, MaskEditUtil.FORMAT_CPF));
            }
        }else{
            if (validarEmail(usuario) == false){
                return "Email Inválido";
            }
        }

        //Verificando senha (uma letra maiuscula, um caracter especial e um caracter alfanumérico)
        return Utilidades.checkString(senha);
    }


    private void iniciaViews() {

        btnLogar = (Button) findViewById(R.id.bt_login);
        edtEmail = (EditText) findViewById(R.id.et_email);
        edtSenha = (EditText) findViewById(R.id.et_password);
        mLoad = (RelativeLayout) findViewById(R.id.all_load);
    }

    private ContasUsuario mountLoginResponse(String body) {

        ContasUsuario contasUsuario = new ContasUsuario();

        try {

            JSONObject userAccountJsonObject = new JSONObject(body).getJSONObject("userAccount");
            contasUsuario.setIdusuario(userAccountJsonObject.getInt("userId"));
            contasUsuario.setNome(userAccountJsonObject.getString("name"));
            contasUsuario.setCodBanco(userAccountJsonObject.getString("bankAccount"));
            contasUsuario.setAgencia(userAccountJsonObject.getString("agency"));
            contasUsuario.setBalanco(userAccountJsonObject.getString("balance"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contasUsuario;
    }

    public void doLogin(String user, String password) {
        SharedPreferences.Editor editor = getSharedPreferences(Ref_login, MODE_PRIVATE).edit();
        mLoad.setVisibility(View.VISIBLE);
        mContasUsuario = null;

        editor.putString("login", user);
        editor.commit();


        final RequestBody formBody = new FormEncodingBuilder()
                .add("user", user)
                .add("password", password)
                .build();

        RequestsAdm.postFormBody(
                formBody,
                "https://bank-app-test.herokuapp.com/api/login",
                new RequestsAdm.RespostaRequisicao() {
                    @Override
                    public void respostaSucesso(String body) {
                        super.respostaSucesso(body);

                        System.out.println("RESPOSTA LOGIN: " + body);
                        mContasUsuario = mountLoginResponse(body);

                    }

                    @Override
                    public void fimDaThread() {
                        super.fimDaThread();

                        //Aqui chamamos a UIThread
                        //Apenas ela pode atualizar componetes de tela no Android
                        Utilidades.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mContasUsuario == null) {
                                    Toast.makeText(MainActivity.this, "Usuário inválido!!", Toast.LENGTH_LONG).show();
                                } else {

                                    //Requisição obteve resposta, Usuario foi povoado
                                    //Inicializando banco local e salvando resposta
                                    DBLocal dbLocal = new DBLocal(MainActivity.this);
                                    dbLocal.saveUserAccount(mContasUsuario);

                                    startActivity(new Intent(MainActivity.this, PrincipalActivity.class));
                                    finish();
                                }
                                mLoad.setVisibility(View.GONE);
                            }
                        });
                    }
                }
        );

    }

}
