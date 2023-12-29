package com.example.notesv2.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.example.notesv2.Activities.NoteActivity
import com.example.notesv2.R
import com.example.notesv2.databinding.FragmentSignInBinding


class SignInFragment : Fragment() {

    lateinit var binding: FragmentSignInBinding

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding=FragmentSignInBinding.bind(view)

        firebaseAuth= FirebaseAuth.getInstance()


        binding.gotosignup.setOnClickListener {

            Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_signUpFragment)

        }

        binding.gotoforgotpassword.setOnClickListener {

            Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_forgetPasswordFragment)

        }

        binding.login.setOnClickListener {
            val mail: String = binding.loginemail.text.toString().trim()
            val password: String = binding.loginpassword.text.toString().trim()
            if (mail.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Все поля обязательны для заполнения", Toast.LENGTH_SHORT)
                    .show()
            } else {


                binding.progressbar.visibility= View.VISIBLE

                firebaseAuth.signInWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {


                            checkMailVerification()
                        } else {
                            Toast.makeText(requireContext(),
                                "Аккаунт не существует",
                                Toast.LENGTH_SHORT)
                                .show()
                            binding.progressbar.visibility= View.INVISIBLE
                        }
                    }
            }

        }



    }

    private fun checkMailVerification() {


        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!!.isEmailVerified) {
            Toast.makeText(requireContext(), "Вход в систему", Toast.LENGTH_SHORT).show()
            binding.progressbar.visibility=View.INVISIBLE

            val intent = Intent(activity, NoteActivity::class.java)
            startActivity(intent)

            activity?.finish()




        } else {
            Toast.makeText(requireContext(), "Сначала подтвердите свою почту", Toast.LENGTH_SHORT).show()
            binding.progressbar.visibility=View.INVISIBLE
            firebaseAuth.signOut()
        }
    }


}