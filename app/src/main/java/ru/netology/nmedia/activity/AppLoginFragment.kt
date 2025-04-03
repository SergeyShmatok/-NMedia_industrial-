package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAppLoginBinding
import ru.netology.nmedia.util.AndroidUtils.hideKeyboard
import ru.netology.nmedia.viewmodel.LoginViewModel

class AppLoginFragment: Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAppLoginBinding.inflate(inflater, container, false)

        val loginViewModel by viewModels<LoginViewModel>()


loginViewModel.loginSuccessful.observe(viewLifecycleOwner) {
    loginViewModel.toastFun(true)
    findNavController().navigate(R.id.action_application_login_fragment_to_feedFragment)

}

        loginViewModel.loginState.observe(viewLifecycleOwner) { stateModel ->
            if (stateModel.error) loginViewModel.toastFun()
            binding.loading.isVisible = stateModel.loading
        }


        binding.signInButton.setOnClickListener {

            hideKeyboard(requireView())

            fun colorSetter(color: Int) = resources.getColor(color, null)

            val loginLength = binding.logIn.editText?.text.isNullOrBlank()
            val passwordLength = binding.password.editText?.text.isNullOrBlank()

            if ( loginLength || passwordLength) {
                Snackbar.make(binding.root, "  -  Поле логина или пароля не может быть пустым  -",
                    Snackbar.LENGTH_LONG).setTextColor(colorSetter(R.color.curry_yellow))
                    .setAnchorView(R.id.snack_bar_anchor).show()

                return@setOnClickListener }

                val login = binding.logIn.editText?.text?.trim().toString()

                val pass = binding.password.editText?.text?.trim().toString()

                loginViewModel.checkingUserLogin(login, pass)


        }


        return binding.root

    }

}