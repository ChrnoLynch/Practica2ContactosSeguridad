package com.example.lprub.contactos.actividades;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lprub.contactos.R;
import com.example.lprub.contactos.datos.Agenda;
import com.example.lprub.contactos.datos.Contacto;

import java.util.ArrayList;

/**
 * Created by lprub on 18/10/2015.
 */
public class ActividadAddContact extends AppCompatActivity {
    private EditText etNombre, etNumero1, etNumero2, etNumero3, etNumero4;
    private Contacto aux=new Contacto();
    private Intent i;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formulariocontac);
         i=this.getIntent();
        Bundle bundle=i.getExtras();
//        id=(long)bundle.getInt("pos");
        id=8;
        System.out.println("***** "+id);
        etNombre=(EditText)this.findViewById(R.id.etNombre);
        etNumero1=(EditText)this.findViewById(R.id.etNumero1);
        etNumero2=(EditText)this.findViewById(R.id.etNumero2);
        etNumero3=(EditText)this.findViewById(R.id.etNumero3);
        etNumero4=(EditText)this.findViewById(R.id.etNumero4);
    }

    //En el caso de cancelar, solo se cerrara la actividad y no haremos ningun cambio
    public void cancelar(View v){
        finish();
    }
    //En el caso de aceptar crearemos un contacto y lo rellenamos con los datos introducios en el
    //formulario y lo añadimos en nuestra Agenda
    public void aceptar(View v){
        if (etNombre.length()>0||etNumero1.length()>0){
            aux.setId(id);
            aux.setNombre(etNombre.getText().toString());

            ArrayList<String> num=new ArrayList();
            num.add(etNumero1.getText().toString());

            if(etNumero2.getText().length()>0){
                num.add(etNumero2.getText().toString());
            }
            if(etNumero3.getText().length()>0){
                num.add(etNumero3.getText().toString());
            }
            if(etNumero4.getText().length()>0){
                num.add(etNumero4.getText().toString());
            }
            aux.setNumeros(num);
            Bundle b=new Bundle();
            b.putParcelable("contacto",aux);
            i.putExtras(b);
            setResult(Activity.RESULT_OK, i);
            finish();
        }else{
            //En caso de que uno de los campos importantes este vacio, mostramos una tostada indicandolo.
            Toast t=new Toast(this);
            t.makeText(this, this.getString(R.string.campo_vacio), Toast.LENGTH_LONG).show();
        }

    }
}
