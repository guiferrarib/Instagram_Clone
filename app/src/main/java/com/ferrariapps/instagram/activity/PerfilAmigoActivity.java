package com.ferrariapps.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ferrariapps.instagram.R;
import com.ferrariapps.instagram.adapter.AdapterGrid;
import com.ferrariapps.instagram.helper.ConfiguracaoFirebase;
import com.ferrariapps.instagram.helper.UsuarioFirebase;
import com.ferrariapps.instagram.model.Postagem;
import com.ferrariapps.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonPerfil;
    private CircleImageView imagePerfil;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference postagensUsuarioRef;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerPerfilAmigos;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;
    private String idUsuarioLogado;
    private List<Postagem> postagens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        inicializarComponentes();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            usuarioSelecionado =(Usuario) bundle.getSerializable("usuarioSelecionado");
            postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
            if (caminhoFoto != null && !caminhoFoto.equals("")){

                Uri url = Uri.parse(caminhoFoto);
                Glide.with(PerfilAmigoActivity.this)
                        .asBitmap()
                        .load(url)
                        .into(imagePerfil);

            }

        }

        inicializarImageLoader();

        carregarFotosPostagem();

        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Postagem postagem = postagens.get(position);
                Intent intent = new Intent(getApplicationContext(), VisualizarPostagemActivity.class);
                intent.putExtra("postagem",postagem);
                intent.putExtra("usuario",usuarioSelecionado);
                startActivity(intent);
            }
        });

    }

    private void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

    }

    private void carregarFotosPostagem(){

        postagens = new ArrayList<>();
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid/3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    postagens.add(postagem);
                    assert postagem != null;
                    urlFotos.add(postagem.getCaminhoFoto());
                }

                adapterGrid = new AdapterGrid(getApplicationContext(),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioLogado = snapshot.getValue(Usuario.class);
                verificaSegueUsuarioAmigo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void verificaSegueUsuarioAmigo(){
        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);

        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            habilitarBotaoSeguir(true);
                        }else {
                            habilitarBotaoSeguir(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void  habilitarBotaoSeguir(boolean segueUsuario){

        if (segueUsuario){
            buttonPerfil.setText("Seguindo");
        }else{
            buttonPerfil.setText("Seguir");

            buttonPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarSeguidor(usuarioLogado,usuarioSelecionado);
                }
            });
        }

    }

    private void salvarSeguidor(Usuario userLogado, Usuario useroAmigo){

        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome",userLogado.getNome());
        dadosUsuarioLogado.put("caminhoFoto",userLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef
                .child(useroAmigo.getId())
                .child(userLogado.getId());
        seguidorRef.setValue(dadosUsuarioLogado);

        buttonPerfil.setText("Seguindo");
        buttonPerfil.setOnClickListener(null);
        
        int seguindo = userLogado.getSeguindo()+1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef
                .child(userLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        int seguidores = useroAmigo.getSeguidores()+1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef
                .child(useroAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);

    }

    @Override
    protected void onResume() {
        super.onResume();
        recuperarDadosPerfilAmigo();
        recuperarDadosUsuarioLogado();
    }

    @Override
    protected void onPause() {
        super.onPause();
        usuarioAmigosRef.removeEventListener(valueEventListenerPerfilAmigos);
    }

    private void recuperarDadosPerfilAmigo(){

        usuarioAmigosRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigos = usuarioAmigosRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Usuario usuario = snapshot.getValue(Usuario.class);
                        assert usuario != null;
                        String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        textPublicacoes.setText(postagens);
                        textSeguidores.setText(seguidores);
                        textSeguindo.setText(seguindo);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void inicializarComponentes() {
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        imagePerfil = findViewById(R.id.imagePerfil);
        buttonPerfil = findViewById(R.id.buttonPerfil);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
        buttonPerfil.setText("Carregando");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}