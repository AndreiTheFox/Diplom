package ru.fox.diplom.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.fox.diplom.R
import ru.fox.diplom.databinding.FragmentRegisterBinding
import ru.fox.diplom.util.AndroidUtils
import ru.fox.diplom.viewmodel.AuthViewModel
import ru.fox.diplom.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val authViewModel: AuthViewModel by activityViewModels()

        val binding = FragmentRegisterBinding.inflate(
            inflater,
            container,
            false
        )
        val registerViewModel: RegisterViewModel by activityViewModels()

        binding.navBack.setOnClickListener {
            findNavController().navigateUp()
        }


        registerViewModel.dataState.observe(viewLifecycleOwner) { state ->
            when {
                state.userAlreadyExists -> Toast.makeText(
                    context,
                    R.string.user_already_exists.toString(),
                    Toast.LENGTH_LONG
                ).show()

                state.error -> Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG)
                    .show()
            }

        }
        authViewModel.state.observe(viewLifecycleOwner) {
            binding.register.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())

                val userName = binding.userName.text.toString()
                val userLogin = binding.userLogin.text.toString()
                val userPassword = binding.password.text.toString()
                val userRepeatPassword = binding.repeatPassword.text.toString()

                //Check fields are not empty
                if (userName.isBlank() || userLogin.isBlank() || userPassword.isBlank() || userRepeatPassword.isBlank()) {
                    Snackbar.make(binding.root, R.string.noLoginData, Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                //If password and repeat password are not equal
                if (userPassword != userRepeatPassword) {
                    Snackbar.make(
                        binding.root,
                        R.string.passwords_are_not_equal,
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                registerViewModel.tryRegister(
                    login = userLogin,
                    password = userPassword,
                    username = userName
                )
            }
            if (authViewModel.authorized) {
                findNavController().navigate(R.id.feedFragment)
            }
        }

        return binding.root
    }
}