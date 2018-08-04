package com.example.tarun.apna_cafe_server;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tarun.apna_cafe_server.Common.Common;
import com.example.tarun.apna_cafe_server.Interface.ItemClickListener;
import com.example.tarun.apna_cafe_server.Model.Category;
import com.example.tarun.apna_cafe_server.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    MaterialEditText edtNAme;
    Button btnUpload , btnSelect;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference categories;

    FirebaseStorage storage;
    StorageReference storageReference;

    Category newCategory;

    Uri saveUri;



    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_home );
        Toolbar toolbar = (Toolbar) findViewById ( R.id.toolbar );
        toolbar.setTitle ( "Menu Management" );
        setSupportActionBar ( toolbar );

        //Init FIREBASE


        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance ();
        storageReference = storage.getReference ();

        //View


        //Add Menu Layout


        FloatingActionButton fab = (FloatingActionButton) findViewById ( R.id.fab );
        fab.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View view ) {

                showDialog();
            }
        } );

        DrawerLayout drawer = (DrawerLayout) findViewById ( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle (
                this , drawer , toolbar , R.string.navigation_drawer_open , R.string.navigation_drawer_close );
        drawer.addDrawerListener ( toggle );
        toggle.syncState ( );

        NavigationView navigationView = (NavigationView) findViewById ( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener ( this );

        //Set Name for USERS
        View headerView = navigationView.getHeaderView ( 0 );
        txtFullName=(TextView)headerView.findViewById ( R.id.txtFullName );
        txtFullName.setText ( Common.currentUser.getName () );

        //Init View
        recycler_menu = (RecyclerView)findViewById ( R.id.recycler_menu );
        recycler_menu.setHasFixedSize ( true );
        layoutManager = new LinearLayoutManager ( this );
        recycler_menu.setLayoutManager ( layoutManager );

        loadMenu();

    }

    private void showDialog () {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder ( Home.this );
        alertDialog.setTitle ( "Add new Category" );
        alertDialog.setMessage ( "Please fill full Information ..." );

        LayoutInflater inflater = this.getLayoutInflater ();
        View add_menu_layout = inflater.inflate ( R.layout.add_new_menu_layout,null );

        edtNAme = add_menu_layout.findViewById ( R.id.edtName );
        btnSelect = add_menu_layout.findViewById ( R.id.btnSelect );
        btnUpload = add_menu_layout.findViewById ( R.id.btnUpload );

        //Event for Button
        btnSelect.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                chooseImage();  //user select image from gallery and save URI of this image
            }
        } );

        btnUpload.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                uploadImage();
            }
        } );

        alertDialog.setView ( add_menu_layout );
        alertDialog.setIcon ( R.drawable.ic_shopping_cart_black_24dp );

        //set Button
        alertDialog.setPositiveButton ( "Yes" , new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick ( DialogInterface dialog , int which ) {

                dialog.dismiss ();

                if(newCategory != null)
                {
                    categories.push ().setValue ( newCategory );
                    Toast.makeText ( Home.this , "New category added successfully" , Toast.LENGTH_SHORT ).show ( );
                }
            }
        } );
        alertDialog.setNegativeButton ( "No" , new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick ( DialogInterface dialog , int which ) {

                dialog.dismiss ();
            }
        } );

        alertDialog.show ();
    }

    private void uploadImage () {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog ( this );
            mDialog.setMessage ( "Uploading ...." );
            mDialog.show ();

            String imageName = UUID.randomUUID ().toString ();
            final StorageReference imageFolder = storageReference.child ( "image/"+imageName );
            imageFolder.putFile ( saveUri ).addOnSuccessListener ( new OnSuccessListener <UploadTask.TaskSnapshot> ( ) {
                @Override
                public void onSuccess ( UploadTask.TaskSnapshot taskSnapshot ) {
                    mDialog.dismiss ();
                    Toast.makeText ( Home.this , "Uploaded  !!" , Toast.LENGTH_SHORT ).show ( );
                    imageFolder.getDownloadUrl ().addOnSuccessListener ( new OnSuccessListener <Uri> ( ) {
                        @Override
                        public void onSuccess ( Uri uri ) {
                            //set value for new category , if image upload done and we can get download link

                            newCategory = new Category ( edtNAme.getText ().toString (),uri.toString () );
                        }
                    } );

                }
            } )
                    .addOnFailureListener ( new OnFailureListener ( ) {
                        @Override
                        public void onFailure ( @NonNull Exception e ) {
                            mDialog.dismiss ();
                            Toast.makeText ( Home.this , ""+e.getMessage () , Toast.LENGTH_SHORT ).show ( );
                        }
                    } )
                    .addOnProgressListener ( new OnProgressListener <UploadTask.TaskSnapshot> ( ) {
                        @Override
                        public void onProgress ( UploadTask.TaskSnapshot taskSnapshot ) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred () / taskSnapshot.getTotalByteCount ());
                            mDialog.setMessage ( "Uploaded "+progress+"%" );
                        }
                    } );
        }
    }

    @Override
    protected void onActivityResult ( int requestCode , int resultCode , Intent data ) {
        super.onActivityResult ( requestCode , resultCode , data );

        if(requestCode == Common.Image_pick_request && resultCode == RESULT_OK && data != null && data.getData () != null)
        {
            saveUri = data.getData ();
            btnSelect.setText ( "Image Selected !" );
        }
    }

    private void chooseImage () {
        Intent intent = new Intent (  );
        intent.setType ( "image/*" );
        intent.setAction ( Intent.ACTION_GET_CONTENT );
        startActivityForResult ( Intent.createChooser ( intent,"Select Picture" ),Common.Image_pick_request );
    }

    private void loadMenu () {
        adapter = new FirebaseRecyclerAdapter <Category, MenuViewHolder> (
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories
        ) {
            @Override
            protected void populateViewHolder ( MenuViewHolder viewHolder , Category model , int position ) {

                viewHolder.txtMenuName.setText ( model.getName () );
                Picasso.with ( Home.this ).load ( model.getImage () ).into ( viewHolder.imageView );

                viewHolder.setItemClickListener ( new ItemClickListener ( ) {
                    @Override
                    public void onClick ( View view , int position , boolean isLongClick ) {
                        //send database category id and start new activity

                        Intent foodList = new Intent ( Home.this,FoodList.class );
                        foodList.putExtra ( "CategoryId",adapter.getRef ( position ).getKey () );
                        startActivity ( foodList );

                    }
                } );
            }
        };

        adapter.notifyDataSetChanged ();//Refresh data if have data changed
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed () {
        DrawerLayout drawer = (DrawerLayout) findViewById ( R.id.drawer_layout );
        if (drawer.isDrawerOpen ( GravityCompat.START )) {
            drawer.closeDrawer ( GravityCompat.START );
        } else {
            super.onBackPressed ( );
        }
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ( ).inflate ( R.menu.home , menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId ( );

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected ( item );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected ( MenuItem item ) {
        // Handle navigation view item clicks here.
        int id = item.getItemId ( );

         if(id == R.id.nav_orders)
         {
             Intent orders = new Intent ( Home.this , orderStatus.class );
             startActivity ( orders );
         }


        DrawerLayout drawer = (DrawerLayout) findViewById ( R.id.drawer_layout );
        drawer.closeDrawer ( GravityCompat.START );
        return true;
    }

    //Update     //Delete


    @Override
    public boolean onContextItemSelected ( MenuItem item ) {

        if(item.getTitle ().equals ( Common.UPDATE ))
        {
            showUpdateDialog(adapter.getRef ( item.getOrder () ).getKey (),adapter.getItem ( item.getOrder () ));
        }
        else if(item.getTitle ().equals ( Common.DELETE ))
        {
            deleteCategory(adapter.getRef ( item.getOrder () ).getKey ());
        }


        return super.onContextItemSelected ( item );
    }

    private void deleteCategory ( String key ) {
        categories.child ( key ).removeValue ();
        Toast.makeText ( this , "Item Deleted !!!!!" , Toast.LENGTH_SHORT ).show ( );
    }

    private void showUpdateDialog ( final String key , final Category item ) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder ( Home.this );
        alertDialog.setTitle ( "Update Category" );
        alertDialog.setMessage ( "Please fill full Information ..." );

        final LayoutInflater inflater = this.getLayoutInflater ();
        View add_menu_layout = inflater.inflate ( R.layout.add_new_menu_layout,null );

        edtNAme = add_menu_layout.findViewById ( R.id.edtName );
        btnSelect = add_menu_layout.findViewById ( R.id.btnSelect );
        btnUpload = add_menu_layout.findViewById ( R.id.btnUpload );

        //Set Default Name
        edtNAme.setText ( item.getName () );

        //Event for Button
        btnSelect.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                chooseImage();  //user select image from gallery and save URI of this image
            }
        } );

        btnUpload.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                changeImage(item);
            }
        } );

        alertDialog.setView ( add_menu_layout );
        alertDialog.setIcon ( R.drawable.ic_shopping_cart_black_24dp );

        //set Button
        alertDialog.setPositiveButton ( "Yes" , new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick ( DialogInterface dialog , int which ) {

                dialog.dismiss ();

               item.setName ( edtNAme.getText ().toString () );
               categories.child ( key ).setValue ( item );
            }
        } );
        alertDialog.setNegativeButton ( "No" , new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick ( DialogInterface dialog , int which ) {

                dialog.dismiss ();
            }
        } );

        alertDialog.show ();
    }


    private void changeImage ( final Category item) {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog ( this );
            mDialog.setMessage ( "Uploading ...." );
            mDialog.show ();

            String imageName = UUID.randomUUID ().toString ();
            final StorageReference imageFolder = storageReference.child ( "image/"+imageName );
            imageFolder.putFile ( saveUri ).addOnSuccessListener ( new OnSuccessListener <UploadTask.TaskSnapshot> ( ) {
                @Override
                public void onSuccess ( UploadTask.TaskSnapshot taskSnapshot ) {
                    mDialog.dismiss ();
                    Toast.makeText ( Home.this , "Uploaded  !!" , Toast.LENGTH_SHORT ).show ( );
                    imageFolder.getDownloadUrl ().addOnSuccessListener ( new OnSuccessListener <Uri> ( ) {
                        @Override
                        public void onSuccess ( Uri uri ) {
                            //set value for new category , if image upload done and we can get download link
                            item.setImage ( uri.toString () );
                        }
                    } );

                }
            } )
                    .addOnFailureListener ( new OnFailureListener ( ) {
                        @Override
                        public void onFailure ( @NonNull Exception e ) {
                            mDialog.dismiss ();
                            Toast.makeText ( Home.this , ""+e.getMessage () , Toast.LENGTH_SHORT ).show ( );
                        }
                    } )
                    .addOnProgressListener ( new OnProgressListener <UploadTask.TaskSnapshot> ( ) {
                        @Override
                        public void onProgress ( UploadTask.TaskSnapshot taskSnapshot ) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred () / taskSnapshot.getTotalByteCount ());
                            mDialog.setMessage ( "Uploaded "+progress+"%" );
                        }
                    } );
        }
    }
}
