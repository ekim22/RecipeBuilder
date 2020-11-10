package com.sdp.recipebuilder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.sdp.recipebuilder.model.Recipe
import com.sdp.recipebuilder.model.Step
import com.sdp.recipebuilder.util.RecipeUtil
import java.util.*


class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private var TAG = MainActivity::class.qualifiedName
    private var db: FirebaseFirestore? = null
//    private var docRef: DocumentReference = db?.collection("MyRecipes")!!.document()
    private var speechRecognizer: SpeechRecognizer? = null
    private var editText: EditText? = null
    private var micButton: ImageView? = null
    private var items: ArrayList<Step>? = null
    private var itemsAdapter: ArrayAdapter<Step>? = null
    private var recipeList: ListView? = null

    private var myRecipeList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        myRecipeList = findViewById<View>(R.id.recipeRecyclerView) as RecyclerView

        recipeList = findViewById<View>(R.id.recipeSteps) as ListView
        items = ArrayList()
        itemsAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, items!!)
        recipeList!!.adapter = itemsAdapter

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        initFirestore()
//        writeToDb()
        readFromDb()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }
        editText = findViewById(R.id.etNewStep)
        micButton = findViewById(R.id.mic)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {
                editText!!.setText("")
                editText!!.setHint("Listening...")
            }

            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {
                editText!!.setHint("Enter a recipe step.")
                micButton!!.setImageResource(R.drawable.ic_mic_black_off)
            }

            override fun onError(i: Int) {
                editText!!.setError("Error: $i")
//                editText!!.setText("ERROR CODE: " + i)
                micButton!!.setImageResource(R.drawable.ic_mic_black_off)
            }

            override fun onResults(bundle: Bundle) {
                micButton!!.setImageResource(R.drawable.ic_mic_black_off)
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, data.toString())
                editText!!.setText(data!![0])
                val myView = findViewById<View>(R.id.btnAddStep)
                myView.performClick()
            }

            override fun onPartialResults(bundle: Bundle) {
                editText!!.setHint("PARTIAL RESULTS")
                micButton!!.setImageResource(R.drawable.ic_mic_black_off)
            }

            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        setupMicListener(speechRecognizerIntent)
        setupListViewListener()
        setupFireStoreListener()
        this.intent?.handleIntent()
    }

    private fun initFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMicListener(speechRecognizerIntent: Intent) {
        micButton!!.setOnTouchListener(OnTouchListener { view, motionEvent ->

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "started listening")
                micButton!!.setImageResource(R.drawable.ic_mic_black_on)
                speechRecognizer!!.startListening(speechRecognizerIntent)
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "stopped listening")
                speechRecognizer!!.stopListening()
            }
            false
        })
    }

    private fun setupListViewListener() {
        recipeList!!.onItemLongClickListener = OnItemLongClickListener { adapter, item, pos, id -> // Remove the item within array at position
            val newRecipe = Step(items!![pos].id, items!![pos].step)
            // Remove from ListView
            items!!.removeAt(pos)
            // Delete from Firestore
            deleteStep(newRecipe)
            // Refresh the adapter
            itemsAdapter!!.notifyDataSetChanged()
            // Return true consumes the long click event (marks it handled)
            true
        }
    }

    fun onAddItem(v: View?) {
        val etNewItem = findViewById<View>(R.id.etNewStep) as EditText
        val itemText = etNewItem.text.toString()
        val newRecipe = Step(stepCounter.toString(), itemText)
        stepCounter++
        if (itemText != "") {
            itemsAdapter!!.add(newRecipe)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etNewItem.windowToken, 0)
        }
        addStep(v, newRecipe)
        etNewItem.setText("")
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer!!.destroy()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RecordAudioRequestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        val RC_SIGN_IN = 1001
        const val RecordAudioRequestCode = 1
        var stepCounter = 1
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "Made it to onNewIntent...")
        super.onNewIntent(intent)
        intent?.handleIntent()
    }

    private fun Intent.handleIntent() {
        Log.d(TAG, "logging handleIntent...")
        when (action) {
            // When the action is triggered by a deep-link, Intent.ACTION_VIEW will be used
            Intent.ACTION_VIEW -> handleDeepLink(data)

            else -> {
                Log.d(TAG, "got to else in handleIntent...")
            }
        }
    }

    private fun handleDeepLink(data: Uri?) {
        Log.d(TAG, "logging in handleDeepLink...")
        when (data?.path) {
            DeepLink.START -> {
                Log.d(TAG, "logging in START...")
                val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                micButton!!.setImageResource(R.drawable.ic_mic_black_on)
                speechRecognizer!!.startListening(speechRecognizerIntent)
            }
            DeepLink.STOP -> {
                Log.d(TAG, "logging in STOP...")
                stopService(Intent(this, MainActivity::class.java))
            }
            else -> {
                Log.d(TAG, "logging in ELSE...")
            }
        }
    }

    private fun readFromDb() {
        db!!.collection("recipes")
                .get()
                .addOnSuccessListener { documents ->
                    var docs = documents.documents
                    for (doc in docs) {
                        Log.d(TAG, "readFromDb: " + doc.data)
                    }
                }

    }

    private fun addStep(v: View?, step: Step) {
        val step = hashMapOf(
                step.id to step.step,
                "uid" to FirebaseAuth.getInstance().currentUser!!.uid
        )

        db?.collection("recipes")!!.document()
                .set(step, SetOptions.merge())
                .addOnSuccessListener { Log.d("ADD", "Step successfully written to Firestore") }
                .addOnFailureListener { e -> Log.w("ADD", "Error writing recipe step", e) }

    }

    private fun deleteStep(step: Step) {
        val step = hashMapOf<String, Any>(
                step.id to FieldValue.delete()
        )

        db?.collection("recipes")!!.document()
                .update(step)
                .addOnCompleteListener { Log.d("DEL", "$step step successfully deleted from Firestore") }
                .addOnFailureListener { e -> Log.w("DEL", "Error deleting recipe step", e) }
    }

    private fun writeToDb() {
        var recipes: CollectionReference = db?.collection("recipes")!!

        val steps = hashMapOf(
                "1" to "In a small bowl, combine the sugars, flour and spices; set aside.",
                "2" to "In a large bowl, toss apples with lemon juice. Add sugar mixture; toss to coat.",
                "3" to "Line a 9-in. pie plate with bottom crust; trim even with edge.",
                "4" to "Fill with apple mixture; dot with butter.",
                "5" to "Roll remaining crust to fit top of pie; place over filling. Trim, seal and flute edges. Cut slits in crust.",
                "6" to "Beat egg white until foamy; brush over crust. Sprinkle with sugar. Cover edges loosely with foil.",
                "7" to "Bake at 375° for 25 minutes. Remove foil and bake until crust is golden brown and filling is bubbly, 20-25 minutes longer. Cool on a wire rack.",
        )
        val ingredients = arrayListOf(
                "sugar", "apples", "milk", "flour", "water", "cinnamon"
        )

        val newRecipe = Recipe(FirebaseAuth.getInstance().currentUser!!.uid, "ApplePie", steps, ingredients,
                "An apple pie is a pie in which the principal filling ingredient is apple," +
                        " originated in England. It is often served with whipped cream, ice cream, or cheddar cheese.")


        recipes.add(newRecipe)

//        db?.collection("MyRecipes")!!.document(docRef.id)
//                .set(newRecipe)
//                .addOnSuccessListener { Log.d(TAG, "Step successfully written to Firestore") }
//                .addOnFailureListener { e -> Log.w(TAG, "Error writing recipe step", e)}
    }

    private fun setupFireStoreListener() {
        var docRef: DocumentReference = db?.collection("recipes")!!.document()

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                itemsAdapter!!.notifyDataSetChanged()
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

    }

    // The two methods below add three-dot menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_signout -> {
                AuthUI.getInstance().signOut(this)
            }
            R.id.action_profile -> {
                // TODO: Tie to profile page
            }
            R.id.action_add_random_recipes -> {
                onAddRecipesClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    private fun shouldStartSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        // Start sign in with LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, RC_SIGN_IN)
        finish()
    }

    private fun startLoginActivity() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onAddRecipesClicked() {
        val recipes = db!!.collection("recipes")

        for (x in 0..10) {
            val recipe = RecipeUtil.getRandom(this)
            recipe.uid = FirebaseAuth.getInstance().currentUser!!.uid
            Log.d(TAG, "onAddRecipesClicked: " + recipe.uid)
            recipes.add(recipe)
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        } else {
            FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
                    .addOnSuccessListener { getTokenResult ->
                        Log.d(TAG, "onSuccess: " + getTokenResult!!.token)
                    }
        }
    }

}