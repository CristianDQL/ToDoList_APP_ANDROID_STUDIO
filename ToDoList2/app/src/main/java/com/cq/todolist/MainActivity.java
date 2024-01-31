package com.cq.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //FIREBASE AUTHENTICATOR. logout
    private FirebaseAuth mAuth;

    //FIREBASE ADD DATA. variable para guardar la referencia a la BBDD.
    private FirebaseFirestore db;
    //Identificador de usuario
    private String idUser;
    //Acceso al ListView para actualizar la interfaz con el listado de tareas.
    ListView listViewTareas;
    //Adapter para rellenar el ListView
    ArrayAdapter<String> adapterTareas;
    //2 listas: una para las tareas y otra para el adptador de tareas
    // para poder recuperarlas de la BBDD. EN paralero.
    List<String> listaTareas = new ArrayList<>();
    List<String> listaIdTareas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIREBASE AUTHENTICATOR **LogOut
        // Initialize Firebase Auth-variable.
        mAuth = FirebaseAuth.getInstance();

        //FIREBASE ADD DATA. inicializamoslas variables.
        idUser = mAuth.getCurrentUser().getUid(); //ID or Email, cualquier identificador.
        db = FirebaseFirestore.getInstance();

        listViewTareas = findViewById(R.id.listTareas);


        //********Comunicación entre activities (meter en método)
        String nombre = getIntent().getStringExtra("nombre");
        //TextView etiquetaNomUser = findViewById(R.id.cajaCorreo);
        if (nombre != null && !nombre.isEmpty()) {
            Toast.makeText(MainActivity.this, "Hola " + nombre, Toast.LENGTH_SHORT).show();
        }



        //Método para actualizar el listView con los datos de la BBDD
        actualizarUI();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // añadir elementos a la barra si esta está presente
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //Como dos ítem van a llamar a este método, los diferenciamos por el identificador.

        if(item.getItemId() == R.id.mas){
            //acción activar el cuadro de diálogo para añadir tarea
            //EditText: Como el cuadro de diáolo va a conterner un VIew, hay que configurar primero ese View
            final EditText taskEditText = new EditText(this);
            //AletDialog ahora configuramos el cuadro de diálogo
            //EN la siguiente construcción, nótese que no se cierra la primera sentencia con ; debido a
            //que vamos a continuar en ese mismo código el resto de sentencias. Para no tener que hacer siemre
            //Un dialog.algo, no se cierra con ; y se realizan los .algo a continuación.
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nueva tarea")
                    .setMessage("¿Qué quieres hacer a continuación?")
                    .setView(taskEditText)
                    //los dos botones, el positive y el negative + listeners. Sobreescribimos el métood de positive.
                    .setPositiveButton("añadir", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    //ADD DATA: añadir tarea a la base de datos.
                                                    //Creo primero las variables. Las obtengo de la view del cuadro de dialogo creada más arriba.
                                                    String miTarea = taskEditText.getText().toString();
                                                    // Add a new document with a generated id.
                                                    Map<String, Object> data = new HashMap<>(); //El documento
                                                    data.put("nombreTarea", miTarea);
                                                    data.put("usuario", idUser);

                                                    db.collection("Tareas")
                                                            .add(data)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    //Toast trasladado aquí
                                                                    Toast.makeText(MainActivity.this,"Tarea añadida", Toast.LENGTH_SHORT).show();

                                                                    return;
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    //Toast
                                                                    Toast.makeText(MainActivity.this,"Fallo al crear la tarea", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                     }
                    })
                    .setNegativeButton("cancelar", null)
                    .create();
            dialog.show();



            //Toast-DE momento se elimnia,se traslada hacia arriba, parece que funciona.
            //Toast.makeText(this, "Tarea añadida", Toast.LENGTH_SHORT).show();

            return true;
        }else if (item.getItemId() == R.id.logout){
            //**LogOut: acción llamar a Firebase para hacer el cierre de sesión en FIrebase y volver a la activity de Login.
            mAuth.signOut();
            //Vuelta al login
            startActivity(new Intent(MainActivity.this, Login.class));
            finish(); //finalizo la main

            return true;
        }else return super.onOptionsItemSelected(item);
        }

        //Método para actualizar el lsitView con los datos de la BBDD
        private void actualizarUI(){
            db.collection("Tareas")
                    //query según la clave y el valor. EL valor lo hemos almacenado en idUser anteriormente
                    .whereEqualTo("usuario", idUser)
                    //Con este listener, siempre que hay cambios en el elemento escuchado, se ejecuta.
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {

                                return;
                            }

                            //Vamos limpiando las listas, para que no vengan con datos. Y cada vez que hay cambios, se limpia y se rellena
                            //para evitar duplicidad.
                            listaTareas.clear();
                            listaIdTareas.clear();

                            for (QueryDocumentSnapshot doc : value) {
                               listaTareas.add(doc.getString("nombreTarea"));
                               listaIdTareas.add(doc.getId());
                            }
                            //Adapter: para rellenar el listView.
                            //Compruebo si no tiene nada y lo pongo en null
                            if(listaTareas.size() == 0){
                                listViewTareas.setAdapter(null);
                            }else{
                                //La main lo relleno con ->LAYOUT donde está el elemento->VIEW del texto->RELLENO CON ESTOS DATOS de la lista
                                adapterTareas = new ArrayAdapter<>(MainActivity.this,
                                        R.layout.item_tarea,
                                        R.id.textViewTarea,
                                        listaTareas);
                                //Con el adapter se rellena el listView
                                listViewTareas.setAdapter(adapterTareas);
                            }



                        }
                    });
        }

        //BORRAR TAREA + Añadir en XML android:onClick="borrarTarea"
        public void borrarTarea(View view){ //El view es el botón clickeado, que ya está a la escucha desde el XML
            View parent = (View) view.getParent(); //del view=botón obtenemos el padre
            //Mediante el padre consigo el hijo, es decir el TextView= textViewTarea
            // Ella lo llama "nombreTarea" cuidado por si da error
            TextView tareaTextView = parent.findViewById(R.id.textViewTarea);
            //variable para mostrar el contenido de la caja.
            String tarea = tareaTextView.getText().toString();

            //AHora mediante los dos ArrayList paralelos que creamos antes, mediante
            //la posición de listaTareas, obtendremos la posición de su Id en el
            //otro Array. SOn paralelos=misma posición. Le pasamos la tarea=posiicón
            int posicion = listaTareas.indexOf(tarea);

            //Borrado de datos en la BBDD
            //mediante la BBDD accedemos a la colección y al documentos, mediante
            //el identificador de tareas, en la posición que será la misma que hemos
            //guardado antes para el array parelelo de listaTareas.
            db.collection("Tareas").document(listaIdTareas.get(posicion)).delete();
            Toast.makeText(MainActivity.this,"Tarea eliminadad correctamente", Toast.LENGTH_SHORT).show();

            //Añadir en el botón-XML el código: android:onClick="borrarTarea"

        }

    //ACTUALIZAR TAREA
    public void updateTarea(View view){
        View parent = (View) view.getParent();

        //recoge el texto modificado
        TextView tareaTextView = parent.findViewById(R.id.textViewTarea);
        String tarea = tareaTextView.getText().toString();

        //posición de la tarea en el array listaTareas
        int posicion = listaTareas.indexOf(tarea);

        //Alert para el cuadro de diálogo donde se modifican los datos
        //Hay que meterle un dialog, pero en lugar de estar vacío debería
        //tener el texto a editar.

        //Esta es la caja de texto para el alert
        EditText taskEditText = new EditText(this);
        //dentro le meto el mensaje a modificar que he cogido antes.
        taskEditText.setText(tarea);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Modificar tarea")
                .setMessage("¿Cómo quieres modificar la tarea?")
                //.setMessage(tarea) así mostraría el texto editado como un mensaje fijo
                .setView(taskEditText)
                //los dos botones, el positive y el negative + listeners. Sobreescribimos el métood de positive.
                .setPositiveButton("modificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //Creo primero las variables. Las obtengo de la view del cuadro de dialogo creada más arriba.
                        //Esta variable obtiene la caja de texto, ya modificada
                        String miTarea = taskEditText.getText().toString();


                        Map<String, Object> data = new HashMap<>(); //El documento
                        data.put("nombreTarea", miTarea);
                        data.put("usuario", idUser);


                        //.set() actualiza todo el documento y .update() solo algunos campos.
                        db.collection("Tareas").document(listaIdTareas.get(posicion)).update(data)

                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast trasladado aquí
                                        Toast.makeText(MainActivity.this,"Tarea modificada correctamente", Toast.LENGTH_SHORT).show();

                                        return;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Toast
                                        Toast.makeText(MainActivity.this,"Fallo al modificar la tarea", Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .setNegativeButton("cancelar", null)
                .create();
        dialog.show();

        //Añadir en el botón-XML el código: android:onClick="updateTarea"

    }


}