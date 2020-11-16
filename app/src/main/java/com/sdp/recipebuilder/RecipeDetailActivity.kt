package com.sdp.recipebuilder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.sdp.recipebuilder.adapter.RecipeAdapter
import com.sdp.recipebuilder.adapter.ViewPagerAdapter
import com.sdp.recipebuilder.model.Recipe
import java.util.*
import kotlin.collections.ArrayList

class RecipeDetailActivity : AppCompatActivity(), EventListener<DocumentSnapshot> {
    private var mRecipeName: TextView? = null
    private var mRecipeDesc: TextView? = null
    private var mRecipePic: ImageView? = null
    private var mRecipeSteps: HashMap<String, String>? = null
    private var mRecipeIngredients: ArrayList<String>? = null
    private var mRecipeRegistration: ListenerRegistration? = null
    private var speechRecognizer: SpeechRecognizer? = null

    var isRotate = false
    var btnAnimation = ButtonAnimation()
    var tabConfigured = false

    private lateinit var db: FirebaseFirestore
    private lateinit var mRecipeRef: DocumentReference
    private lateinit var mRecipeAdapter: RecipeAdapter

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var pagerAdapter: ViewPagerAdapter? = null



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Get recipe ID from extras
        val recipeId = intent.extras!!.getString(KEY_RECIPE_ID)
                ?: throw IllegalArgumentException("Must pass extra $KEY_RECIPE_ID")
        Log.d(TAG, "onCreate: $recipeId")

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        mRecipeRef = db.collection("recipes").document(recipeId)


        mRecipeName = findViewById(R.id.recipe_name)
        openDialog(mRecipeName!!)
        mRecipeDesc = findViewById(R.id.recipe_description)
        mRecipeDesc!!.movementMethod = ScrollingMovementMethod()
        openDialog(mRecipeDesc!!)
        mRecipePic = findViewById(R.id.recipe_image)

        // Get steps
        val stepsQuery = mRecipeRef.get().addOnSuccessListener { document ->

        }

//        val recipeRecyclerView = findViewById<View>(R.id.recipeRecyclerView)
//        configureTabLayout()

        // Steps
        val steps = hashMapOf<String, String>()
        // Ingredients
        val ingredients = ArrayList<String>()

        val addBtn = findViewById<View>(R.id.fabAdd)
        val micBtn = findViewById<View>(R.id.fabMic)
        val writeBtn = findViewById<View>(R.id.fabWrite)

        btnAnimation.init(micBtn)
        btnAnimation.init(writeBtn)

        addBtn.setOnClickListener { v ->
            isRotate = btnAnimation.rotateFab(v, !isRotate)
            if (isRotate) {
                btnAnimation.showIn(micBtn)
                btnAnimation.showIn(writeBtn)
            } else {
                btnAnimation.showOut(micBtn)
                btnAnimation.showOut(writeBtn)
            }
        }

        writeBtn.setOnClickListener {
            if (viewPager!!.currentItem == 0) {
                val builder = AlertDialog.Builder(this)
                val inflater = layoutInflater
                builder.setTitle("Step")
                val dialogLayout = inflater.inflate(R.layout.dialog_item, null)
                val editRecipeItemText = dialogLayout.findViewById<EditText>(R.id.edit_item)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Done") { dialogInterface, i ->
                    mRecipeSteps?.set(mRecipeSteps!!.size.toString(), editRecipeItemText.text.toString())
                    mRecipeRef.update("steps", mRecipeSteps!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "onCreate: Step successfully added")
                            }
                            .addOnFailureListener { Log.w(TAG, "Error writing step") }
                }
                builder.setNegativeButton("Cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                builder.show()
            } else if (viewPager!!.currentItem == 1) {
                val builder = AlertDialog.Builder(this)
                val inflater = layoutInflater
                builder.setTitle("Ingredient")
                val dialogLayout = inflater.inflate(R.layout.dialog_item, null)
                val editRecipeItemText = dialogLayout.findViewById<EditText>(R.id.edit_item)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Done") { dialogInterface, i ->
                    mRecipeIngredients?.add(editRecipeItemText.text.toString())
                    mRecipeRef.update("ingredients", mRecipeIngredients!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "onCreate: Ingredient successfully added")
                            }
                            .addOnFailureListener { Log.w(TAG, "Error writing ingredient") }
                }
                builder.setNegativeButton("Cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                builder.show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {

            }

            override fun onError(i: Int) {

            }

            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, data.toString())
                Log.d(TAG, "onResults: currentItem " + viewPager!!.currentItem)
                if (viewPager!!.currentItem == 0) {
                    Log.d(TAG, "onResults: in STEPS")
                    mRecipeSteps?.set(mRecipeSteps!!.size.toString(), data?.get(0).toString())
                    mRecipeRef.update("steps", mRecipeSteps!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "onCreate: Step successfully added")
                            }
                            .addOnFailureListener { Log.w(TAG, "Error writing step") }
                } else if (viewPager!!.currentItem == 1) {
                    Log.d(TAG, "onResults: in INGREDIENTS")
                    mRecipeIngredients?.add(data?.get(0).toString())
                    mRecipeRef.update("ingredients", mRecipeIngredients!!)
                            .addOnSuccessListener {
                                Log.d(TAG, "onCreate: Ingredient successfully added")
                            }
                            .addOnFailureListener { Log.w(TAG, "Error writing ingredient") }
                }
            }

            override fun onPartialResults(bundle: Bundle) {

            }

            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        setupMicListener(speechRecognizerIntent, micBtn as FloatingActionButton)

    }

    companion object {
        private const val TAG = "RecipeDetail"
        const val KEY_RECIPE_ID = "key_recipe_id"
    }

    override fun onStart() {
        super.onStart()
        mRecipeRegistration = mRecipeRef.addSnapshotListener(this)
    }

    override fun onStop() {
        super.onStop()
        if (mRecipeRegistration != null) {
            mRecipeRegistration?.remove()
            mRecipeRegistration = null
        }
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        error?.let {
            Log.w(TAG, "recipe:onEvent", it)
            return
        }
        Log.d(TAG, "onEvent: " + value!!.toString())
        Log.d(TAG, "onEvent: " + value!!.toObject(Recipe::class.java).toString())
        onRecipeLoaded(value!!.toObject(Recipe::class.java))
    }

    private fun onRecipeLoaded(recipe: Recipe?) {
        mRecipeName?.text = recipe?.title
        mRecipeDesc?.text = recipe?.description
        // Background image
        if (recipe!!.pic != "") {
            Glide.with(mRecipePic!!.context)
                    .load(recipe.pic)
                    .into(mRecipePic!!)
        }
        mRecipeSteps = recipe.steps
        mRecipeIngredients = recipe.ingredients
        configureTabLayout()
//        setTabWriteBtnListener()
    }

    private fun configureTabLayout() {
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setTabTextColors(Color.BLACK, Color.WHITE)

        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)

        mRecipeSteps?.let { StepFragment(it) }?.let { pagerAdapter!!.addFragment(it, "Steps") }
        mRecipeIngredients?.let { IngredientFragment(it) }?.let { pagerAdapter!!.addFragment(it, "Ingredients") }

        viewPager!!.adapter = pagerAdapter
        tabLayout!!.setupWithViewPager(viewPager)
    }

    private fun setTabWriteBtnListener() {
        viewPager!!.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d(TAG, "onTabSelected: changing tabs detected. selected: " + tab.text)
                viewPager!!.currentItem = tab.position
//                if (tab.text == "Steps") {
//                    writeBtn.setOnClickListener {
//                        Log.d(TAG, "onTabSelected: STEPS")
//                        Toast.makeText(this@RecipeDetailActivity, "STEPS", Toast.LENGTH_SHORT).show()
//                    }
//                } else if (tab.text == "Ingredients") {
//                    writeBtn.setOnClickListener {
//                        Log.d(TAG, "onTabSelected: INGREDS")
//                        Toast.makeText(this@RecipeDetailActivity, "INGRED", Toast.LENGTH_SHORT).show()
//                    }
//                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun openDialog(view: TextView) {
        view.setOnClickListener {
            var builderTitle = ""
            var docField = ""
            if (view.tag.toString() == "recipe_name") {
                builderTitle = "Recipe name"
                docField = "title"
            } else if (view.tag.toString() == "recipe_desc") {
                builderTitle = "Recipe description"
                docField = "description"
            }
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle(builderTitle)
            val dialogLayout = inflater.inflate(R.layout.dialog_item, null)
            val editRecipeItemText = dialogLayout.findViewById<EditText>(R.id.edit_item)
            editRecipeItemText.setText(view.text)
            editRecipeItemText.setSelection(view.text.length)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Done") { dialogInterface, i ->
                view.text = editRecipeItemText.text.toString()
                mRecipeRef.update(docField, editRecipeItemText.text.toString())
            }
            builder.setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            builder.show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMicListener(speechRecognizerIntent: Intent, micBtn: FloatingActionButton) {
        micBtn.setOnClickListener(View.OnClickListener {
            speechRecognizer!!.startListening(speechRecognizerIntent)
        })
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), MainActivity.RecordAudioRequestCode)
        }
    }

}