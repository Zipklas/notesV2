package com.example.notesv2.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesv2.Activities.MainActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import com.example.notesv2.Adapters.NoteAdapter
import com.example.notesv2.Models.InformationModel
import com.example.notesv2.Models.NoteModel
import com.example.notesv2.Utils.SwipeGesture
import com.example.notesv2.Utils.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.notesv2.R
class NoteFragment : Fragment() {

    lateinit var addNoteFab:LinearLayout

    lateinit var noteUser:ImageView

    lateinit var search:EditText

    lateinit var chatFabText:TextView

    private var backPressedTime: Long = 0

    private var backToast: Toast? = null

    lateinit var rcNote:RecyclerView

    lateinit var noData:ImageView

    lateinit var options: FirestoreRecyclerOptions<NoteModel>

    lateinit var firebaseAdapter: NoteAdapter




    var layoutManager =
        StaggeredGridLayoutManager(2
            , StaggeredGridLayoutManager.VERTICAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val callback= object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    activity?.moveTaskToBack(true)
                    activity?.finish()
                    return
                } else {
                    val backToast = Toast.makeText(context, "Double Press to Exit", Toast.LENGTH_SHORT)
                    backToast.show()
                }
                backPressedTime = System.currentTimeMillis()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

        val view:View=inflater.inflate(R.layout.fragment_note, container, false)

        rcNote=view.findViewById(R.id.rc_note)

        setUpFirebaseAdapter()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        requireView().hideKeyboard()


        CoroutineScope(Dispatchers.Main).launch {
            delay(10)

            activity?.window?.statusBarColor= resources.getColor(android.R.color.transparent)

            activity?.window?.navigationBarColor=resources.getColor(android.R.color.transparent)

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        addNoteFab=view.findViewById(R.id.add_note_fab)
        rcNote=view.findViewById(R.id.rc_note)
        noData=view.findViewById(R.id.no_data)
        chatFabText=view.findViewById(R.id.chatFabText)
        noteUser=view.findViewById(R.id.note_user)
        search=view.findViewById(R.id.search)




        FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString()).addSnapshotListener { value, error ->


            layoutManager =
                StaggeredGridLayoutManager(2
                    , StaggeredGridLayoutManager.VERTICAL)

            rcNote.layoutManager = layoutManager
        }






        noteUser.setOnClickListener {

            val dialog = Dialog(requireContext(), R.style.BottomSheetDialogTheme)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.account_dialog)

            dialog.show()

            val userProfile: ImageView?=dialog.findViewById(R.id.user_profile)
            val userName:TextView?=dialog.findViewById(R.id.user_name)
            val userMail:TextView?=dialog.findViewById(R.id.user_mail)
            val userLogout:Button?=dialog.findViewById(R.id.user_logout)


            FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().uid.toString())
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val data= snapshot.getValue(InformationModel::class.java)


                        Picasso.get().load(data?.userProfilePhoto).placeholder(R.drawable.dp_holder).error(
                          R.drawable.dp_holder).into(userProfile!!)

                        userName?.text=data?.userName

                        userMail?.text=data?.userEmail


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }


                })



            userLogout?.setOnClickListener {

                FirebaseAuth.getInstance().signOut()

                try {
                    val googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("34676181147-2ke5akvf0jn05iml256o4vqrumldth7s.apps.googleusercontent.com")
                        .requestEmail()
                        .build()



                    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(requireActivity(),googleSignInOptions)

                    Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

                }
                catch (e:Exception){


                }



                startActivity(Intent(requireActivity(),MainActivity::class.java))

                requireActivity().finish()


            }


        }



        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                noData.visibility = View.GONE

                setUpFirebaseAdapter()

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!){

                    setUpFirebaseAdapter()

                }


                var query=FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").orderBy("title").startAt(s.toString()).endAt(s.toString()+"\uF8FF")

                options = FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query,NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner).build()

                firebaseAdapter=NoteAdapter(options,requireActivity())

                recyclerViewSetUp()


            }

            override fun afterTextChanged(s: Editable?) {

                if (s?.isEmpty()!!){

                    setUpFirebaseAdapter()

                }
                else{



                }


            }

        })



        search.setOnEditorActionListener { v, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }

            return@setOnEditorActionListener true

        }



        rcNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollX > oldScrollY -> {
                    chatFabText.visibility=View.GONE
                }
                scrollX == scrollY -> {
                    chatFabText.visibility = View.VISIBLE
                }
                else -> {
                    chatFabText.visibility = View.VISIBLE
                }
            }
        }



        addNoteFab.setOnClickListener {

            navController.navigate(R.id.action_noteFragment_to_saveEditFragment)

        }



        swipeToGesture(rcNote)

        FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").addSnapshotListener { value, error ->

                if (value!!.isEmpty) {

                    noData.visibility = View.VISIBLE

                } else {

                    noData.visibility = View.GONE

                }
            }


    }

    private fun swipeToGesture(rcNote: RecyclerView?) {

        val swipeGesture=object :SwipeGesture(requireContext()){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                var actionBtnTapped = false

                val position=viewHolder.position

                val previousId=firebaseAdapter.snapshots.getSnapshot(position).id

                val note=firebaseAdapter.getItem(position)

                try {

                    when(direction){

                        ItemTouchHelper.LEFT->{



                            FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                .collection("myNotes").document(firebaseAdapter.snapshots.getSnapshot(position).id).delete()

                            search.apply {
                                hideKeyboard()
                                clearFocus()
                            }

                            val snackBar = Snackbar.make(
                                requireView(), "Заметка удалена", Snackbar.LENGTH_LONG
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {

                                    transientBottomBar?.setAction("Отменить") {

                                        FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                            .collection("myNotes").document().set(note)


                                        actionBtnTapped = true
                                    }

                                    super.onShown(transientBottomBar)

                                }
                            }).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                                setAnchorView(R.id.add_note_fab)
                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.orangeRed
                                )
                            )
                            snackBar.show()




                        }



                        ItemTouchHelper.RIGHT->{


                            FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                .collection("myNotes").document(firebaseAdapter.snapshots.getSnapshot(position).id).delete()

                            search.apply {
                                hideKeyboard()
                                clearFocus()
                            }

                            val snackBar = Snackbar.make(
                                requireView(), "Заметка удалена", Snackbar.LENGTH_LONG
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {

                                    transientBottomBar?.setAction("Отменить") {

                                        FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                            .collection("myNotes").document().set(note)


                                        actionBtnTapped = true
                                    }

                                    super.onShown(transientBottomBar)

                                }
                            }).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                                setAnchorView(R.id.add_note_fab)
                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.orangeRed
                                )
                            )
                            snackBar.show()



                        }


                    }

                }
                catch (e:Exception){

                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()

                }



            }


        }

        val touchHelper=ItemTouchHelper(swipeGesture)

        touchHelper.attachToRecyclerView(rcNote)


    }


    private fun setUpFirebaseAdapter(){

        val query:Query= FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").orderBy("date",Query.Direction.DESCENDING)

        options = FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query,NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner).build()

        firebaseAdapter=NoteAdapter(options,requireActivity())

        recyclerViewSetUp()

    }

    private fun recyclerViewSetUp(){

        rcNote.setHasFixedSize(true)

        rcNote.adapter=firebaseAdapter

        rcNote.layoutManager=layoutManager

        rcNote.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

    }

    override fun onStart() {
        super.onStart()



        rcNote.recycledViewPool.clear()
        firebaseAdapter.notifyDataSetChanged()
        firebaseAdapter.startListening()

    }



    override fun onStop() {
        super.onStop()

        firebaseAdapter.stopListening()

    }

    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context

    }

    override fun onDetach() {
        super.onDetach()

        mContext = null

    }



}
