package com.cq.todolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity  { //implements View.OnClickListener

    //1.variable
    Button botonLogin;

    //5. HAcemos lo mismo con el boton de registro.
    TextView botonRegistro;

    //FIREBASE AUTHENTICATOR. Declaración de variable.
    private FirebaseAuth mAuth;
    //signUp
    EditText emailText, passText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //COn este método oculta la ActionBar
        getSupportActionBar().hide();



        //FIREBASE AUTHENTICATOR
        // Initialize Firebase Auth-variable.
        mAuth = FirebaseAuth.getInstance();
        //Creo la referencia de las cajas
        emailText = findViewById(R.id.cajaCorreo);
        passText = findViewById(R.id.cajaPass);




        //1.Funcionalidad al botón: priemro lo obtenemos y lo guardamos en una variable.
           //Que será un atributo de la clase.
        //2. Luego, aquí debajo le pasamos el identificador del botón en el Activity Login.
        botonLogin = findViewById(R.id.botonLogin);
        //3. Lo ponemos a la escucha con un listener. De param creamos un objeto de la interface
        //y sobreescribimos si método (Override)
        botonLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                //4. AL hacer click, pasamos a la MainActivity. Especofocamos que va
                //desde el objeto this de esta clase y pasa a la Main, mediante los 2 param

                //**FIREBASE AUTHENTICATOR-Login
                //Antes creo las variables para traer el contenido de las cajas. Según las variables de el código de Firebase.
                String email = emailText.getText().toString();
                String password = passText.getText().toString();

                //VALIDACIONES
                if (email.isEmpty()) {
                    emailText.setError("Campo obligatorio");
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //Expresiones regulares o PATTERNS propio de AS que ya vienen
                    //preconfigurados, mejor opción.
                    emailText.setError("Correo incorrecto");

                }else if(password.isEmpty()){
                    passText.setError("Campo obligatorio");
                }else if(password.length() < 6){
                    passText.setError("Mínimo 6 caracteres");

                }else{
                    //Tras realizar todas las validaciones
                    //**FIREBASE AUTHENTICATOR-Login
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information


                                        Intent intent = new Intent(Login.this, MainActivity.class);


                                    /*
                                    Toast Comunicación entre activities
                                    No ha sido necesario la implementación del Interface ni sobreescribir el método
                                    Ya que tenemos el método onComplete.
                                    */
                                        EditText nombreUsuario = findViewById(R.id.cajaCorreo);
                                        String cadena = nombreUsuario.getText().toString();
                                        String[] partes = cadena.split("@");
                                        String nombre = partes[0];
                                        intent.putExtra("nombre", nombre);


                                        startActivity(intent);

                                    } else {
                                        //Un toast para avisar que las credenciales son incorrectas
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this , "Authentication failed.",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }






            }
        });

        /*

        //LOGIN HECHO CON LAMBDA.

        botonLogin.setOnclickListener(view -> {
            //Loguear Usuario en Firebase

                    String email = emailText.getText().toString();
                    String password = passText.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        //Sign in success
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                    }else{
                                        //If sign in fails, displau a message to the user.
                                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                });
         */



        //6.Referencia de Registro y su funcionalidad.
        botonRegistro = findViewById(R.id.botonRegistro);
        botonRegistro.setOnClickListener(new View.OnClickListener() { //Esto se pod´ria hacer con fn lambda.
            @Override
            public void onClick(View v) {

                //7(no hecho en ese orden). FIREBASE AUTHENTICATOR: Se crea el usuario -REGISTRO- en la BBDD
                //Ya he creado las referencias para traer el contenido de las cajas en onCreate
                //Ahora copio el código que me da Firebase. Modifiico para adecuarlo a las variables.
                //Cuando damos click en el botón, se crea el usuario
                //Antes creo las variables para traer el contenido de las cajas. Según las variables de el código de Firebase.
                String email = emailText.getText().toString();
                String password = passText.getText().toString();




                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {


                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information

                                    //8. Generamos el Toast. POr defecto se muestra abajo. Se puede cambiar, añadirle
                                    //iconos, imágenes,etc.
                                    //Esto antes de implementar estos métodos estaba fuera, ahora lo trasladamos
                                    Toast.makeText(Login.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(Login.this, "Authentication failed.",Toast.LENGTH_SHORT).show();

                                }
                            }
                        });




            }
        });
        //SUBRAYADO: Esto lo añado yo. no S EPUEDE HACER POR xml.
        botonRegistro.setPaintFlags(botonRegistro.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);



    }


}