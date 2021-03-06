package com.ferrariapps.instagram.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.ferrariapps.instagram.R;
import com.ferrariapps.instagram.activity.PerfilAmigoActivity;
import com.ferrariapps.instagram.adapter.AdapterPesquisa;
import com.ferrariapps.instagram.helper.ConfiguracaoFirebase;
import com.ferrariapps.instagram.helper.RecyclerItemClickListener;
import com.ferrariapps.instagram.helper.UsuarioFirebase;
import com.ferrariapps.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PesquisaFragment extends Fragment {

    private androidx.appcompat.widget.SearchView searchViewPesquisa;
    private RecyclerView recyclerPesquisa;
    private List<Usuario> listaUsuarios;
    private DatabaseReference usuariosRef;
    private AdapterPesquisa adapterPesquisa;
    private String idUsuarioLogado;

    public PesquisaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        searchViewPesquisa = view.findViewById(R.id.searchViewPesquisa);
        recyclerPesquisa = view.findViewById(R.id.recyclerPesquisa);

        listaUsuarios = new ArrayList<>();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        recyclerPesquisa.setHasFixedSize(true);
        recyclerPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterPesquisa = new AdapterPesquisa(listaUsuarios,getActivity());
        recyclerPesquisa.setAdapter(adapterPesquisa);

        recyclerPesquisa.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerPesquisa,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent intent = new Intent(getActivity(), PerfilAmigoActivity.class);
                        intent.putExtra("usuarioSelecionado",usuarioSelecionado);
                        startActivity(intent);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        searchViewPesquisa.setQueryHint("Buscar usuários");

        searchViewPesquisa.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toUpperCase();
                pesquisarUsuarios(textoDigitado);
                return true;
            }
        });

        return view;
    }

    private void pesquisarUsuarios(String textoDigitado) {

        listaUsuarios.clear();

        if (textoDigitado.length() > 0){
            Query query = usuariosRef.orderByChild("nomeUpper")
                    .startAt(textoDigitado)
                    .endAt(textoDigitado+"\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    listaUsuarios.clear();

                    for (DataSnapshot ds : snapshot.getChildren()){

                        Usuario usuario = ds.getValue(Usuario.class);
                        assert usuario != null;
                        if (idUsuarioLogado.equals(usuario.getId()))
                            continue;

                        listaUsuarios.add(ds.getValue(Usuario.class));
                    }

                    adapterPesquisa.notifyDataSetChanged();

                   // int total = listaUsuarios.size();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}