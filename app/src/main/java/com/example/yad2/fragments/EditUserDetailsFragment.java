package com.example.yad2.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.yad2.R;
import com.example.yad2.activities.RegisterActivity;
import com.example.yad2.models.Model;
import com.example.yad2.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

import lombok.SneakyThrows;

public class EditUserDetailsFragment extends Fragment {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Bitmap imageBitmap;
    String imageUrl, address, phoneNumber, firstName, lastName;
    private FloatingActionButton camBtn, galleryBtn;
    ImageView userImage;
    TextInputEditText editAddress, editPhone, editFirstName, editLastName;
    Button saveUpdates;
    TextInputLayout editAddressLayout;
    private ProgressBar progressBar;
    User currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user_details, container, false);

        camBtn = view.findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener(v -> openCam());
        galleryBtn = view.findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(v -> openGallery());
        progressBar = view.findViewById(R.id.edit_user_progress_bar);
        progressBar.setVisibility(View.GONE);

        userImage = view.findViewById(R.id.user_edit_image);
        editAddress = view.findViewById(R.id.user_edit_address);
        editPhone = view.findViewById(R.id.user_edit_phone);
        editFirstName = view.findViewById(R.id.user_edit_fname);
        editLastName = view.findViewById(R.id.user_edit_lname);

        saveUpdates = view.findViewById(R.id.save_updates);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            currentUser = (User) bundle.getSerializable("user");
        }

        editAddress.setText(currentUser.getAddress());
        editPhone.setText(currentUser.getPhoneNumber());
        editFirstName.setText(currentUser.getFirstName());
        editLastName.setText(currentUser.getLastName());


        if (currentUser.getUserImageUrl() != null) {
            Picasso.get()
                    .load(currentUser.getUserImageUrl())
                    .into(userImage);
        }


        saveUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = editAddress.getText().toString();
                String firstName = editFirstName.getText().toString();
                String lastName = editLastName.getText().toString();
                String phoneNumber = editPhone.getText().toString();

                Activity activity = getActivity();

                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(activity, "Please make sure all fields are filled", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    currentUser.setAddress(address);
                    currentUser.setFirstName(firstName);
                    currentUser.setLastName(lastName);
                    currentUser.setPhoneNumber(phoneNumber);
                    Bundle bundle = new Bundle();

                    if (imageBitmap == null) {
                        Model.instance.updateUser(currentUser, () -> {
                            bundle.putSerializable("user", currentUser);
                            view.clearFocus();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(activity, "user updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigate(R.id.to_nav_user_profile, bundle);
                        });
                    } else {
                        Model.instance.saveProductImage(imageBitmap, UUID.randomUUID() + ".jpg", url -> {
                            currentUser.setUserImageUrl(url);
                            bundle.putSerializable("user", currentUser);

                            view.clearFocus();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(activity, "user updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            Model.instance.updateUser(currentUser, () -> {
                                Navigation.findNavController(view).popBackStack();
                            });
                        });
                    }
                }
            }
        });

        return view;
    }


    private void openCam() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }


    @SneakyThrows
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                userImage.setImageBitmap(imageBitmap);
            }

            if (requestCode == REQUEST_GALLERY) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    userImage.setImageURI(selectedImageUri);
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), selectedImageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}