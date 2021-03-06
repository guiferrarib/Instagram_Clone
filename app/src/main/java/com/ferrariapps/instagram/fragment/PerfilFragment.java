package com.ferrariapps.instagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ferrariapps.instagram.R;
import com.ferrariapps.instagram.activity.EditarPerfilActivity;
import com.ferrariapps.instagram.activity.VisualizarPostagemActivity;
import com.ferrariapps.instagram.adapter.AdapterGrid;
import com.ferrariapps.instagram.helper.ConfiguracaoFirebase;
import com.ferrariapps.instagram.helper.UsuarioFirebase;
import com.ferrariapps.instagram.model.Postagem;
import com.ferrariapps.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    private GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonPerfil;
    private Usuario usuarioLogado;
    private List<Postagem> postagens;

    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference postagensUsuarioRef;
    private AdapterGrid adapterGrid;
    private ValueEventListener valueEventListenerPerfil;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");

        postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

        inicializarComponentes(view);

        buttonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditarPerfilActivity.class));
            }
        });

        inicializarImageLoader();

        carregarFotosPostagem();

        return view;
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

                adapterGrid = new AdapterGrid(Objects.requireNonNull(getActivity()),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Postagem postagem = postagens.get(position);
                Intent intent = new Intent(getActivity(), VisualizarPostagemActivity.class);
                intent.putExtra("postagem",postagem);
                intent.putExtra("usuario",usuarioLogado);
                startActivity(intent);
            }
        });

    }

    private void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(Objects.requireNonNull(getActivity()))
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

    }

    private void inicializarComponentes(View view) {
        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        progressBar = view.findViewById(R.id.progressBarPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonPerfil = view.findViewById(R.id.buttonPerfil);
    }


    private void recuperarDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(
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

    private void recuperarFotoUsuario(){
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null && !caminhoFoto.equals("")){

            Uri url = Uri.parse(caminhoFoto);
            Glide.with(Objects.requireNonNull(getActivity()))
                    .asBitmap()
                    .load(url)
                    .into(imagePerfil);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        recuperarDadosUsuarioLogado();
        recuperarFotoUsuario();
    }

    @Override
    public void onPause() {
        super.onPause();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }
}