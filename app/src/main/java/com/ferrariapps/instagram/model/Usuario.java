package com.ferrariapps.instagram.model;

import android.content.Context;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ferrariapps.instagram.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String id;
    private String nome;
    private String nomeUpper;
    private String email;
    private String senha;
    private String caminhoFoto;
    private int seguidores = 0;
    private int seguindo = 0;
    private int postagens = 0;

    public Usuario() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());
        usuariosRef.setValue(this);

    }

    public void atualizarQtdPostagem(Context context){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(getId());
        HashMap<String, Object> dados = new HashMap<>();
        dados.put("postagens",getPostagens());
        usuarioRef.updateChildren(dados).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(context, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Ocorreu algum problema ao atualizar os dados!", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    public void atualizar(Context context){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        HashMap<String, Object> objeto = new HashMap<>();
        objeto.put("/usuarios/"+getId()+"/nome",getNome());
        objeto.put("/usuarios/"+getId()+"/nomeUpper",getNomeUpper());
        objeto.put("/usuarios/"+getId()+"/caminhoFoto",getCaminhoFoto());
        firebaseRef.updateChildren(objeto);
    }

    public Map<String, Object> converterParaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email",getEmail());
        usuarioMap.put("nome",getNome());
        if (getNomeUpper() != null)
        usuarioMap.put("nomeUpper",getNomeUpper());
        usuarioMap.put("id",getId());
        usuarioMap.put("caminhoFoto",getCaminhoFoto());
        if (getSeguidores() != 0)
        usuarioMap.put("seguidores",getSeguidores());
        if (getSeguindo() != 0)
        usuarioMap.put("seguindo",getSeguindo());
        if(getPostagens() != 0)
        usuarioMap.put("postagens",getPostagens());

        return usuarioMap;

    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public int getSeguindo() {
        return seguindo;
    }

    public void setSeguindo(int seguindo) {
        this.seguindo = seguindo;
    }

    public int getPostagens() {
        return postagens;
    }

    public void setPostagens(int postagens) {
        this.postagens = postagens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeUpper() {
        return nomeUpper;
    }

    public void setNomeUpper(String nomeUpper) {
        this.nomeUpper = nomeUpper;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
