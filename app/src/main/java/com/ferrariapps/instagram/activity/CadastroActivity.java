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
import com.ferrariapps.instagram.helper.UsuarioFirebase;
import com.ferrariapps.instagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private EditText editCadastroNome, editCadastroEmail, editCadastroSenha;
    private Button buttonCadastrar;
    private ProgressBar progressCadastro;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoNome = editCadastroNome.getText().toString();
                String textoNomeUpper = editCadastroNome.getText().toString().toUpperCase();
                String textoEmail = editCadastroEmail.getText().toString();
                String textoSenha = editCadastroSenha.getText().toString();

                if (!textoNome.isEmpty()){

                    if(!textoEmail.isEmpty()){

                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();

                            usuario.setNome(textoNome);
                            usuario.setNomeUpper(textoNomeUpper);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);

                            cadastrar(usuario);

                        }else{
                            Toast.makeText(CadastroActivity.this,
                                    "Preencha sua senha!", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(CadastroActivity.this,
                                "Preencha seu email!", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(CadastroActivity.this,
                            "Preencha seu nome!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void cadastrar(Usuario usuario){

        progressCadastro.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    try {
                        progressCadastro.setVisibility(View.GONE);

                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome(),getApplicationContext());

                        Toast.makeText(CadastroActivity.this,
                                "Cadastro efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{

                    progressCadastro.setVisibility(View.GONE);

                    String erroExcecao="";
                    try {
                        throw Objects.requireNonNull(task.getException());
                    }catch (FirebaseAuthWeakPasswordException e){
                        erroExcecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erroExcecao = "Por favor, digite um e-mail válido!";
                    }catch (FirebaseAuthUserCollisionException e){
                        erroExcecao = "Este e-mail já está em uso!";
                    }catch (Exception e){
                        erroExcecao = "Erro ao cadastrar usuário: "+e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,
                            erroExcecao, Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    public void inicializarComponentes() {
        editCadastroNome = findViewById(R.id.editCadastroNome);
        editCadastroEmail = findViewById(R.id.editCadastroEmail);
        editCadastroSenha = findViewById(R.id.editCadastroSenha);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        progressCadastro = findViewById(R.id.progressCadastro);
        editCadastroNome.requestFocus();
    }
}