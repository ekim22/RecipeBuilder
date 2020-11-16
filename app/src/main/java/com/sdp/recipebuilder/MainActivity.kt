package com.sdp.recipebuilder


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.sdp.recipebuilder.adapter.RecipeAdapter
import com.sdp.recipebuilder.adapter.RecipeAdapter.OnRecipeSelectedListener
import com.sdp.recipebuilder.model.Recipe
import com.sdp.recipebuilder.model.Step
import com.sdp.recipebuilder.util.RecipeUtil
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, FirebaseAuth.AuthStateListener,
        OnRecipeSelectedListener {
    private var TAG = MainActivity::class.qualifiedName
//    private var docRef: DocumentReference = db?.collection("MyRecipes")!!.document()
    private var speechRecognizer: SpeechRecognizer? = null
    private var editText: EditText? = null
    private var micButton: ImageView? = null
    private var items: ArrayList<Step>? = null
    private var itemsAdapter: ArrayAdapter<Step>? = null
    private var recipeList: ListView? = null
    private var recipeRecycler: RecyclerView? = null
    private var toolbar: Toolbar? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var mQuery: Query
    private lateinit var recipes: ArrayList<Recipe>
    private lateinit var mAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recipeRecycler = findViewById(R.id.recycler_recipes)


//        recipeList = findViewById<View>(R.id.recipeSteps) as ListView
//        items = ArrayList()
//        itemsAdapter = ArrayAdapter(this,
//                android.R.layout.simple_list_item_1, items!!)
//        recipeList!!.adapter = itemsAdapter

        // Enable Firestore logging
//        FirebaseFirestore.setLoggingEnabled(true)

        val addBtn = findViewById<View>(R.id.fabAdd)

        addBtn.setOnClickListener {
            db.collection("recipes")
                    .add(RecipeUtil.getTemplateRecipe())
        }

        val touchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_baseline_delete_outline_24)
            private val background = ColorDrawable(Color.RED)
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                Log.d(TAG, "onSwiped: " + mAdapter.getSnapshot(viewHolder.adapterPosition).data)
                val recipeViewHolder = viewHolder as RecipeAdapter.RecipeViewHolder
                recipeViewHolder.deleteRecipe()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight
                val backgroundCornerOffset = 20
                when {
                    dX > 0 -> {
                        val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
                        val iconRight = itemView.left + iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                        background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom)
                    }
                    dX < 0 -> {
                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                        background.setBounds(itemView.right + dX.toInt() + backgroundCornerOffset, itemView.top, itemView.right, itemView.bottom)
                    }
                    else -> {
                        background.setBounds(0, 0, 0, 0)
                    }
                }
                background.draw(c)
                icon.draw(c)
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recipeRecycler)

        initFirestore()
        initRecyclerView()
        setupFireStoreListener()
    }

    private fun initFirestore() {
        db = FirebaseFirestore.getInstance()

        mQuery = db
                .collection("recipes")
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                .orderBy("title", Query.Direction.ASCENDING)
                .limit(LIMIT.toLong())
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
//        speechRecognizer!!.destroy()
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
        private const val LIMIT = 50
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
        db.collection("recipes")
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val docs = documents.documents
                    for (doc in docs) {
                        Log.d(TAG, "readFromDb: " + doc.data)
                    }
                }
    }

    private fun setupFireStoreListener() {
        db.collection("recipes")
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        Log.d(TAG, "--------------------------------")
                        var snapshotList = snapshot.documentChanges
                        for (snapshot in snapshotList) {
                            when (snapshot.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d(TAG, "Created: " + snapshot.document.toObject(Recipe::class.java).toString())
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    Log.d(TAG, "Modified: " + snapshot.document.toObject(Recipe::class.java).toString())
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.d(TAG, "Removed: " + snapshot.document.toObject(Recipe::class.java).toString())
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }
    }

    private fun addStep(v: View?, step: Step) {
        val step = hashMapOf(
                step.id to step.step,
                "uid" to FirebaseAuth.getInstance().currentUser!!.uid
        )

        db.collection("recipes")!!.document()
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
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
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
        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
        mAdapter.stopListening()
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

    private fun onAddRecipesClicked() {
        val recipes = db.collection("recipes")

        for (x in 0..2) {
            RecipeUtil.getRandomMealDb(this, recipes)
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        }
//        else {
//            FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
//                    .addOnSuccessListener { getTokenResult ->
//                        Log.d(TAG, "onSuccess: " + getTokenResult!!.token)
//                    }
//        }
    }

    private fun initRecyclerView() {
        mAdapter = object : RecipeAdapter(mQuery, this@MainActivity) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    recipeRecycler?.visibility = View.GONE
                } else {
                    recipeRecycler?.visibility = View.VISIBLE
                }
            }

            override fun onError(e: FirebaseFirestoreException?) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }

        }
        recipeRecycler?.layoutManager = LinearLayoutManager(this)
        recipeRecycler?.adapter = mAdapter
    }

    override fun onRecipeSelected(recipe: DocumentSnapshot?) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.KEY_RECIPE_ID, recipe?.id)
        startActivity(intent)
    }

    override fun handleDeleteRecipe(documentSnapshot: DocumentSnapshot?) {
        val recipe = documentSnapshot?.toObject(Recipe::class.java)

        documentSnapshot!!.reference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "handleDeleteRecipe: Item deleted")
                }

        recipeRecycler?.let {
            Snackbar.make(it, "Recipe deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        if (recipe != null) {
                            documentSnapshot.reference.set(recipe)
                        }
                    }.show()
        }

    }

    override fun handleEditRecipe(recipe: DocumentSnapshot?) {}

    override fun onClick(p0: View?) {
        Toast.makeText(this, "On click", Toast.LENGTH_SHORT).show()
    }

}
