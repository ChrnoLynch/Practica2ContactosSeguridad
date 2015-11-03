package com.example.lprub.contactos.actividades;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.lprub.contactos.R;
import com.example.lprub.contactos.adaptadores.Adaptador;
import com.example.lprub.contactos.datos.Agenda;
import com.example.lprub.contactos.datos.Contacto;
import com.example.lprub.contactos.datos.GestionSeguridad;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ActividadPrincipal extends AppCompatActivity {
    private static final int ACTIVIDAD1=1, ACTIVIDAD2=2;
    private Agenda agenda;
    private Agenda aux;
    private Adaptador clAdaptador;
    private ListView lv;
    private Switch switch1;
    private TextView tvFecha;
    private GestionSeguridad copiaSeguridad;
    private SharedPreferences preSincro;
    private GregorianCalendar calendar;
    private SharedPreferences.Editor edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_principal);
        try {
            iniciar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        actualizarVista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actividad_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_añadir) {
//            add();
//            return true;
//        }
//        if (id == R.id.action_ordenarDes) {
//            agenda.ordenarAgenda();
//            clAdaptador.notifyDataSetChanged();
//            return true;
//        }
//        if (id == R.id.action_ordenarAsc) {
//            agenda.ordenarAgendaInverso();
//            clAdaptador.notifyDataSetChanged();
//            return true;
//        }
        if (id == R.id.setCopiaSeguridad) {
            try {
                sobrescribirTotal();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.getCopiaSeguridad) {
            try {
                leerTotal();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.sincronizar) {
            try {
                sincronizar();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return true;
        }
        if(id==R.id.sincronizarIncremental){
            try {
                sincronizarIncremental();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menucontextual, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Recogemos la información sobre que item es el que hemos seleccionado.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int indice = info.position;
        View v = info.targetView;
        //Aqui es donde segun que opcion del menu contextual hemos seleccionado, se realizara un metodo u otro.
        switch (item.getItemId()) {
            case R.id.mneditar:
                Intent intent=new Intent(this, ActividadEditcontact.class);
                Bundle bundle=new Bundle();
                bundle.putInt("pos", indice);
                bundle.putParcelable("contacto", agenda.get(indice));
                intent.putExtras(bundle);
                startActivityForResult(intent, ACTIVIDAD1);
                return true;
            case R.id.mnborrar:
                borrar(indice);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode== Activity.RESULT_OK && requestCode==ACTIVIDAD1) {
            Contacto aux=data.getParcelableExtra("contacto");
            agenda.setContacto(data.getIntExtra("pos",0),aux);
        }
        else if(resultCode== Activity.RESULT_OK&&requestCode==ACTIVIDAD2) {
            Contacto aux=data.getParcelableExtra("contacto");
            agenda.add(agenda.size(),aux);
        }
    }

    public void iniciar() throws IOException, XmlPullParserException {
        preSincro = getSharedPreferences("PREF", Context.MODE_PRIVATE);
        animacionSincronizacion();
        copiaSeguridad=new GestionSeguridad();
        aux = new Agenda(this);
        copiaSeguridad.crearFichero(this, "Total", aux.getAgenda());
        copiaSeguridad.crearFichero(this, "Incremental", aux.getAgenda());
        sincronizacionAutomatica();
        fecha();
    }

    //Metodo para llamar por telefono
    public void llamar(int posicion){
        String fono=agenda.getContacto(posicion).getNumero(0);
        Uri numero = Uri.parse( "tel:" + fono.toString() );
        Intent intent = new Intent(Intent.ACTION_CALL, numero);
        startActivity(intent);
    }
    //Metodo para llamar al Adaptador
    public void llamarAdaptador(Agenda agenda){
        lv = (ListView) findViewById(R.id.lvContactos);
        //Crear objeto adaptador y ponerselo a nuestro listview
        clAdaptador = new Adaptador(this, R.layout.itemlist, agenda);
        lv.setAdapter(clAdaptador);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int posicion, long id) {
                llamar(posicion);
            }
        });
        //Registrar el control LisrView para mostrar un menu contextual
        registerForContextMenu(lv);
    }

    public void borrar(final int indice) {
        String s=(this.getString(R.string.desea)+agenda.get(indice).getNombre()+"?");
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle(this.getString(R.string.dialogo_borrar));
        dialogo1.setMessage(s);
        dialogo1.setCancelable(false);
        dialogo1.setNegativeButton(this.getString(R.string.cancelar), null);
        dialogo1.setPositiveButton(this.getString(R.string.aceptar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                agenda.getAgenda().remove(indice);
                clAdaptador.notifyDataSetChanged();
            }
        });
        dialogo1.show();
    }

    public void actualizarVista(){
        clAdaptador.notifyDataSetChanged();
        agenda.ordenarAgenda();
    }

    public void add(){
        Intent intent= new Intent(this, ActividadAddContact.class);
        Bundle bundle=new Bundle();
        bundle.putInt("pos", agenda.size());
        intent.putExtras(bundle);
        startActivityForResult(intent, ACTIVIDAD2);
    }

    public void añadir(View v){
        add();
    }

    //Ordena la agenda
    public void ordenarAgenView(View v){
        agenda.ordenarAgenda();
        clAdaptador.notifyDataSetChanged();
    }
    //Ordena la agenda a la inversa
    public void ordenarAgenViewInver(View v){
        agenda.ordenarAgendaInverso();
        clAdaptador.notifyDataSetChanged();
    }
    //Guarda una copia de la agenda que se esta viendo el en programa en nuestro archivo xml
    // incremental
    public void guardarIncremental(View v) throws IOException {
        copiaSeguridad.copia(this, "Incremental", agenda.getAgenda());
    }
    //Lee y muestra en pantalla nuestra copia total de contactos
    public void leerTotal() throws IOException, XmlPullParserException {
        Agenda total=new Agenda(copiaSeguridad.leer(this, "Total"));
        llamarAdaptador(total);
        actualizarVista();
    }
    //Realiza otra copia de seguridad de los contactos de la agenda sobreescribiendo la anterior
    public void sobrescribirTotal() throws IOException {
        aux=new Agenda(this);
        copiaSeguridad.copia(this, "Total", aux.getAgenda());
    }
    //Muestra en pantalla la mezcla de nuestra copia de seguridad incremental y los contactos
    // del telefono
    public void sincronizar() throws IOException, XmlPullParserException {
        edit=preSincro.edit();
        calendar=new GregorianCalendar();
        edit.putString("TIME", calendar.getTime().toString());
        edit.commit();
        fecha();
        agenda=new Agenda(unionAgendas());
        llamarAdaptador(agenda);
        actualizarVista();
    }
    public void sincronizarIncremental() throws IOException, XmlPullParserException {
        List<Contacto> incremental=copiaSeguridad.leer(this,"Incremental");
        agenda=new Agenda(incremental);
        llamarAdaptador(agenda);
        actualizarVista();
    }
    //Este metodo coge los contactos de nuestra copia incremental y nuestros contactos del telefono
    //haciendo una mezcla entre ellas, de forma que se juntan todos los contactos con distinto nombre,
    //mientras que los que tienen el mismo nombre, suman sus numeros de telefono de ambas partes
    public List<Contacto> unionAgendas() throws IOException, XmlPullParserException {
        List<Contacto> telefono=aux.getAgenda(),
                incremental=copiaSeguridad.leer(this,"Incremental");
        List <String>x,y;
        ArrayList<Contacto> result=new ArrayList<>();
        result.addAll(telefono);

        for(int z=0;z<incremental.size();z++) {
            int cont1=0;
            for(int q=0;q<telefono.size();q++) {
                if(incremental.get(z).getNombre().compareTo(telefono.get(q).getNombre())==0){
                    x=telefono.get(q).getNumeros();
                    y=incremental.get(z).getNumeros();
                    for (int j = 0; j < y.size(); j++) {
                        int cont2 = 0;
                        for (int i = 0; i < x.size(); i++) {
                            if (x.get(i).compareTo(y.get(j))!=0) {
                                cont2++;
                            }
                        }
                        if (cont2>=x.size()) {
                            x.add(y.get(j));
                        }
                    }
                }
                else{
                    cont1++;
                }
            }
            if(cont1>=telefono.size()){
                result.add(incremental.get(z));
            }
        }
        return result;
    }
    //Metodo que pregunta si en la preferencia compartida de sincronizacion automatica se encuentra
    //un true o un false y actua en consecuencia
    public void sincronizacionAutomatica() throws IOException, XmlPullParserException {
        if (preSincro.getBoolean("AUTO", false)==true){
            sincronizar();
        }
        else{
            agenda = new Agenda(copiaSeguridad.leer(this,"Incremental"));
            llamarAdaptador(agenda);
        }
    }
    //Metodo para cambiar nuestra preferencia compartida de sincronizacion automatica y asi
    // indicar si queremos que la aplicacion se sincronize al iniciar la aplicacion o no
    public void autoEstado(View v){
        edit=preSincro.edit();
        if(preSincro.getBoolean("AUTO", false)==false){
            edit.putBoolean("AUTO",true);
            edit.commit();
        }else{
            edit.putBoolean("AUTO",false);
            edit.commit();
        }
        animacionSincronizacion();
    }
    public void animacionSincronizacion(){
        switch1= (Switch) this.findViewById(R.id.switch1);
        if(preSincro.getBoolean("AUTO",false)){
            switch1.setChecked(true);
        }
        else{
            switch1.setChecked(false);
        }
    }
    public void fecha(){
        tvFecha= (TextView) this.findViewById(R.id.tvFecha);
        if(preSincro.contains("TIME")) {
            tvFecha.setText(preSincro.getString("TIME", "Nunca Sincronizada"));
        }
        else{
            tvFecha.setText("Nunca Sincronizada");
        }

    }
}
