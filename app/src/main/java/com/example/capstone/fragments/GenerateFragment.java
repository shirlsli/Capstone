package com.example.capstone.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.capstone.BitmapScaler;
import com.example.capstone.CallbackListener;
import com.example.capstone.R;
import com.example.capstone.SearchAdapter;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class GenerateFragment extends Fragment implements SearchAdapter.EventListener, CallbackListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private String poemLine;
    private ImageView ivForwardArrow;
    protected SearchAdapter adapter;
    protected List<String> allGeneratedLines;
    private RecyclerView rvGeneratedLines;
    private LottieAnimationView lottieAnimationView;
    private LottieAnimationView lottieAnimationViewConfused;
    private ArrayList<String> generatedLines;
    private boolean activateTutorial = false;
    private User user;
    private String url;
    private ArrayList<String> wordsInPrompt;
    private CallbackListener listener;
    private ArrayList<String> results = new ArrayList<>();
    private static final String TAG = "GenerateFragment";
    private ImageView ivCamera;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;


    public GenerateFragment() {
        // Required empty public constructor
    }

    public static GenerateFragment newInstance(String param1, String param2) {
        GenerateFragment fragment = new GenerateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }
    // allow user to choose to make their poem line public
    // add friend's username to their poem line in edit poem screen
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            activateTutorial = bundle.getBoolean("activateTutorial");
        }
        etUserInput = view.findViewById(R.id.etUserInput);
        etUserInput.setVisibility(View.VISIBLE);
        lottieAnimationView = view.findViewById(R.id.lottieLoad);
        lottieAnimationViewConfused = view.findViewById(R.id.lottieLoadConfused);
        rvGeneratedLines = view.findViewById(R.id.rvGeneratedLines);
        ivForwardArrow = view.findViewById(R.id.ivForwardArrow);
        ivCamera = view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        wordsInPrompt = new ArrayList<>();
        if (activateTutorial) {
            tutorial();
        }
        ivForwardArrow.setOnClickListener(new View.OnClickListener() {
            // need to identify if the word is a real word
            @Override
            public void onClick(View v) {
                try {
                    generatePrompts();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void tutorial() {
        new GuideView.Builder(getContext())
                .setTitle("Give us a prompt!")
                .setContentText("Enter a word, phrase, or sentence.")
                .setTargetView(etUserInput)
                .setDismissType(DismissType.targetView)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        new GuideView.Builder(getContext())
                                .setTitle("Generate and proceed!")
                                .setContentText("After you generate a poem line, \n" +
                                        "tap the forward arrow to proceed.")
                                .setTargetView(ivForwardArrow)
                                .setDismissType(DismissType.outside)
                                .build()
                                .show();
                    }
                })
                .build()
                .show();
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
                // by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 500);
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    // Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                InputImage image = InputImage.fromBitmap(takenImage, 270);

                ImageLabelerOptions options =
                        new ImageLabelerOptions.Builder()
                                .setConfidenceThreshold(0.8f)
                                .build();
                ImageLabeler labeler = ImageLabeling.getClient(options);
                labeler.process(image)
                        .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                            @Override
                            public void onSuccess(List<ImageLabel> labels) {
                                Log.i(TAG, "Successfully labeled photo");
                                if (labels.size() > 0) {
                                    beginGeneration(labels.get(0).getText());
                                    callOpenAI(labels.get(0).getText());
                                } else {
                                    rvGeneratedLines.setVisibility(View.GONE);
                                    invalidUserInput();
                                }
                                for (int i = 0; i < labels.size(); i++) {
                                    Log.i(TAG, "Labels: " + labels.get(i).getText());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to label photo", e);
                                invalidUserInput();
                            }
                        });
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFileUri(String photoFileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "storePhotos");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "Failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + photoFileName);
        return file;
    }

    private void beginGeneration(String prompt) {
        etUserInput.setText(prompt);
        hideSoftKeyboard(getActivity());
        if (generatedLines != null) {
            adapter.clear();
            generatedLines = null;
        }
        lottieAnimationViewConfused.setVisibility(View.GONE);
        lottieAnimationView.setVisibility(View.VISIBLE);
        ivForwardArrow.setVisibility(View.GONE);
    }

    private String dictionaryEntries(String input) {
        final String language = "en-gb";
        final String fields = "";
        final String strictMatch = "false";
        final String word_id = input.toLowerCase();
        return "https://od-api.oxforddictionaries.com:443/api/v2/entries/" + language + "/" + word_id + "?" + "fields=" + fields + "&strictMatch=" + strictMatch;
    }

    private void invalidUserInput() {
        lottieAnimationView.setVisibility(View.GONE);
        lottieAnimationViewConfused.setVisibility(View.VISIBLE);
        showErrorMessage();
        ivForwardArrow.setVisibility(View.VISIBLE);
        wordsInPrompt.clear();
    }

    @Override
    public void onCallbackEvent(String result) {
        results.add(result);
        if (results.size() == wordsInPrompt.size()) {
            for (int i = 0; i < results.size(); i++) { // some legit words are not included in dictionary
                if (results.get(i).equals("https://od-api.oxforddictionaries.com:443/api/v2/entries/en-gb/" + wordsInPrompt.get(i) + "?fields=&strictMatch=false")) {
                    invalidUserInput();
                    return;
                }
            }
            String prompt = "";
            for (int i = 0; i < wordsInPrompt.size(); i++) {
                prompt = prompt + wordsInPrompt.get(i) + " ";
            }
            callOpenAI(prompt);
            wordsInPrompt.clear();
        }
    }

    public void generatePrompts() throws InterruptedException {
        String prompt = etUserInput.getText().toString();
        if (!prompt.contains(" ")) {
            wordsInPrompt.add(prompt);
        } else {
            wordsInPrompt.addAll(Arrays.asList(prompt.split(" ")));
        }
        if (wordsInPrompt.size() > 0) {
            for (int i = 0; i < wordsInPrompt.size(); i++) {
                if (wordsInPrompt.get(i).contains("\"[^a-zA-Z0-9]\"")) {
                    invalidUserInput();
                    wordsInPrompt.clear();
                    return;
                } else {
                    beginGeneration(prompt);
                    DictionaryRequest dictionaryRequest = new DictionaryRequest(this);
                    url = dictionaryEntries(wordsInPrompt.get(i));
                    dictionaryRequest.execute(url);
                }
            }
        } else {
            invalidUserInput();
        }
    }

    private void callOpenAI(String prompt) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                OpenAIThread openAIThread = new OpenAIThread(prompt);
                openAIThread.runCallback(new Runnable() {
                    @Override
                    public void run() {
                        generatedLines = openAIThread.getGeneratedLines();
                        allGeneratedLines = generatedLines.subList(1, generatedLines.size());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter != null) {
                                    adapter.clear();
                                    adapter.addAll(allGeneratedLines);
                                } else {
                                    displayPoemLines(allGeneratedLines);
                                }
                                connectAdapterToRecyclerView();
                            }
                        });
                    }
                });
            }
        });
    }

    private void connectAdapterToRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
        rvGeneratedLines.setLayoutManager(linearLayoutManager);
        rvGeneratedLines.setAdapter(adapter);
        lottieAnimationView.setVisibility(View.GONE);
        rvGeneratedLines.setVisibility(View.VISIBLE);
        ivForwardArrow.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        Toast.makeText(getActivity(), "Sorry, couldn't quite understand your input!",
                Toast.LENGTH_LONG).show();
        etUserInput.setText("");
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void displayPoemLines(List<String> generatedLines) {
        adapter = new SearchAdapter(getView().getContext(), generatedLines, this);
    }

    public void createPoemLine(String line, ArrayList<String> generatedLines) {
        try {
            String prompt = etUserInput.getText().toString();
            etUserInput.setText(line);
            ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
            currentUserQuery.include(User.KEY_FRIENDS);
            currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
            currentUserQuery.getFirstInBackground(new GetCallback<User>() {
                @Override
                public void done(User currentUser, ParseException e) {
                    user = currentUser;
                }
            });
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // goes to Create Poem Fragment
                    bundleToCreatePoem(prompt, line);
                }
            });
            Log.i(TAG, "poem line creation success! " + poemLine);
        } catch (Exception exception) {
            Log.e(TAG, "poem line creation failed :(", exception);
        }
    }

    private void bundleToCreatePoem(String prompt, String line) {
        ArrayList<String> poemLines = new ArrayList<>();
        Fragment createPoemFragment = new CreatePoemFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("Poem", poemLines);
        bundle.putString("Line", line);
        bundle.putString("Prompt", prompt);
        bundle.putParcelable("user", user);
        bundle.putStringArrayList("GeneratedLines", generatedLines);
        bundle.putBoolean("activateTutorial", activateTutorial);
        createPoemFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.flContainer, createPoemFragment).addToBackStack( "generate_poem" ).commit();
    }

    @Override
    public void onEvent(ArrayList<String> data) {
        createPoemLine(data.get(0), new ArrayList<String>(allGeneratedLines));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

}
