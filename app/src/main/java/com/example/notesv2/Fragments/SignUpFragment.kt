package com.example.notesv2.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.example.notesv2.Models.InformationModel
import com.example.notesv2.R
import com.example.notesv2.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {

    lateinit var binding: FragmentSignUpBinding

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding=FragmentSignUpBinding.bind(view)

        firebaseAuth=FirebaseAuth.getInstance()

        firebaseDatabase=FirebaseDatabase.getInstance()

        binding.gotologin.setOnClickListener {

            Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment)

        }

        binding.signup.setOnClickListener {

            val mail = binding.signupemail.text.toString().trim()
            val password = binding.signuppassword.text.toString().trim()
            val repassword = binding.signuprepassword.text.toString().trim()



            if (mail.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
                Toast.makeText(requireContext(), "Все поля обязательны для заполнения", Toast.LENGTH_SHORT)
                    .show()
            } else if (password.length < 7) {


                Toast.makeText(requireContext(), "Пароль должен быть не менее 8 символов", Toast.LENGTH_SHORT)
                    .show()
            } else if (password != repassword) {

                Toast.makeText(requireContext(), "Пароль не совпадают", Toast.LENGTH_SHORT)
                    .show()

            } else {

                binding.progressbar.visibility= View.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

                            val user = InformationModel(binding.signupname.text.toString(),
                                binding.signupemail.text.toString(),
                                binding.signuppassword.text.toString(),
                                "https://res.cloudinary.com/sunayan02/image/upload/v1659941770/suer_myphid.jpg",
                                firebaseUser?.uid.toString()
                            )

                            if (firebaseUser != null) {
                                firebaseDatabase.reference.child("Users").child(firebaseUser.uid)
                                    .setValue(user)
                            }

                            Toast.makeText(requireContext(),
                                "Регистрация успешна!",
                                Toast.LENGTH_SHORT).show()
                            sendEmailVerification()




                        } else {
                            Toast.makeText(requireContext(),
                                "Ошибка регистрации",
                                Toast.LENGTH_SHORT).show()
                            binding.progressbar.visibility=View.INVISIBLE
                        }

                    }

            }


        }



    }


    private fun sendEmailVerification() {

        var firebaseUser: FirebaseUser? =firebaseAuth.currentUser
        if (firebaseAuth!=null){

            binding.progressbar.visibility=View.INVISIBLE

            firebaseUser?.sendEmailVerification()?.addOnSuccessListener {


                Toast.makeText(
                    requireContext(), "Письмо с подтверждением отправлено \n Подтвердите и войдите снова", Toast.LENGTH_SHORT).show()

                Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_signInFragment)



            }
        }
        else{

            Toast.makeText(requireContext(), "Ошибка при отправке письма верификации", Toast.LENGTH_SHORT).show()

        }
    }

}