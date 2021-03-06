package com.ferrariapps.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ferrariapps.instagram.R;
import com.ferrariapps.instagram.helper.ConfiguracaoFirebase;
import com.ferrariapps.instagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginActivity extends AppCompatActivity {

    private EditText editLoginEmail, editLoginSenha;
    private Button buttonEntrar;
    private ProgressBar progressLogin;

    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        verificarUsuarioLogado();
        inicializarComponentes();

        buttonEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoEmail = editLoginEmail.getText().toString();
                String textoSenha = editLoginSenha.getText().toString();

                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);
                            validarLogin(usuario);

                        }else{
                            Toast.makeText(LoginActivity.this,
                                    "Preencha sua senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(LoginActivity.this,
                                "Preencha seu email!", Toast.LENGTH_SHORT).show();
                    }
            }
        });

    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
    }

    public void validarLogin(Usuario usuario) {

        progressLogin.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressLogin.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this,
                            "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }else {

                    progressLogin.setVisibility(View.GONE);

                    String erroExcecao="";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erroExcecao = "A senha não coincide com o usuário informado!";
                    }catch (FirebaseAuthInvalidUserException e){
                        erroExcecao = "Este usuário não está cadastrado!";
                    }catch (FirebaseAuthRecentLoginRequiredException e){
                        erroExcecao = "Este usuário já está logado!";
                    }catch (Exception e){
                        erroExcecao = "Erro ao fazer o login: "+e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,
                            erroExcecao, Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    public void inicializarComponentes() {
        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginSenha = findViewById(R.id.editLoginSenha);
        buttonEntrar = findViewById(R.id.buttonEntrar);
        progressLogin = findViewById(R.id.progressLogin);
        editLoginEmail.requestFocus();
    }

    public void abrirCadastro(View view){
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

}