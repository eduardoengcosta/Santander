package br.com.entrevista.banco;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import br.com.entrevista.banco.Model.ContasUsuario;
import br.com.entrevista.banco.Model.Statement;
import br.com.entrevista.banco.adapters.StatementsAdapter;
import br.com.entrevista.banco.dao.DBLocal;
import br.com.entrevista.banco.utils.RequestsAdm;
import br.com.entrevista.banco.utils.Utilidades;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class  PrincipalActivity extends AppCompatActivity {

    private TextView mName;
    private TextView mAccount;
    private TextView mBalance;

    private ImageView mLogout;
    private ProgressBar mLoadStatements;

    private LinearLayout mLlTryAgain;
    private Button mTryAgain;

    private List<Statement> mStatements;
    private StatementsAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ContasUsuario mUserAccount;

    //Confirmação de saída da aplicação
    private Dialog mDialogLogout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        initViews();
        initActions();
        loadData();
        
    }

    private void loadData() {
        //Obtendo user account salva no banco local
        DBLocal bancoLocal = new DBLocal(PrincipalActivity.this);
        mUserAccount = bancoLocal.getContasUsuario();

        mName.setText(mUserAccount.getNome());
        mAccount.setText(mUserAccount.getCodBanco() + " / " + Utilidades.formatAgency(mUserAccount.getAgencia()));
        Utilidades.setCurrencyText(mBalance, mUserAccount.getBalanco());

        //Chamando requisição para obtenção dos statmentes
        getStatements(mUserAccount);
    }

    private void initActions() {
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialogLogout();
            }
        });

        //Botão exibido quando não houve conexão com a internet
        mTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStatements(mUserAccount);
            }
        });
    }

    private void initViews() {
        mName = (TextView) findViewById(R.id.tv_name);
        mAccount = (TextView) findViewById(R.id.tv_account_number);
        mBalance = (TextView) findViewById(R.id.tv_balance);

        //Eh exibido quando nao tem internet
        mLlTryAgain = (LinearLayout) findViewById(R.id.ll_try_again);
        mTryAgain = (Button) findViewById(R.id.btn_try_again);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_statements);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mLogout = (ImageView) findViewById(R.id.btn_logout);
        mLoadStatements = (ProgressBar) findViewById(R.id.pb_statements);
    }
    public void getStatements(ContasUsuario userAccount) {

        mLlTryAgain.setVisibility(View.GONE);
        mLoadStatements.setVisibility(View.VISIBLE);

        mStatements = null;

        RequestsAdm.get(
                "https://bank-app-test.herokuapp.com/api/statements/" + userAccount.getIdusuario(),
                new RequestsAdm.RespostaRequisicao() {
                    @Override
                    public void respostaSucesso(String body) {
                        super.respostaSucesso(body);

                        System.out.println("RESPOSTA STATEMENTS: " + body);
                        mStatements = mountStatementsResponse(body);

                    }

                    @Override
                    public void fimDaThread() {
                        super.fimDaThread();

                        //Aqui chamamos a UIThread
                        //Apenas ela pode atualizar componetes de tela no Android
                        Utilidades.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //Testamos se o objeto userAccount é nulo, o que significaria que a requisição não trouxe uma resposta
                                if (mStatements == null) {

                                    //Exibindo mensagem e botão de tentar novamente
                                    mLlTryAgain.setVisibility(View.VISIBLE);
                                } else {

                                    //Garantindo que não seja exibido o componente de tentar novamente
                                    mLlTryAgain.setVisibility(View.GONE);

                                    //Requisição obteve resposta, a lista mStatements foi povoada
                                    mAdapter = new StatementsAdapter(PrincipalActivity.this, mStatements);
                                    mRecyclerView.setAdapter(mAdapter);

                                }

                                //Esconendo loader
                                mLoadStatements.setVisibility(View.GONE);


                            }
                        });
                    }
                }
        );

    }
    private List<br.com.entrevista.banco.Model.Statement> mountStatementsResponse(String body) {

        List<Statement> statements = new ArrayList<>();

        try {

            JSONArray statementList = new JSONObject(body).getJSONArray("statementList");

            for (int i = 0; i < statementList.length(); i++) {
                JSONObject statementObj = statementList.getJSONObject(i);

                Statement s = new Statement() {
                };
                s.setTitle(statementObj.getString("title"));
                s.setDesc(statementObj.getString("desc"));
                s.setDate(statementObj.getString("date"));
                s.setValue(statementObj.getDouble("value"));

                statements.add(s);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return statements;
    }

    public void showDialogLogout() {
        mDialogLogout = new Dialog(PrincipalActivity.this);
        mDialogLogout.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogLogout.setContentView(R.layout.dialog_logout);
        mDialogLogout.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Window window = mDialogLogout.getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        mDialogLogout.setCancelable(true);

        Button logout = (Button) mDialogLogout.findViewById(R.id.btn_dialog_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DBLocal bancoLocal = new DBLocal(PrincipalActivity.this);
                bancoLocal.cleanDb();

                startActivity(new Intent(PrincipalActivity.this, MainActivity.class));
                finish();
            }
        });

        Button cancel = (Button) mDialogLogout.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialogLogout.dismiss();
            }
        });
        mDialogLogout.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        mDialogLogout.show();
    }


}
