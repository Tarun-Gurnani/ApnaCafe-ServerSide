package com.example.tarun.apna_cafe_server;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tarun.apna_cafe_server.Common.Common;
import com.example.tarun.apna_cafe_server.Interface.ItemClickListener;
import com.example.tarun.apna_cafe_server.Model.Category;
import com.example.tarun.apna_cafe_server.Model.Food;
import com.example.tarun.apna_cafe_server.ViewHolder.FoodViewHolder;
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

import static com.example.tarun.apna_cafe_server.Common.Common.Image_pick_request;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //Add new food
    MaterialEditText edtName,edtDescription,edtPrice,edtDiscount;
    Button btnSelect,btnUpload;

    Food newFood;

    Uri saveUri;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_food_list );

        //Fireabase
        db=FirebaseDatabase.getInstance ();
        foodList=db.getReference ("Foods");
        storage=FirebaseStorage.getInstance ();
        storageReference = storage.getReference ();

        //Init
        recyclerView=(RecyclerView)findViewById ( R.id.recycler_food );
        recyclerView.setHasFixedSize ( true );
        layoutManager= new LinearLayoutManager ( this );
        recyclerView.setLayoutManager ( layoutManager );

        //Fab button
        fab =(FloatingActionButton)findViewById ( R.id.fab );
        fab.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {

                showAddFoodDialog();
            }
        } );

        if(getIntent () != null)
        {
            categoryId=getIntent ().getStringExtra ( "CategoryId" );
        }
        if(!categoryId.isEmpty ())
        {
            loadListFood(categoryId);
        }
    }

    private void showAddFoodDialog () {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder ( FoodList.this );
            alertDialog.setTitle ( "Add new Food" );
            alertDialog.setMessage ( "Please fill full Information ..." );

            LayoutInflater inflater = this.getLayoutInflater ();
            View add_menu_layout = inflater.inflate ( R.layout.add_new_food_layout,null );

            edtName = add_menu_layout.findViewById ( R.id.edtName );
              edtDescription = add_menu_layout.findViewById ( R.id.edtDescription );
              edtPrice = add_menu_layout.findViewById ( R.id.edtPrice );
              edtDiscount = add_menu_layout.findViewById ( R.id.edtDiscount );

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

                    if(newFood != null)
                    {
                        foodList.push ().setValue ( newFood );
                        Toast.makeText ( FoodList.this , "New category added successfully" , Toast.LENGTH_SHORT ).show ( );
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
            imageFolder.putFile ( saveUri ).addOnSuccessListener ( new OnSuccessListener<UploadTask.TaskSnapshot> ( ) {
                @Override
                public void onSuccess ( UploadTask.TaskSnapshot taskSnapshot ) {
                    mDialog.dismiss ();
                    Toast.makeText ( FoodList.this , "Uploaded  !!" , Toast.LENGTH_SHORT ).show ( );
                    imageFolder.getDownloadUrl ().addOnSuccessListener ( new OnSuccessListener <Uri> ( ) {
                        @Override
                        public void onSuccess ( Uri uri ) {
                            //set value for new category , if image upload done and we can get download link

                            newFood = new Food (  );
                            newFood.setName ( edtName.getText ().toString () );
                            newFood.setDescription ( edtDescription.getText ().toString () );
                            newFood.setPrice ( edtPrice.getText ().toString () );
                            newFood.setDiscount ( edtDiscount.getText ().toString () );
                            newFood.setMenuId ( categoryId );
                            newFood.setImage ( uri.toString () );
                        }
                    } );

                }
            } )
                    .addOnFailureListener ( new OnFailureListener ( ) {
                        @Override
                        public void onFailure ( @NonNull Exception e ) {
                            mDialog.dismiss ();
                            Toast.makeText ( FoodList.this , ""+e.getMessage () , Toast.LENGTH_SHORT ).show ( );
                        }
                    } )
                    .addOnProgressListener ( new OnProgressListener<UploadTask.TaskSnapshot> ( ) {
                        @Override
                        public void onProgress ( UploadTask.TaskSnapshot taskSnapshot ) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred () / taskSnapshot.getTotalByteCount ());
                            mDialog.setMessage ( "Uploaded "+progress+"%" );
                        }
                    } );
        }
    }


    private void chooseImage () {
        Intent intent = new Intent (  );
        intent.setType ( "image/*" );
        intent.setAction ( Intent.ACTION_GET_CONTENT );
        startActivityForResult ( Intent.createChooser ( intent,"Select Picture" ), Common.Image_pick_request );
    }

    private void loadListFood ( String categoryId ) {

        adapter = new FirebaseRecyclerAdapter <Food, FoodViewHolder> (
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild ( "menuId" ).equalTo ( categoryId )
        ) {
            @Override
            protected void populateViewHolder ( FoodViewHolder viewHolder , Food model , int position ) {
                viewHolder.food_name.setText ( model.getName () );
                Picasso.with ( getBaseContext () ).load ( model.getImage () ).into ( viewHolder.food_image );

                viewHolder.setItemClickListener ( new ItemClickListener ( ) {
                    @Override
                    public void onClick ( View view , int position , boolean isLongClick ) {
                        //codeLate
                    }
                } );
            }
        };
        adapter.notifyDataSetChanged ();
        recyclerView.setAdapter ( adapter );
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

    @Override
    public boolean onContextItemSelected ( MenuItem item ) {

        if(item.getTitle ().equals ( Common.UPDATE ))
        {
            showUpdateFoodDialog(adapter.getRef ( item.getOrder () ).getKey (),adapter.getItem ( item.getOrder () ));
        }
        else if(item.getTitle ().equals ( Common.DELETE ))
        {
            deleteFood(adapter.getRef ( item.getOrder () ).getKey ());
        }
        return super.onContextItemSelected ( item );
    }

    private void deleteFood ( String key ) {
        foodList.child ( key ).removeValue ();
    }

    private void showUpdateFoodDialog ( final String key , final Food item ) {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder ( FoodList.this );
            alertDialog.setTitle ( "Edit Food" );
            alertDialog.setMessage ( "Please fill full Information ..." );

            LayoutInflater inflater = this.getLayoutInflater ();
            View add_menu_layout = inflater.inflate ( R.layout.add_new_food_layout,null );

            edtName = add_menu_layout.findViewById ( R.id.edtName );
            edtDescription = add_menu_layout.findViewById ( R.id.edtDescription );
            edtPrice = add_menu_layout.findViewById ( R.id.edtPrice );
            edtDiscount = add_menu_layout.findViewById ( R.id.edtDiscount );

            //Set default value
            edtName.setText ( item.getName () );
            edtDiscount.setText ( item.getDiscount () );
            edtPrice.setText ( item.getPrice () );
            edtDescription.setText ( item.getDescription () );

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


                        //update info
                        item.setName (edtName.getText ().toString ());
                        item.setPrice (edtPrice.getText ().toString ());
                        item.setDiscount (edtDiscount.getText ().toString ());
                        item.setDescription (edtDescription.getText ().toString ());


                        foodList.child ( key ).setValue ( item );
                        Toast.makeText ( FoodList.this , " Category was edited successfully" , Toast.LENGTH_SHORT ).show ( );

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


    private void changeImage ( final Food item) {
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
                    Toast.makeText ( FoodList.this , "Uploaded  !!" , Toast.LENGTH_SHORT ).show ( );
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
                            Toast.makeText ( FoodList.this , ""+e.getMessage () , Toast.LENGTH_SHORT ).show ( );
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

